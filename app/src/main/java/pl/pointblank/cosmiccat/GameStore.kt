package pl.pointblank.cosmiccat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.util.UUID

private val photoPattern = Regex("sprite-[0-9a-f-]{36}\\.png")

internal sealed interface GameLoad {
    data class Ready(val progress: Progress) : GameLoad
    data class Recovery(val canRestore: Boolean) : GameLoad
}

internal class UnsupportedSaveVersion : IOException()

internal class GameStore(private val directory: File) {
    private val save = File(directory, "cosmic-cat.json")
    private val previous = File(directory, "cosmic-cat.json.previous")

    fun load(): GameLoad = synchronized(fileLock) {
        val bytes = read(save)
        if (bytes == null && !previous.exists()) return GameLoad.Ready(Progress())
        try {
            if (bytes != null) return GameLoad.Ready(decode(bytes))
        } catch (_: JSONException) {
            // Leave the original file in place until the player chooses how to recover.
        }
        GameLoad.Recovery(canRestore = readPrevious() != null)
    }

    fun update(expected: Progress, change: (Progress) -> Progress): Progress = synchronized(fileLock) {
        val stored = read(save)
        val current = stored?.let(::decode) ?: Progress()
        if (current != expected) throw IOException("Game save changed")
        val next = change(current)
        if (next != current) {
            if (stored != null) replace(previous, stored)
            replace(save, encode(next))
            cleanPhotos(next, current)
        }
        next
    }

    fun recover(choice: GameLoad.Recovery): Progress = synchronized(fileLock) {
        if (load() != choice) throw IOException("Game save changed")
        val restored = if (choice.canRestore) {
            readPrevious() ?: throw IOException("Backup is unavailable")
        } else Progress()
        read(save)?.let { replace(File(directory, "cosmic-cat-damaged-${UUID.randomUUID()}.json"), it) }
        replace(save, encode(restored))
        restored
    }

    private fun readPrevious(): Progress? = try {
        read(previous)?.let(::decode)
    } catch (_: JSONException) {
        null
    } catch (_: UnsupportedSaveVersion) {
        null
    }

    private fun decode(bytes: ByteArray) = GameSaveJson.decode(bytes.toString(Charsets.UTF_8))
    private fun encode(progress: Progress) = GameSaveJson.encode(progress).toByteArray(Charsets.UTF_8)

    private fun read(file: File): ByteArray? = try {
        Files.readAllBytes(file.toPath())
    } catch (_: NoSuchFileException) {
        null
    }

    private fun replace(file: File, bytes: ByteArray) {
        val temporary = File.createTempFile("cosmic-cat-", ".tmp", directory)
        try {
            temporary.outputStream().use {
                it.write(bytes)
                it.fd.sync()
            }
            Files.move(temporary.toPath(), file.toPath(), ATOMIC_MOVE)
        } finally {
            temporary.delete()
        }
    }

    private fun cleanPhotos(active: Progress, backup: Progress) {
        val kept = setOf(active.cat.photo, active.vehicle.photo, backup.cat.photo, backup.vehicle.photo)
        try {
            directory.listFiles()?.forEach { file ->
                if (photoPattern.matches(file.name) && file.name !in kept) file.delete()
            }
        } catch (_: SecurityException) {
            // Cleanup is optional once the save has been committed.
        }
    }

    private companion object {
        val fileLock = Any()
    }
}

internal fun importSprite(context: Context, uri: Uri): String {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val size = info.size
        val scale = 640f / maxOf(size.width, size.height)
        if (scale < 1f) {
            decoder.setTargetSize((size.width * scale).toInt().coerceAtLeast(1),
                (size.height * scale).toInt().coerceAtLeast(1))
        }
    }
    val name = "sprite-${UUID.randomUUID()}.png"
    val file = File(context.filesDir, name)
    try {
        file.outputStream().use {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) throw IOException()
            it.fd.sync()
        }
    } catch (error: Exception) {
        file.delete()
        throw error
    } finally {
        bitmap.recycle()
    }
    return name
}

internal object GameSaveJson {
    private const val SCHEMA_VERSION = 2
    private val legacyMissionOrder = listOf(
        Destination.MARS, Destination.PLUTO, Destination.VENUS,
        Destination.JUPITER, Destination.NEPTUNE, Destination.SATURN,
    )

    fun decode(stored: String): Progress {
        val json = JSONObject(stored)
        val version = if (json.has("schemaVersion")) json.getInt("schemaVersion") else 1
        if (version > SCHEMA_VERSION) throw UnsupportedSaveVersion()
        if (version < 1) throw JSONException("Invalid save schema version")
        val completed = json.optInt("completed").coerceAtLeast(0)
        val activeMission = if (version == 1) legacyMissionOrder[completed % legacyMissionOrder.size] else {
            val id = json.getString("activeMission")
            missionsByDestination.keys.firstOrNull { it.name == id } ?: throw JSONException("Invalid active mission")
        }
        val cat = json.optJSONObject("cat") ?: JSONObject()
        val vehicle = json.optJSONObject("vehicle") ?: JSONObject()
        val buildings = json.optJSONObject("buildings") ?: JSONObject()
        val defaults = Progress()
        return Progress(
            started = json.getBoolean("started"),
            cat = CatStyle(
                name = cat.optString("name", defaults.cat.name),
                gender = cat.readEnum("gender", defaults.cat.gender),
                fur = cat.readEnum("fur", defaults.cat.fur),
                hair = cat.readEnum("hair", defaults.cat.hair),
                hairColor = cat.readEnum("hairColor", defaults.cat.hairColor),
                suit = cat.readEnum("suit", defaults.cat.suit),
                photo = cat.readPhoto(),
            ),
            vehicle = Vehicle(
                nose = vehicle.readEnum("nose", defaults.vehicle.nose),
                fins = vehicle.readEnum("fins", defaults.vehicle.fins),
                paint = vehicle.readEnum("paint", defaults.vehicle.paint),
                wheels = vehicle.readEnum("wheels", defaults.vehicle.wheels),
                boots = vehicle.readEnum("boots", defaults.vehicle.boots),
                photo = vehicle.readPhoto(),
            ),
            location = json.readEnum("location", defaults.location),
            completed = completed,
            found = json.optBoolean("found"),
            rewardPending = json.optBoolean("rewardPending"),
            coins = json.optInt("coins").coerceAtLeast(0),
            buildings = Building.entries.filter { buildings.has(it.name) }
                .associateWith { buildings.optInt(it.name).coerceIn(0, MAX_LEVEL) },
            friends = json.readSet<Alien>("friends"),
            visited = json.readSet<Destination>("visited"),
            trailStages = json.readDestinationMap<TrailStage>("trailStages"),
            stickers = json.readSet<Destination>("stickers"),
            seeds = json.readSet<Destination>("seeds"),
            plants = json.readDestinationMap<PlantStage>("plants"),
            requests = json.readSet<Alien>("requests"),
            activeMission = activeMission,
        )
    }

    fun encode(progress: Progress): String {
        val json = JSONObject().apply {
            put("schemaVersion", SCHEMA_VERSION)
            put("activeMission", progress.activeMission.name)
            put("started", progress.started)
            put("location", progress.location.name)
            put("completed", progress.completed)
            put("found", progress.found)
            put("rewardPending", progress.rewardPending)
            put("coins", progress.coins)
            put("friends", JSONArray(progress.friends.map { it.name }))
            put("visited", JSONArray(progress.visited.map { it.name }))
            put("trailStages", progress.trailStages.asJson())
            put("stickers", JSONArray(progress.stickers.map { it.name }))
            put("seeds", JSONArray(progress.seeds.map { it.name }))
            put("plants", progress.plants.asJson())
            put("requests", JSONArray(progress.requests.map { it.name }))
            put("buildings", JSONObject(progress.buildings.mapKeys { it.key.name }))
            put("cat", JSONObject().apply {
                put("name", progress.cat.name)
                put("gender", progress.cat.gender.name)
                put("fur", progress.cat.fur.name)
                put("hair", progress.cat.hair.name)
                put("hairColor", progress.cat.hairColor.name)
                put("suit", progress.cat.suit.name)
                put("photo", progress.cat.photo)
            })
            put("vehicle", JSONObject().apply {
                put("nose", progress.vehicle.nose.name)
                put("fins", progress.vehicle.fins.name)
                put("paint", progress.vehicle.paint.name)
                put("wheels", progress.vehicle.wheels.name)
                put("boots", progress.vehicle.boots.name)
                put("photo", progress.vehicle.photo)
            })
        }
        return json.toString()
    }

    private fun JSONObject.readPhoto() = optString("photo").takeIf(photoPattern::matches)

    private inline fun <reified T : Enum<T>> JSONObject.readEnum(key: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == optString(key) } ?: default

    private inline fun <reified T : Enum<T>> JSONObject.readSet(key: String): Set<T> {
        val values = optJSONArray(key) ?: return emptySet()
        return (0 until values.length()).mapNotNull { index ->
            enumValues<T>().firstOrNull { it.name == values.optString(index) }
        }.toSet()
    }

    private inline fun <reified T : Enum<T>> JSONObject.readDestinationMap(key: String): Map<Destination, T> {
        val values = optJSONObject(key) ?: return emptyMap()
        return Destination.entries.mapNotNull { destination ->
            val saved = values.optString(destination.name)
            enumValues<T>().firstOrNull { it.name == saved }?.let { destination to it }
        }.toMap()
    }

    private fun Map<Destination, Enum<*>>.asJson() = JSONObject(
        entries.associate { (destination, value) -> destination.name to value.name },
    )
}
