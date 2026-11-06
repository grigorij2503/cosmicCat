package pl.pointblank.cosmiccat

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin

private enum class Landmark {
    PAD, MESA, GARAGE, CRATER, ICE, AIRLOCK, ICE_GARDEN, ARCH, MIRRORS, TELESCOPE,
    BALLOON, ANTENNA, STORM, WINDSOCK, WEATHER, TURBINES, RINGS, SORTER, WINDOW,
}

private val landmarks = mapOf(
    Destination.MARS to listOf(Landmark.PAD, Landmark.GARAGE, Landmark.CRATER),
    Destination.PLUTO to listOf(Landmark.ICE, Landmark.AIRLOCK, Landmark.ICE_GARDEN),
    Destination.VENUS to listOf(Landmark.ARCH, Landmark.MIRRORS, Landmark.TELESCOPE),
    Destination.JUPITER to listOf(Landmark.BALLOON, Landmark.ANTENNA, Landmark.STORM),
    Destination.NEPTUNE to listOf(Landmark.WINDSOCK, Landmark.WEATHER, Landmark.TURBINES),
    Destination.SATURN to listOf(Landmark.RINGS, Landmark.SORTER, Landmark.WINDOW),
)

internal fun DrawScope.expeditionSky(planet: Destination, camera: Float, width: Float, extension: Float) {
    val tint = colorOf(planet.color)
    val soil = colorOf(planet.ground)
    val dusk = colorOf(planet.sky)
    val horizon = if (planet.cloudStation) tint.mix(dusk, .48f) else soil.mix(dusk, .34f)
    drawRect(Brush.verticalGradient(listOf(Midnight, dusk, horizon), -extension, GROUND),
        Offset(camera, -extension), Size(width, WORLD_HEIGHT + extension))
    val center = Offset(camera + width * .69f, 165f - extension * .37f)
    val halo = width * .62f
    drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = .26f), tint.copy(alpha = .07f), Color.Transparent),
        center, halo), halo, center)
    when (planet) {
        Destination.PLUTO, Destination.NEPTUNE -> {
            val left = camera - 80f
            val y = 89f - extension * .42f
            repeat(3) { i ->
                val n = i * 15f
                shape(if (i == 1) Mint.copy(alpha = .10f) else SkyBlue.copy(alpha = .13f)) {
                    moveTo(left, y + n + 50)
                    cubicTo(left + width * .34f, y + n - 75, left + width * .69f, y + n + 90, left + width + 160, y + n - 34)
                    cubicTo(left + width * .66f, y + n + 126, left + width * .34f, y + n - 48, left, y + n + 69)
                    close()
                }
            }
        }
        Destination.JUPITER, Destination.VENUS -> {
            val y = 120f - extension * .24f
            repeat(4) { i ->
                val x = camera - 50f + i * width * .21f
                ellipse(tint.copy(alpha = .10f - i * .014f), x, y + i * 36f, width * .66f, 20f + i * 4f)
            }
        }
        Destination.SATURN -> rotate(-16f, center) {
            repeat(3) { i ->
                val h = 86f + i * 23f
                drawOval(tint.copy(alpha = .12f - i * .025f), Offset(camera - 120f, center.y - h / 2),
                    Size(width + 240f, h), style = Stroke(6f - i))
            }
        }
        Destination.MARS -> {
            val y = 152f - extension * .18f
            repeat(3) { i ->
                ellipse(tint.copy(alpha = .07f), camera + width * (.06f + .26f * i), y + 23 * i, 116f, 9f)
            }
        }
        Destination.MOON -> Unit
    }
}

internal fun DrawScope.expeditionLandmarks(trail: Trail, camera: Float, width: Float, time: Float) {
    val tint = colorOf(trail.destination.color)
    val points = listOf(177f, trail.stationX - 26f, trail.goalX + 8f)
    landmarks.getValue(trail.destination).forEachIndexed { i, kind ->
        val x = points[i]
        if (x !in camera - 170f..camera + width + 170f) return@forEachIndexed
        sceneAt(x, GROUND, if (i == 2) 1.14f else 1f) { landmark(kind, tint, time) }
    }
    // A smaller piece at the entrance of the middle zone connects it visually with the station.
    val marker = trail.second.x + 100f
    if (marker in camera - 120f..camera + width + 120f) sceneAt(marker, GROUND, .67f) {
        val sign = when (trail.destination) {
            Destination.MARS -> Landmark.MESA
            Destination.PLUTO -> Landmark.ICE
            Destination.VENUS -> Landmark.MIRRORS
            Destination.JUPITER -> Landmark.ANTENNA
            Destination.NEPTUNE -> Landmark.WINDSOCK
            else -> Landmark.RINGS
        }
        landmark(sign, tint.mix(Panel, .24f), time)
    }
}

private fun DrawScope.landmark(kind: Landmark, tint: Color, time: Float) {
    val frame = tint.mix(Panel, .48f)
    val light = tint.mix(Snow, .46f)
    when (kind) {
        Landmark.PAD -> {
            ellipse(frame, -115f, -11f, 230f, 21f)
            ellipse(light.copy(alpha = .61f), -97f, -8f, 194f, 11f)
            ellipse(frame, -76f, -6f, 152f, 8f)
            for (x in listOf(-82f, 79f)) {
                outlinedTile(frame, x - 5, -45f, 10f, 39f, 4f)
                drawCircle(Gold, 5f, Offset(x, -44f))
            }
            mast(-110f, 91f, frame, tint)
            stripe(light, -111f, -82f, -75f, -82f, 4f)
            shape(tint, 1.8f) { moveTo(-109f, -83f); lineTo(-73f, -83f); lineTo(-82f, -67f); lineTo(-109f, -68f); close() }
            twinkle(Snow, -96f, -75f, 4f)
        }
        Landmark.MESA -> {
            shape(frame, 2f) {
                moveTo(-79f, 0f); lineTo(-68f, -39f); lineTo(-41f, -60f); lineTo(-33f, -86f)
                lineTo(34f, -86f); lineTo(44f, -59f); lineTo(66f, -38f); lineTo(80f, 0f); close()
            }
            stripe(light.copy(alpha = .61f), -53f, -40f, 51f, -40f, 4f)
            stripe(light.copy(alpha = .36f), -59f, -22f, 59f, -22f, 3f)
        }
        Landmark.GARAGE -> {
            outlinedTile(frame, -112f, -89f, 221f, 88f, 12f)
            outlinedTile(tint, -126f, -103f, 250f, 18f, 7f)
            tile(Midnight, -44f, -76f, 88f, 76f, 9f)
            tile(frame.mix(Midnight, .42f), -36f, -69f, 72f, 68f, 6f)
            for (y in listOf(-57f, -45f, -33f, -21f)) stripe(frame, -34f, y, 34f, y, 2f)
            outlinedTile(SkyBlue.mix(frame, .34f), -96f, -68f, 40f, 29f, 6f)
            stripe(light, -83f, -65f, -90f, -50f, 2f)
            outlinedTile(frame, 60f, -39f, 30f, 39f, 5f)
            for (i in 0..2) stripe(Gold, 65f, -30f + i * 8f, 81f, -30f + i * 8f, 2f)
            mast(89f, 132f, frame, Gold)
        }
        Landmark.CRATER -> {
            ellipse(Midnight.copy(alpha = .47f), -161f, -20f, 322f, 45f)
            ellipse(frame, -155f, -27f, 310f, 45f)
            ellipse(tint.mix(Midnight, .22f), -144f, -23f, 288f, 34f)
            ellipse(frame.mix(Midnight, .54f), -123f, -17f, 246f, 21f)
            for (side in listOf(-1, 1)) {
                val x = side * 112f
                shape(frame, 2f) {
                    moveTo(x - 40, -3f); lineTo(x - 24, -31f); lineTo(x + 4, -47f)
                    lineTo(x + 36, -24f); lineTo(x + 41, -4f); close()
                }
                stripe(light, x - 23, -28f, x + 3, -42f, 3f)
            }
        }
        Landmark.ICE, Landmark.ICE_GARDEN -> {
            val blue = if (kind == Landmark.ICE) SkyBlue else Mint
            val range = if (kind == Landmark.ICE) -1..1 else -2..2
            for (i in range) crystal(i * 35f, 56f + (2 - kotlin.math.abs(i)) * 19, blue.mix(Lavender, (i + 2) * .14f))
            if (kind == Landmark.ICE_GARDEN) {
                twinkle(Snow, -63f, -105f, 8f)
                twinkle(Mint, 77f, -88f, 6f)
            }
        }
        Landmark.AIRLOCK -> {
            outlinedTile(SkyBlue.mix(frame, .2f), -111f, -100f, 222f, 100f, 42f)
            outlinedTile(frame, -58f, -82f, 116f, 82f, 31f)
            tile(Midnight, -37f, -66f, 74f, 65f, 23f)
            tile(SkyBlue.copy(alpha = .44f), -32f, -61f, 64f, 60f, 20f)
            stripe(frame, 0f, -63f, 0f, -2f, 3f)
            for (x in listOf(-83f, 83f)) {
                drawCircle(Midnight, 13f, Offset(x, -43f))
                drawCircle(Mint, 9f, Offset(x, -43f))
            }
        }
        Landmark.ARCH -> {
            for (side in listOf(-1, 1)) {
                val x = side * 91f
                shape(frame, 2f) {
                    moveTo(x - 31, 0f); lineTo(x - 18, -99f); lineTo(x + 2, -125f)
                    lineTo(x + 25, -111f); lineTo(x + 31, -56f); lineTo(x + 34, 0f); close()
                }
                stripe(light, x - 17, -72f, x + 24, -72f, 3f)
            }
            shape(tint.mix(frame, .34f), 2f) {
                moveTo(-104f, -99f); lineTo(-82f, -132f); lineTo(29f, -149f)
                lineTo(99f, -130f); lineTo(119f, -103f); lineTo(81f, -91f)
                lineTo(47f, -112f); lineTo(-42f, -104f); lineTo(-70f, -91f); close()
            }
        }
        Landmark.MIRRORS -> {
            for (i in -1..1) {
                val x = i * 67f
                val y = -70f - (1 - kotlin.math.abs(i)) * 25
                mast(x, -y, frame, light)
                rotate(-22f + i * 13f, Offset(x, y)) {
                    outlinedTile(tint, x - 28, y - 22, 56f, 44f, 8f)
                    tile(SkyBlue, x - 21, y - 15, 42f, 30f, 4f)
                    stripe(Snow, x - 13, y + 9, x + 9, y - 10, 3f)
                }
            }
        }
        Landmark.TELESCOPE -> {
            outlinedTile(frame, -69f, -40f, 138f, 39f, 13f)
            stripe(frame, 7f, -43f, 7f, -82f, 17f)
            rotate(-32f, Offset(0f, -86f)) {
                outlinedTile(tint, -79f, -105f, 150f, 38f, 13f)
                outlinedTile(light, 48f, -111f, 31f, 50f, 8f)
                tile(SkyBlue, 68f, -103f, 8f, 34f, 4f)
                stripe(Snow.copy(alpha = .62f), -52f, -97f, 30f, -97f, 3f)
            }
        }
        Landmark.BALLOON -> {
            mast(0f, 78f, frame, Mint)
            val y = -150f + sin(time * 1.6f) * 4
            stripe(light.copy(alpha = .57f), 0f, -45f, 21f, y + 42f, 2f)
            ellipse(Midnight, -33f, y - 40f, 112f, 77f)
            ellipse(tint.mix(Coral, .28f), -30f, y - 37f, 106f, 71f)
            ellipse(light, -17f, y - 28f, 62f, 15f)
            outlinedTile(frame, 5f, y + 24f, 43f, 23f, 7f)
            drawCircle(Mint, 4f, Offset(27f, y + 36f))
        }
        Landmark.ANTENNA -> {
            outlinedTile(frame, -92f, -57f, 184f, 56f, 17f)
            mast(0f, 114f, frame, Mint)
            for (x in listOf(-60f, 60f)) {
                stripe(frame, x, -16f, x * .58f, -92f, 7f)
                drawCircle(Gold, 5f, Offset(x * .58f, -92f))
            }
            rotate(-27f, Offset(0f, -113f)) {
                drawArc(light, 0f, 180f, true, Offset(-45f, -140f), Size(90f, 56f))
                stripe(Snow, -39f, -112f, 39f, -112f, 3f)
                drawCircle(Mint, 5f, Offset(0f, -130f))
            }
        }
        Landmark.STORM -> {
            for (x in listOf(-107f, 104f)) mast(x, 107f, frame, Gold)
            val eye = Offset(0f, -83f)
            rotate(-13f, eye) {
                repeat(4) { i ->
                    val w = 242f - i * 42f
                    val h = 108f - i * 19f
                    drawOval(tint.mix(Coral, .55f).copy(alpha = .46f - .06f * i), Offset(-w / 2, eye.y - h / 2),
                        Size(w, h), style = Stroke(10f - i))
                }
                ellipse(light.copy(alpha = .50f), -25f, -94f, 50f, 22f)
            }
        }
        Landmark.WINDSOCK -> {
            mast(-29f, 119f, frame, Mint)
            val lift = sin(time * 4f) * 6
            shape(Mint.mix(tint, .36f), 2f) {
                moveTo(-25f, -116f); quadraticTo(20f, -123f, 69f, -107f + lift)
                lineTo(69f, -92f + lift); quadraticTo(15f, -98f, -25f, -89f); close()
            }
            stripe(Snow, -4f, -117f, -4f, -92f, 7f)
            stripe(Snow, 32f, -115f + lift * .4f, 32f, -96f + lift * .4f, 6f)
        }
        Landmark.WEATHER -> {
            outlinedTile(frame, -105f, -73f, 202f, 72f, 16f)
            outlinedTile(SkyBlue.mix(frame, .28f), -84f, -59f, 116f, 42f, 9f)
            for (i in 0..4) tile(Mint, -69f + i * 17f, -23f - (i + 2) * 4, 7f, (i + 2) * 4f, 2f)
            mast(73f, 117f, frame, Mint)
            rotor(73f, -119f, 24f, time * 130f, light)
        }
        Landmark.TURBINES -> for (i in -1..1) {
            val x = i * 83f
            val y = -119f - (1 - kotlin.math.abs(i)) * 43
            mast(x, -y, frame, Mint)
            rotor(x, y, if (i == 0) 47f else 36f, time * 92f + i * 39, light)
        }
        Landmark.RINGS -> {
            mast(-64f, 93f, frame, Gold)
            mast(64f, 93f, frame, Gold)
            rotate(-14f, Offset(0f, -93f)) {
                for (i in 0..2) drawOval(tint.copy(alpha = .67f - i * .14f),
                    Offset(-104f + i * 13f, -128f + i * 9f), Size(208f - i * 26f, 68f - i * 18f), style = Stroke(5f))
            }
        }
        Landmark.SORTER -> {
            outlinedTile(frame, -110f, -67f, 220f, 67f, 18f)
            for (i in -1..1) {
                val x = i * 60f
                outlinedTile(tint, x - 23, -64f - (1 - kotlin.math.abs(i)) * 13, 46f, 53f, 7f)
                sceneAt(x, -50f, .27f) { crystal(0f, 46f + (i + 1) * 14f, SkyBlue) }
            }
            stripe(light, -95f, -9f, 95f, -9f, 3f)
        }
        Landmark.WINDOW -> {
            outlinedTile(frame, -129f, -114f, 258f, 113f, 48f)
            outlinedTile(SkyBlue.mix(Panel, .38f), -114f, -101f, 228f, 87f, 35f)
            rotate(-16f, Offset(0f, -60f)) {
                for (i in 0..2) drawOval(tint.copy(alpha = .72f - i * .17f),
                    Offset(-98f + i * 12, -77f + i * 5), Size(196f - i * 24f, 34f - i * 10f), style = Stroke(4f))
            }
            twinkle(Snow, -66f, -73f, 6f)
            twinkle(Snow, 72f, -42f, 5f)
        }
    }
}

private fun DrawScope.mast(x: Float, height: Float, frame: Color, light: Color) {
    outlinedTile(frame, x - 7f, -height, 14f, height, 5f)
    outlinedTile(frame, x - 18f, -9f, 36f, 9f, 3f)
    drawCircle(light, 4f, Offset(x, -height + 3f))
}

private fun DrawScope.rotor(x: Float, y: Float, r: Float, angle: Float, tint: Color) {
    repeat(3) { i ->
        rotate(angle + i * 120f, Offset(x, y)) {
            shape(tint, 1.7f) {
                moveTo(x - 4, y); quadraticTo(x - 13, y - r * .62f, x - 4, y - r)
                quadraticTo(x + 6, y - r * .81f, x + 4, y); close()
            }
        }
    }
    drawCircle(Midnight, 8f, Offset(x, y))
    drawCircle(Mint, 5f, Offset(x, y))
}

private fun DrawScope.crystal(x: Float, height: Float, tint: Color) {
    shape(tint.mix(Panel, .19f), 2f) {
        moveTo(x - 17, 0f); lineTo(x - 15, -height * .78f); lineTo(x + 1, -height)
        lineTo(x + 19, -height * .73f); lineTo(x + 21, 0f); close()
    }
    shape(tint.mix(Snow, .55f)) {
        moveTo(x - 13, -height * .77f); lineTo(x + 1, -height + 4)
        lineTo(x + 7, -height * .66f); lineTo(x + 1, -4f); lineTo(x - 6, -5f); close()
    }
}
