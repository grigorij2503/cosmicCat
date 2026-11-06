package pl.pointblank.cosmiccat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
internal fun PortSkyline(state: Progress, modifier: Modifier = Modifier) {
    val frame = RoundedCornerShape(18.dp)
    Canvas(modifier.clip(frame).background(Midnight).border(1.dp, Rim, frame)) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Midnight, Color(0xFF354368))))
        val earth = Offset(w * .84f, h * .24f)
        val r = h * .17f
        drawCircle(Brush.radialGradient(listOf(SkyBlue.copy(alpha = .27f), Color.Transparent), earth, r * 1.9f),
            r * 1.9f, earth)
        drawCircle(Brush.radialGradient(listOf(SkyBlue, Color(0xFF496CB7)), earth - Offset(r * .32f, r * .38f), r * 1.6f), r, earth)
        ellipse(Mint.copy(alpha = .66f), earth.x - r * .69f, earth.y - r * .51f, r * .78f, r * .39f)
        ellipse(Mint.copy(alpha = .55f), earth.x + r * .14f, earth.y - r * .06f, r * .57f, r * .46f)
        repeat(26) { i ->
            val x = w * ((i * 37 + 4) % 103) / 103
            val y = h * ((i * 19 + 5) % 47) / 100
            if ((Offset(x, y) - earth).getDistanceSquared() < r * r * 1.3f) return@repeat
            if (i % 7 == 0) twinkle(Lavender.copy(alpha = .73f), x, y, h * .032f)
            else drawCircle(SkyBlue.copy(alpha = .49f), h * .007f, Offset(x, y))
        }
        ellipse(Color(0xFF4E5B83), -w * .19f, h * .58f, w * 1.38f, h * .66f)
        ellipse(Color(0xFF374669), -w * .20f, h * .73f, w * 1.39f, h * .49f)
        ellipse(Midnight.copy(alpha = .43f), w * .08f, h * .89f, w * .84f, h * .12f)
        val unit = minOf(w / 543f, h / 176f)
        Building.entries.forEachIndexed { i, building ->
            val x = w * (.12f + i * .19f)
            sceneAt(x, h * (.78f + (i % 2) * .078f), unit) {
                val level = state.portLevel(building)
                if (level > 0) module(building, level)
                else {
                    ellipse(Panel, -44f, -6f, 88f, 14f)
                    drawOval(Mist.copy(alpha = .40f), Offset(-39f, -7f), Size(78f, 13f), style = Stroke(1.5f))
                    for (side in listOf(-1, 1)) drawCircle(Mist.copy(alpha = .7f), 2.5f, Offset(side * 29f, 0f))
                }
            }
        }
    }
}

@Composable
internal fun BuildingArt(building: Building, level: Int, modifier: Modifier) {
    Canvas(modifier) {
        val scale = minOf(size.width / 110, size.height / 104)
        sceneAt(size.width / 2, size.height * .88f, scale) { module(building, level) }
    }
}

private fun DrawScope.module(building: Building, level: Int) {
    val tint = building.color.color()
    val base = if (level == 0) tint.mix(Panel, .36f) else tint
    val shade = base.mix(Panel, .61f)
    ellipse(Midnight.copy(alpha = .66f), -51f, -4f, 102f, 13f)
    outlinedTile(shade, -47f, -13f, 94f, 16f, 6f)
    when (building) {
        Building.LAB -> {
            outlinedTile(shade, -40f, -54f, 80f, 43f, 9f)
            outlinedTile(base, -26f, -73f, 52f, 63f, 13f)
            portWindow(0f, -52f, 13f, base)
            outlinedTile(shade, -9f, -29f, 18f, 20f, 5f)
            stripe(base, 29f, -53f, 29f, -78f, 3f)
            drawCircle(Mint, 3f, Offset(29f, -79f))
            for (i in 0..2) stripe(base, -36f, -24f - i * 6, -29f, -24f - i * 6, 2f)
        }
        Building.HANGAR -> {
            outlinedTile(base, -43f, -55f, 86f, 46f, 13f)
            shape(base.mix(Snow, .34f), 2.4f) {
                moveTo(-47f, -53f); quadraticTo(-31f, -74f, 0f, -74f)
                quadraticTo(31f, -74f, 47f, -53f); close()
            }
            outlinedTile(shade, -24f, -45f, 48f, 36f, 5f)
            repeat(4) { i -> stripe(base.copy(alpha = .68f), -20f, -39f + i * 7f, 20f, -39f + i * 7f, 1.5f) }
            for (side in listOf(-1, 1)) drawCircle(Gold, 3f, Offset(side * 33f, -29f))
        }
        Building.GREENHOUSE -> {
            val glass = base.mix(Mint, .44f)
            val dome = Offset(-43f, -78f)
            val domeSize = Size(86f, 124f)
            drawArc(Brush.verticalGradient(listOf(glass.mix(Snow, .55f), glass.mix(Panel, .54f)), -78f, -15f),
                180f, 180f, true, dome, domeSize)
            drawArc(Midnight, 180f, 180f, false, dome, domeSize, style = Stroke(2.4f))
            for (side in listOf(-1, 1)) {
                val x = side * 23f
                stripe(shade, x, -62f, x * 1.32f, -17f, 2f)
                plant(x, -17f, .33f, glass, PlantStage.BLOOM, side + 1, withPot = false)
            }
            stripe(Snow.copy(alpha = .71f), -15f, -70f, -24f, -65f, 3f)
            outlinedTile(shade, -45f, -19f, 90f, 10f, 4f)
        }
        Building.HOMES -> {
            habitat(-23f, -9f, .70f, base.mix(Lavender, .19f))
            habitat(24f, -9f, .60f, base.mix(Gold, .30f))
            stripe(base, 1f, -14f, 9f, -14f, 3f)
        }
        Building.TOWER -> {
            outlinedTile(shade, -26f, -40f, 52f, 30f, 8f)
            outlinedTile(base, -13f, -72f, 26f, 55f, 9f)
            rotate(-31f, Offset(0f, -67f)) {
                drawArc(base.mix(Snow, .42f), 0f, 180f, true, Offset(-29f, -89f), Size(58f, 39f))
                stripe(Midnight, -29f, -69f, 29f, -69f, 2.2f)
                stripe(Snow, 0f, -66f, 0f, -86f, 3f)
                drawCircle(Mint, 4f, Offset(0f, -87f))
            }
            portWindow(0f, -30f, 7f, base)
        }
    }
    repeat(level) { i -> drawCircle(Gold, 2.1f, Offset((i - (level - 1) * .5f) * 10f, -4f)) }
}

@Composable
internal fun HabitatArt(tint: Color, modifier: Modifier) {
    Canvas(modifier) {
        val scale = minOf(size.width / 110, size.height / 104)
        sceneAt(size.width / 2, size.height * .82f, scale) {
            twinkle(Snow.copy(alpha = .6f), -38f, -62f, 5f)
            twinkle(tint, 40f, -72f, 4f)
            ellipse(Midnight.copy(alpha = .7f), -48f, -4f, 96f, 17f)
            habitat(0f, 0f, 1.04f, tint)
        }
    }
}

private fun DrawScope.habitat(x: Float, y: Float, unit: Float, tint: Color) = sceneAt(x, y, unit) {
    val wall = tint.mix(Panel, .38f)
    outlinedTile(wall, -36f, -46f, 72f, 47f, 13f)
    shape(tint.mix(Snow, .20f), 2.6f) {
        moveTo(-42f, -40f); quadraticTo(-29f, -48f, -16f, -66f)
        quadraticTo(0f, -81f, 16f, -66f); quadraticTo(28f, -49f, 42f, -40f)
        quadraticTo(0f, -35f, -42f, -40f); close()
    }
    portWindow(16f, -24f, 9f, tint)
    outlinedTile(Panel, -21f, -28f, 17f, 29f, 6f)
    drawCircle(Gold, 2f, Offset(-10f, -13f))
}

private fun DrawScope.portWindow(x: Float, y: Float, r: Float, tint: Color) {
    drawCircle(Midnight, r + 2f, Offset(x, y))
    drawCircle(Snow, r, Offset(x, y))
    drawCircle(Brush.radialGradient(listOf(SkyBlue, tint.mix(Panel, .61f)), Offset(x - 2, y - 3), r * 1.7f),
        r - 2, Offset(x, y))
    drawCircle(Snow.copy(alpha = .85f), r * .19f, Offset(x - r * .29f, y - r * .24f))
}

@Composable
internal fun GardenScene(state: Progress, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    val plants = state.plants.filterValues { it != PlantStage.BLOOM }.entries.sortedBy { it.key.ordinal }
    val beds = state.portLevel(Building.GREENHOUSE)
    Canvas(modifier.clip(shape).border(1.dp, Mint.copy(alpha = .43f), shape)) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF122944), Color(0xFF3C6B78))))
        val glass = Mint.copy(alpha = .32f)
        drawArc(glass, 180f, 180f, false, Offset(w * .07f, h * .12f), Size(w * .86f, h * .95f), style = Stroke(h * .025f))
        for (i in 1..3) {
            val x = w * (.15f + i * .175f)
            stripe(glass, x, h * if (i == 2) .14f else .22f, x, h * .69f, h * .016f)
        }
        stripe(glass, w * .12f, h * .49f, w * .88f, h * .49f, h * .018f)
        ellipse(Lavender.copy(alpha = .71f), w * .64f, h * .25f, h * .18f, h * .18f)
        twinkle(Mint, w * .24f, h * .34f, h * .041f)
        twinkle(Snow.copy(alpha = .65f), w * .76f, h * .41f, h * .027f)
        drawRect(Brush.verticalGradient(listOf(Panel, Midnight), h * .66f, h), Offset(0f, h * .66f), Size(w, h * .34f))
        stripe(Mint.copy(alpha = .4f), 0f, h * .67f, w, h * .67f, h * .023f)
        val unit = minOf(w / 295f, h / 116f)
        repeat(beds) { i ->
            val x = w * (.5f + (i - (beds - 1) / 2f) * .265f)
            sceneAt(x, h * .80f, unit) {
                ellipse(Midnight.copy(alpha = .72f), -37f, -1f, 74f, 17f)
                outlinedTile(Color(0xFF619C9C), -32f, -9f, 64f, 18f, 7f)
                tile(Color(0xFF24394C), -26f, -7f, 52f, 8f, 4f)
                val entry = plants.getOrNull(i)
                if (entry != null) plant(0f, -6f, .79f, flowerColor(entry.key), entry.value, entry.key.ordinal, withPot = false)
                else stripe(Mist.copy(alpha = .65f), -7f, -3f, 7f, -3f, 1.8f)
            }
        }
    }
}

@Composable
internal fun PlantArt(destination: Destination, stage: PlantStage?, modifier: Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 95f
        plant(size.width / 2, size.height * .91f, s, flowerColor(destination), stage, destination.ordinal)
    }
}

private fun flowerColor(destination: Destination): Color = when (destination) {
    Destination.MARS -> Gold
    Destination.PLUTO -> Lavender
    Destination.VENUS -> Color(0xFFFB9861)
    Destination.JUPITER -> Mint
    Destination.NEPTUNE -> SkyBlue
    Destination.SATURN -> Coral
    Destination.MOON -> Snow
}

private fun DrawScope.plant(
    x: Float, y: Float, scale: Float, tint: Color, stage: PlantStage?, variant: Int, withPot: Boolean = true,
) = sceneAt(x, y, scale) {
    if (stage == null) {
        rotate(-33f, Offset(0f, -38f)) {
            val at = Offset(-22f, -56f)
            val s = Size(44f, 35f)
            drawOval(Brush.linearGradient(listOf(tint, tint.mix(Panel, .40f)), at, at + Offset(43f, 35f)), at, s)
            drawOval(Midnight, at, s, style = Stroke(2.6f))
            stripe(Snow.copy(alpha = .61f), -12f, -44f, 11f, -46f, 2.6f)
        }
        twinkle(tint, 26f, -62f, 6f)
    } else {
        val stemEnd = if (stage == PlantStage.SPROUT) -40f else -61f
        val ground = if (withPot) -20f else 0f
        stripe(Midnight, 0f, ground, 0f, stemEnd, 8f)
        stripe(Mint.mix(Panel, .24f), 0f, ground, 0f, stemEnd, 5f)
        for (side in listOf(-1, 1)) shape(if (side < 0) Mint else Mint.mix(SkyBlue, .42f), 1.7f) {
            val leafY = ground - 11f - if (side < 0) 0f else 8f
            moveTo(0f, leafY + 6f); quadraticTo(side * 22f, leafY + 7, side * 28f, leafY - 13)
            quadraticTo(side * 5f, leafY - 16f, 0f, leafY + 6); close()
        }
        if (stage == PlantStage.BLOOM) {
            val petals = when (variant) { 1, 3 -> 7; 2, 5 -> 5; else -> 6 }
            repeat(petals) { i ->
                rotate(i * (360f / petals), Offset(0f, stemEnd)) {
                    drawOval(tint.mix(Snow, if (i % 2 == 0) .31f else .09f), Offset(-9f, stemEnd - 26f), Size(18f, 22f))
                    drawOval(Midnight, Offset(-9f, stemEnd - 26f), Size(18f, 22f), style = Stroke(1.6f))
                }
            }
            drawCircle(Midnight, 9.5f, Offset(0f, stemEnd))
            drawCircle(if (tint == Gold) Color(0xFFF29C61) else Gold, 7.5f, Offset(0f, stemEnd))
            drawCircle(Snow, 2f, Offset(-2f, stemEnd - 2f))
        } else if (stage == PlantStage.BUD) {
            shape(tint, 2f) {
                moveTo(0f, stemEnd + 8f); cubicTo(-19f, stemEnd + 3f, -15f, stemEnd - 9f, 0f, stemEnd - 22f)
                cubicTo(15f, stemEnd - 9f, 19f, stemEnd + 3f, 0f, stemEnd + 8f); close()
            }
            stripe(Snow.copy(alpha = .7f), -5f, stemEnd - 6f, -1f, stemEnd - 14f, 2f)
        }
        if (withPot) {
            shape(tint.mix(Panel, .28f), 2.3f) {
                moveTo(-22f, -22f); lineTo(-16f, -1f); quadraticTo(0f, 5f, 16f, -1f)
                lineTo(22f, -22f); close()
            }
            outlinedTile(tint, -25f, -25f, 50f, 9f, 4f)
        }
    }
}

@Composable
internal fun StickerArt(destination: Destination, modifier: Modifier, collected: Boolean = true) {
    Canvas(modifier) {
        val scale = size.minDimension / 100f
        val tint = colorOf(destination.color)
        sceneAt(size.width / 2f, size.height / 2f, scale) {
            if (!collected) {
                drawCircle(Mist.copy(alpha = .09f), 39f, Offset.Zero)
                drawCircle(Mist.copy(alpha = .32f), 37f, Offset.Zero, style = Stroke(2f))
                twinkle(Mist.copy(alpha = .54f), 0f, 0f, 15f)
                return@sceneAt
            }
            drawCircle(Snow, 43f, Offset.Zero)
            drawCircle(Midnight, 38.5f, Offset.Zero)
            drawCircle(Brush.radialGradient(listOf(tint.mix(Panel, .31f), Panel), Offset(-13f, -15f), 53f), 36f, Offset.Zero)
            val ink = tint.mix(Snow, .32f)
            twinkle(Snow, 22f, -21f, 5f)
            twinkle(Snow, -22f, 19f, 4f)
            when (destination) {
                Destination.MARS -> {
                    shape(Gold, 2.5f) {
                        moveTo(-25f, -11f); lineTo(-11f, -3f); lineTo(0f, -22f); lineTo(11f, -3f)
                        lineTo(25f, -11f); lineTo(18f, 18f); lineTo(-18f, 18f); close()
                    }
                    drawCircle(Coral, 4.4f, Offset(0f, 7f))
                    stripe(Snow, -12f, 13f, 12f, 13f)
                }
                Destination.PLUTO -> {
                    shape(ink, 2f) {
                        moveTo(-28f, 22f); quadraticTo(-8f, -8f, 13f, -14f)
                        lineTo(22f, 0f); quadraticTo(9f, 14f, -28f, 22f); close()
                    }
                    drawCircle(Midnight, 12f, Offset(14f, -9f))
                    drawCircle(SkyBlue, 9f, Offset(14f, -9f))
                    twinkle(Snow, 12f, -11f, 5f)
                }
                Destination.VENUS -> {
                    rotate(30f, Offset.Zero) {
                        outlinedTile(Gold, -6f, 1f, 12f, 25f, 5f)
                        drawCircle(Midnight, 20f, Offset(0f, -6f))
                        drawCircle(Gold, 18f, Offset(0f, -6f))
                        drawCircle(SkyBlue, 13f, Offset(0f, -6f))
                        stripe(Snow, -7f, -3f, 2f, -13f, 3f)
                    }
                }
                Destination.JUPITER -> {
                    outlinedTile(ink, -22f, -12f, 44f, 31f, 11f)
                    stripe(ink, 0f, -11f, 0f, -21f, 3f)
                    drawCircle(Mint, 4f, Offset(0f, -22f))
                    for (x in listOf(-9f, 9f)) drawCircle(Midnight, 4f, Offset(x, 0f))
                    stripe(Panel, -5f, 9f, 5f, 9f, 2.4f)
                }
                Destination.NEPTUNE -> {
                    stripe(Gold, -20f, -16f, -20f, 19f, 4f)
                    shape(Mint, 2f) {
                        moveTo(-19f, -18f); quadraticTo(5f, -25f, 25f, -11f)
                        quadraticTo(15f, -4f, 24f, 9f); quadraticTo(3f, -3f, -19f, 2f); close()
                    }
                    stripe(Snow, -5f, -17f, -5f, -1f, 3f)
                }
                Destination.SATURN -> {
                    drawCircle(ink, 15f, Offset.Zero)
                    rotate(-22f, Offset.Zero) {
                        drawOval(Midnight, Offset(-29f, -9f), Size(58f, 18f), style = Stroke(6f))
                        drawOval(Snow, Offset(-29f, -9f), Size(58f, 18f), style = Stroke(3f))
                    }
                }
                Destination.MOON -> twinkle(Snow, 0f, 0f, 23f)
            }
        }
    }
}

@Composable
internal fun PortraitStage(tint: Color, modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(15.dp)
    Box(modifier.clip(shape).background(Panel).border(1.dp, tint.copy(alpha = .44f), shape), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height * .5f)
            val r = size.minDimension * .49f
            drawCircle(Brush.radialGradient(listOf(tint.copy(alpha = .31f), Color.Transparent), center, r), r, center)
            drawCircle(tint.copy(alpha = .28f), r * .8f, center, style = Stroke(1.dp.toPx()))
            twinkle(tint, size.width * .19f, size.height * .23f, size.minDimension * .05f)
        }
        content()
    }
}
