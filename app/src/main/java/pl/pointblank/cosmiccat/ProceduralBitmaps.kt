package pl.pointblank.cosmiccat

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import kotlin.random.Random

internal object ProceduralBitmaps {
    private val dark = ink(0x252A49)
    private val cream = ink(0xFFF2D8)
    private val aqua = ink(0x88E8F5)
    private val whiteGlass = 0xA8FFFFFF.toInt()
    private val shadow = 0x38233259

    fun catBase(fur: PaintColor, face: Boolean = true) = BitmapFactoryGenerator.create {
        val color = ink(fur.rgb)
        val pale = shade(color, cream, .72f)
        oval(59f, 293f, 263f, 311f, shadow)
        stroke(dark, 29f) { moveTo(207f, 257f); cubicTo(291f, 266f, 294f, 211f, 275f, 191f) }
        stroke(color, 20f) { moveTo(207f, 257f); cubicTo(291f, 266f, 294f, 211f, 275f, 191f) }
        stroke(shade(color, dark, .25f), 20f) { moveTo(289f, 220f); quadTo(287f, 200f, 275f, 191f) }
        oval(67f, 34f, 253f, 214f, ink(0xB9F1FF), 6f, true)
        if (!face) return@create
        shape(color, 5f, true) {
            moveTo(93f, 101f); quadTo(72f, 39f, 79f, 13f); quadTo(83f, 3f, 96f, 13f)
            lineTo(136f, 51f); quadTo(160f, 43f, 184f, 51f); lineTo(224f, 13f)
            quadTo(237f, 3f, 241f, 13f); quadTo(248f, 39f, 227f, 101f)
            cubicTo(244f, 148f, 216f, 182f, 160f, 184f)
            cubicTo(104f, 182f, 76f, 148f, 93f, 101f); close()
        }
        for (side in listOf(-1, 1)) {
            val x = 160f + side * 63f
            shape(ink(0xF582A0)) {
                moveTo(x - side * 13f, 58f); lineTo(x + side * 13f, 30f)
                quadTo(x + side * 16f, 52f, x + side * 6f, 75f); close()
            }
        }
        oval(117f, 119f, 202f, 172f, pale, lit = true)
        for (x in listOf(124f, 196f)) {
            oval(x - 12, 95f, x + 12, 124f, dark)
            oval(x - 6, 98f, x + 1, 106f, Color.WHITE)
            oval(x + 2, 111f, x + 6, 116f, ink(0x77D9D3))
            val cheek = if (x < 160) 108f else 212f
            oval(cheek - 11, 127f, cheek + 11, 136f, ink(0xF593A1))
        }
        shape(ink(0xBC617F), 2f) {
            moveTo(150f, 130f); quadTo(160f, 124f, 170f, 130f); quadTo(160f, 146f, 150f, 130f); close()
        }
        stroke(dark, 3f) {
            moveTo(142f, 145f); quadTo(151f, 156f, 160f, 140f); quadTo(169f, 156f, 178f, 145f)
        }
        for (side in listOf(-1, 1)) {
            stroke(shade(color, dark, .54f), 2.4f) {
                moveTo(160f + side * 54, 141f); lineTo(160f + side * 76, 144f)
                moveTo(160f + side * 51, 149f); lineTo(160f + side * 70, 155f)
            }
        }
    }

    fun suit(paint: PaintColor) = BitmapFactoryGenerator.create {
        val color = ink(paint.rgb)
        round(103f, 249f, 153f, 299f, 18f, color, 5f, true)
        round(167f, 249f, 217f, 299f, 18f, color, 5f, true)
        oval(76f, 205f, 128f, 259f, color, 5f, true)
        oval(192f, 205f, 244f, 259f, color, 5f, true)
        round(106f, 184f, 214f, 277f, 32f, cream, 5f, true)
        round(123f, 212f, 197f, 253f, 12f, dark)
        round(129f, 218f, 169f, 246f, 8f, aqua, lit = true)
        oval(175f, 218f, 189f, 232f, ink(0xFA6F7C))
        oval(176f, 236f, 188f, 246f, ink(0xFFD574))
        shape(color, 5f, true) {
            moveTo(77f, 164f); quadTo(160f, 205f, 243f, 164f)
            lineTo(232f, 194f); quadTo(160f, 232f, 88f, 194f); close()
        }
        stroke(whiteGlass, 6f) { moveTo(78f, 116f); quadTo(78f, 141f, 94f, 154f) }
        stroke(whiteGlass, 5f) { moveTo(233f, 79f); quadTo(244f, 97f, 241f, 114f) }
        for (x in listOf(112f, 176f)) round(x, 279f, x + 30, 285f, 3f, whiteGlass)
    }

    fun commander() = BitmapFactoryGenerator.create {
        val full = RectF(0f, 0f, 320f, 320f)
        image(catBase(PaintColor.GRAY), full)
        val marking = ink(0x607197)
        stroke(marking, 5f) {
            moveTo(148f, 56f); quadTo(151f, 65f, 151f, 72f)
            moveTo(160f, 54f); quadTo(161f, 67f, 161f, 79f)
            moveTo(173f, 56f); quadTo(171f, 65f, 171f, 71f)
        }
        image(suit(PaintColor.RED), full)
        oval(129f, 216f, 164f, 250f, ink(0x395681), 2.5f)
        shape(ink(0xFFD574)) {
            moveTo(146.5f, 219f); lineTo(150.6f, 229f); lineTo(161f, 233f)
            lineTo(150.6f, 237f); lineTo(146.5f, 247f); lineTo(142.4f, 237f)
            lineTo(132f, 233f); lineTo(142.4f, 229f); close()
        }
        round(109f, 265f, 144f, 272f, 3f, ink(0xFFD574), 1.8f)
        round(176f, 265f, 211f, 272f, 3f, ink(0xFFD574), 1.8f)
    }

    fun hair(style: Hair, paint: PaintColor) = BitmapFactoryGenerator.create {
        val color = ink(paint.rgb)
        val light = shade(color, Color.WHITE, .37f)
        for (side in listOf(-1, 1)) {
            val x = 160f + side * 92f
            val end = if (style == Hair.TUFTS) 184f else 231f
            shape(color, 4f, true) {
                moveTo(x, 108f)
                cubicTo(x + side * 38, 124f, x - side * 7, end - 30, x + side * 26, end)
                cubicTo(x - side * 19, end + 2, x - side * 19, 151f, x, 108f); close()
            }
            if (style == Hair.BRAIDS) {
                repeat(5) { index ->
                    val y = 139f + index * 15
                    oval(x - 8, y, x + 9, y + 20, color, 2f, true)
                }
                round(x - 10, 206f, x + 12, 215f, 4f, ink(0xFFD574), 2f)
            } else stroke(light, 4f) { moveTo(x + side * 5, 137f); quadTo(x - side * 3, 157f, x + side * 7, end - 15) }
        }
    }

    fun rocket(vehicle: Vehicle) = BitmapFactoryGenerator.create {
        val color = ink(vehicle.paint.rgb)
        flame()
        when (vehicle.fins) {
            Fins.WIDE, Fins.SWEPT -> for (side in listOf(-1, 1)) {
                val end = if (vehicle.fins == Fins.WIDE) 98f else 112f
                shape(ink(0xED6780), 5f, true) {
                    moveTo(160f + side * 35, 152f)
                    quadTo(160f + side * end, 169f, 160f + side * end, 244f)
                    lineTo(160f + side * 32, 216f); close()
                }
            }
            Fins.PODS -> for (x in listOf(75f, 245f)) {
                round(x - 19, 146f, x + 19, 235f, 17f, ink(0xED6780), 5f, true)
                oval(x - 11, 181f, x + 11, 202f, cream, 2f)
            }
        }
        round(123f, 211f, 197f, 249f, 12f, dark)
        shape(color, 6f, true) {
            moveTo(105f, 216f)
            cubicTo(90f, 143f, 105f, 83f, 126f, 64f)
            lineTo(194f, 64f); cubicTo(215f, 83f, 230f, 143f, 215f, 216f)
            quadTo(160f, 239f, 105f, 216f); close()
        }
        shape(ink(0xF17A88), 5f, true) {
            moveTo(109f, 86f)
            when (vehicle.nose) {
                Nose.ROUND -> { quadTo(130f, 21f, 160f, 22f); quadTo(190f, 21f, 211f, 86f) }
                Nose.POINTED -> { lineTo(160f, 9f); lineTo(211f, 86f) }
                Nose.DOUBLE -> { quadTo(135f, 13f, 160f, 56f); quadTo(185f, 13f, 211f, 86f) }
            }
            quadTo(160f, 103f, 109f, 86f); close()
        }
        round(134f, 167f, 186f, 213f, 12f, ink(0x8FE8A3), 4f, true)
        round(161f, 188f, 178f, 194f, 3f, dark)
    }

    fun BitmapFactoryGenerator.flame() {
        shape(ink(0xFB8057), 4f, true) {
            moveTo(132f, 237f); quadTo(122f, 270f, 160f, 315f)
            quadTo(198f, 270f, 188f, 237f); close()
        }
        shape(ink(0xFFDB70), lit = true) {
            moveTo(145f, 247f); quadTo(140f, 272f, 160f, 295f)
            quadTo(180f, 272f, 175f, 247f); close()
        }
    }

    fun BitmapFactoryGenerator.window(cat: Bitmap, x: Float, y: Float, radius: Float) {
        oval(x - radius - 4, y - radius - 4, x + radius + 4, y + radius + 4, cream, 4f, true)
        oval(x - radius + 3, y - radius + 3, x + radius - 3, y + radius - 3, aqua, 2f, true)
        clipOval(x - radius + 4, y - radius + 4, x + radius - 4, y + radius - 4) {
            val width = radius * 3.2f
            image(cat, RectF(x - width / 2, y - radius - 5, x + width / 2, y - radius - 5 + width))
        }
    }

    fun rover(vehicle: Vehicle) = BitmapFactoryGenerator.create {
        oval(22f, 236f, 297f, 262f, shadow)
        val tire = ink(0x323B59)
        round(48f, 140f, 270f, 220f, 28f, ink(vehicle.paint.rgb), 6f, true)
        shape(ink(0xB6EEFF), 5f, true) {
            moveTo(122f, 143f); lineTo(146f, 102f); quadTo(185f, 68f, 228f, 104f)
            lineTo(252f, 144f); close()
        }
        stroke(dark, 6f) { moveTo(91f, 140f); lineTo(77f, 91f) }
        oval(67f, 80f, 86f, 100f, ink(0xF87998), 4f, true)
        round(236f, 158f, 269f, 178f, 8f, ink(0xFFF08D), 3f, true)
        for (x in listOf(95f, 228f)) {
            val radius = if (vehicle.wheels == Wheels.GRIP) 43 else 36
            oval(x - radius, 219f - radius, x + radius, 219f + radius, tire, 5f, true)
            val hub = if (vehicle.wheels == Wheels.MOON) ink(0xB7ACFF) else ink(0x98DFE4)
            oval(x - 20, 199f, x + 20, 239f, hub, 4f, true)
            oval(x - 6, 213f, x + 6, 225f, dark)
            if (vehicle.wheels == Wheels.GRIP) for (dx in listOf(-26f, 0f, 26f)) {
                round(x + dx - 6, 184f, x + dx + 6, 194f, 3f, cream)
            }
        }
    }

    fun planet(destination: Destination) = BitmapFactoryGenerator.create {
        val base = ink(destination.color)
        if (destination == Destination.SATURN) {
            stroke(ink(0xE0A875), 19f) { moveTo(20f, 200f); cubicTo(11f, 131f, 287f, 87f, 299f, 117f) }
        }
        oval(53f, 47f, 267f, 261f, base, 5f, true)
        clipOval(55f, 49f, 265f, 259f) {
            if (destination.cloudStation || destination == Destination.VENUS) {
                repeat(5) {
                    val y = 84f + it * 31
                    val tint = if (it % 2 == 0) Color.WHITE else dark
                    stroke(shade(base, tint, .27f), 16f + it % 2 * 9) {
                        moveTo(44f, y - 13); cubicTo(120f, y + 22, 226f, y + 26, 282f, y - 9)
                    }
                }
                if (destination == Destination.JUPITER) oval(171f, 169f, 233f, 195f, ink(0xCA675C), lit = true)
            } else {
                val random = Random(47 + destination.ordinal)
                repeat(15) {
                    val x = random.nextInt(72, 244).toFloat()
                    val y = random.nextInt(56, 253).toFloat()
                    val r = random.nextInt(7, 36).toFloat()
                    oval(x - r, y - r * .65f, x + r, y + r * .65f, shade(base, dark, .27f), lit = true)
                }
            }
            stroke(0x64FFFFFF, 6f) { moveTo(80f, 104f); quadTo(104f, 70f, 142f, 66f) }
        }
        if (destination == Destination.SATURN) {
            stroke(ink(0xAA7967), 23f) { moveTo(20f, 200f); cubicTo(37f, 243f, 311f, 159f, 299f, 117f) }
            stroke(ink(0xFFE1AA), 12f) { moveTo(20f, 198f); cubicTo(37f, 241f, 311f, 157f, 299f, 116f) }
        }
    }

    fun alien(alien: Alien) = BitmapFactoryGenerator.create {
        val color = ink(alien.color.rgb)
        oval(60f, 268f, 264f, 293f, shadow)
        if (alien.ordinal % 2 == 0) {
            for (x in listOf(106f, 214f)) {
                stroke(dark, 9f) { moveTo(x, 109f); lineTo(x + (x - 160f) * .55f, 63f) }
                val tip = x + (x - 160f) * .55f
                oval(tip - 14, 46f, tip + 14, 75f, ink(0xFFD273), 4f, true)
            }
        } else {
            shape(color, 5f, true) { moveTo(71f, 156f); lineTo(51f, 68f); quadTo(105f, 76f, 116f, 122f); close() }
            shape(color, 5f, true) { moveTo(249f, 156f); lineTo(269f, 68f); quadTo(215f, 76f, 204f, 122f); close() }
        }
        oval(55f, 188f, 111f, 245f, color, 4f, true)
        oval(209f, 174f, 266f, 231f, color, 4f, true)
        for (x in listOf(107f, 213f)) oval(x - 24, 244f, x + 24, 281f, color, 4f, true)
        oval(76f, 88f, 244f, 267f, color, 6f, true)
        val eyes = if (alien == Alien.KROPKA) listOf(113f, 160f, 207f) else listOf(125f, 195f)
        for (x in eyes) {
            oval(x - 21, 132f, x + 21, 184f, cream, 3f)
            oval(x - 10, 144f, x + 12, 176f, dark)
            oval(x - 6, 148f, x + 2, 157f, Color.WHITE)
        }
        oval(132f, 192f, 188f, 227f, dark)
        oval(148f, 211f, 172f, 226f, ink(0xF788A1))
        for (x in listOf(103f, 217f)) oval(x - 12, 191f, x + 12, 203f, ink(0xF69BA7))
    }

    fun discovery(item: Discovery) = BitmapFactoryGenerator.create {
        oval(41f, 253f, 280f, 284f, shadow)
        when (item) {
            Discovery.METEOR -> {
                shape(ink(0xFFB63A), 7f, true) {
                    moveTo(54f, 210f); lineTo(76f, 135f); lineTo(127f, 79f); lineTo(202f, 65f)
                    lineTo(253f, 117f); lineTo(268f, 198f); lineTo(234f, 248f); lineTo(111f, 266f); close()
                }
                shape(ink(0xFFE37A), lit = true) { moveTo(76f, 135f); lineTo(202f, 65f); lineTo(170f, 169f); close() }
                shape(ink(0xEB8E32), lit = true) { moveTo(170f, 169f); lineTo(253f, 117f); lineTo(234f, 248f); close() }
                oval(107f, 180f, 143f, 208f, ink(0xB57442), lit = true)
                oval(201f, 127f, 228f, 154f, ink(0xD9913A), lit = true)
                oval(35f, 244f, 68f, 267f, ink(0xC88458), 3f, true)
                oval(251f, 237f, 280f, 259f, ink(0xC88458), 3f, true)
            }
            Discovery.ICE, Discovery.RING -> {
                for (x in listOf(99f, 158f, 219f)) {
                    val high = if (x == 158f) 49f else 121f
                    shape(aqua, 5f, true) {
                        moveTo(x - 30, 244f); lineTo(x - 42, high + 48); lineTo(x, high)
                        lineTo(x + 39, high + 38); lineTo(x + 29, 242f); close()
                    }
                    stroke(whiteGlass, 6f) { moveTo(x - 10, high + 42); lineTo(x + 3, 223f) }
                }
            }
            Discovery.TELESCOPE, Discovery.WIND -> {
                stroke(dark, 15f) { moveTo(97f, 266f); lineTo(161f, 173f); lineTo(228f, 266f) }
                if (item == Discovery.TELESCOPE) {
                    shape(ink(0xAD96FA), 6f, true) {
                        moveTo(62f, 169f); lineTo(218f, 79f); lineTo(251f, 141f); lineTo(82f, 205f); close()
                    }
                    oval(199f, 70f, 267f, 147f, aqua, 6f, true)
                    oval(220f, 83f, 250f, 130f, ink(0x699BEE), 4f, true)
                } else {
                    stroke(dark, 12f) { moveTo(161f, 187f); lineTo(161f, 63f) }
                    stroke(aqua, 15f) { moveTo(75f, 113f); lineTo(248f, 113f) }
                    for (x in listOf(77f, 247f)) oval(x - 24, 90f, x + 24, 133f, cream, 5f, true)
                    oval(143f, 95f, 180f, 132f, ink(0xFA7894), 5f, true)
                }
            }
            Discovery.FRIEND -> image(alien(Alien.KROPKA), RectF(0f, 0f, 320f, 320f))
        }
    }
}
