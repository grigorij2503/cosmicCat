package pl.pointblank.cosmiccat

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class GameStoreTest {
    @get:Rule val files = TemporaryFolder()

    @Test
    fun restoresPreviousSaveAndItsPhotoWhenCurrentSaveIsDamaged() {
        val store = GameStore(files.root)
        val photo = files.newFile("sprite-445b7987-19f8-401e-baa2-cf1066c5d0b4.png")
        val first = store.update(Progress()) { it.copy(started = true, cat = it.cat.copy(photo = photo.name)) }
        store.update(first) { it.copy(coins = REWARD, cat = it.cat.copy(photo = null)) }
        val damaged = File(files.root, "cosmic-cat.json")
        val invalidJson = """{"started":"""
        damaged.writeText(invalidJson)

        val recovery = GameLoad.Recovery(canRestore = true)
        assertEquals(recovery, store.load())
        assertEquals(first, store.recover(recovery))
        assertEquals(GameLoad.Ready(first), store.load())
        assertTrue(photo.isFile)
        assertEquals(invalidJson, files.root.listFiles()!!.single { it.name.startsWith("cosmic-cat-damaged-") }.readText())
    }

    @Test
    fun startsANewGameAfterExplicitRecoveryWhenNoBackupExists() {
        val store = GameStore(files.root)
        val invalidJson = "{}"
        files.newFile("cosmic-cat.json").writeText(invalidJson)

        val recovery = GameLoad.Recovery(canRestore = false)
        assertEquals(recovery, store.load())
        assertEquals(Progress(), store.recover(recovery))
        assertEquals(GameLoad.Ready(Progress()), store.load())
        assertEquals(invalidJson, files.root.listFiles()!!.single { it.name.startsWith("cosmic-cat-damaged-") }.readText())
    }

    @Test
    fun keepsPreviousSaveAndPhotoWhenCommitFails() {
        val store = GameStore(files.root)
        val photo = files.newFile("sprite-445b7987-19f8-401e-baa2-cf1066c5d0b4.png")
        val initial = store.update(Progress()) { it.copy(started = true, cat = it.cat.copy(photo = photo.name)) }
        files.newFolder("cosmic-cat.json.previous").resolve("blocker").writeText("block atomic replacement")

        assertThrows(IOException::class.java) {
            store.update(initial) { it.copy(cat = it.cat.copy(photo = null)) }
        }
        assertEquals(GameLoad.Ready(initial), store.load())
        assertTrue(photo.isFile)
    }

    @Test
    fun refusesToOverwriteProgressFromAnotherStoreInstance() {
        val store = GameStore(files.root)
        val newer = GameStore(files.root).update(Progress()) { it.copy(started = true, coins = REWARD) }

        assertThrows(IOException::class.java) {
            store.update(Progress()) { it.copy(started = true) }
        }
        assertEquals(GameLoad.Ready(newer), store.load())
    }

    @Test
    fun preservesNewerSaveVersions() {
        val store = GameStore(files.root)
        val newer = """{"schemaVersion":99}"""
        val save = files.newFile("cosmic-cat.json").apply { writeText(newer) }

        assertThrows(UnsupportedSaveVersion::class.java) { store.load() }
        assertThrows(UnsupportedSaveVersion::class.java) { store.recover(GameLoad.Recovery(false)) }
        assertEquals(newer, save.readText())
    }
}
