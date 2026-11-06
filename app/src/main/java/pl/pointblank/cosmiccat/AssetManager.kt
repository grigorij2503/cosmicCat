package pl.pointblank.cosmiccat

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.LruCache
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

class AssetManager(private val context: Context) {
    private val cache = object : LruCache<Any, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: Any, value: Bitmap) = value.allocationByteCount
    }
    private data class RocketKey(val vehicle: Vehicle, val cat: CatStyle)
    private data class RoverKey(val vehicle: Vehicle)
    private val full = RectF(0f, 0f, 320f, 320f)

    private fun get(key: Any, make: () -> Bitmap): Bitmap =
        cache.get(key) ?: make().also { cache.put(key, it) }

    @SuppressLint("DiscouragedApi")
    private fun png(name: String, fallback: () -> Bitmap) = get(name) {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        val bitmap = if (id == 0) null else BitmapFactory.decodeResource(context.resources, id)
        bitmap ?: fallback()
    }

    private fun photo(name: String?): Bitmap? {
        if (name == null) return null
        val file = File(context.filesDir, name)
        return cache.get(file) ?: BitmapFactory.decodeFile(file.path)?.also { cache.put(file, it) }
    }

    private fun CatStyle.visual() = copy(name = "", gender = Gender.BOY)
    private fun Vehicle.rocketStyle() = copy(wheels = Wheels.CLASSIC, boots = Boots.CLASSIC)

    private fun catBitmap(style: CatStyle): Bitmap = get(style.visual()) {
        val picture = photo(style.photo)
        val base = when {
            picture != null -> ProceduralBitmaps.catBase(style.fur, face = false)
            style.fur == PaintColor.ORANGE -> png("cat_base") { ProceduralBitmaps.catBase(style.fur) }
            else -> ProceduralBitmaps.catBase(style.fur)
        }
        val suit = if (style.suit == PaintColor.PURPLE) {
            png("cat_suit_overlay") { ProceduralBitmaps.suit(style.suit) }
        } else ProceduralBitmaps.suit(style.suit)
        BitmapFactoryGenerator.create {
            if (style.hair != Hair.NONE) image(ProceduralBitmaps.hair(style.hair, style.hairColor), full)
            image(base, full)
            if (picture != null) maskedImage(picture, RectF(83f, 37f, 237f, 191f), true)
            image(suit, full)
        }
    }

    fun cat(style: CatStyle) = catBitmap(style).asImageBitmap()

    fun catX() = get("catx-commander", ProceduralBitmaps::commander).asImageBitmap()

    fun rocket(vehicle: Vehicle, cat: CatStyle) = get(RocketKey(vehicle.rocketStyle(), cat.visual())) {
        val picture = photo(vehicle.photo)
        val body = if (vehicle.rocketStyle().copy(photo = null) == Vehicle()) {
            png("rocket_base") { ProceduralBitmaps.rocket(vehicle) }
        } else ProceduralBitmaps.rocket(vehicle)
        val avatar = catBitmap(cat)
        BitmapFactoryGenerator.create {
            if (picture == null) {
                image(body, full)
                with(ProceduralBitmaps) { window(avatar, 160f, 126f, 30f) }
            } else {
                with(ProceduralBitmaps) { flame() }
                maskedImage(picture, RectF(25f, 9f, 295f, 259f), false)
                with(ProceduralBitmaps) { window(avatar, 208f, 149f, 30f) }
            }
        }
    }.asImageBitmap()

    fun rover(vehicle: Vehicle) = get(RoverKey(Vehicle(paint = vehicle.paint, wheels = vehicle.wheels))) {
        ProceduralBitmaps.rover(vehicle)
    }.asImageBitmap()

    fun planet(destination: Destination) = png("planet_${destination.name.lowercase(java.util.Locale.ROOT)}") {
        ProceduralBitmaps.planet(destination)
    }.asImageBitmap()

    fun alien(alien: Alien) = png("alien_friend_${alien.ordinal + 1}") {
        ProceduralBitmaps.alien(alien)
    }.asImageBitmap()

    fun discovery(item: Discovery) = when (item) {
        Discovery.METEOR -> png("golden_meteorite") { ProceduralBitmaps.discovery(item) }.asImageBitmap()
        Discovery.FRIEND -> alien(Alien.KROPKA)
        else -> get(item) { ProceduralBitmaps.discovery(item) }.asImageBitmap()
    }
}
