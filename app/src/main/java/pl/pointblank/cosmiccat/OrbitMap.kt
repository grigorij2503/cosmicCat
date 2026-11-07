package pl.pointblank.cosmiccat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal class OrbitView(
    angle: Float = 0f,
    inclination: Float = .65f,
    time: Float = 0f,
) {
    var rotation by mutableFloatStateOf(angle)
    var tilt by mutableFloatStateOf(inclination)
    var elapsed by mutableFloatStateOf(time)

    companion object {
        val Saver = listSaver<OrbitView, Float>(
            save = { listOf(it.rotation, it.tilt, it.elapsed) },
            restore = { OrbitView(it[0], it[1], it[2]) },
        )
    }
}

internal enum class OrbitBody(
    val destination: Destination?, val radius: Float, val phase: Float, val speed: Float, val diameter: Float,
) {
    MERCURY(null, .18f, -2.6f, 1.18f, 13f),
    VENUS(Destination.VENUS, .27f, -.95f, .88f, 24f),
    EARTH(null, .38f, -2.38f, .72f, 21f),
    MOON(Destination.MOON, 0f, 3.1f, 1.6f, 18f),
    MARS(Destination.MARS, .49f, .40f, .60f, 26f),
    JUPITER(Destination.JUPITER, .62f, 2.77f, .41f, 40f),
    SATURN(Destination.SATURN, .74f, .94f, .34f, 42f),
    URANUS(null, .85f, -1.34f, .27f, 22f),
    NEPTUNE(Destination.NEPTUNE, .95f, -3.03f, .21f, 28f),
    PLUTO(Destination.PLUTO, 1f, -.46f, .17f, 20f),
}

internal class OrbitProjection(size: Size, private val density: Float, view: OrbitView) {
    val sun = Offset(size.width / 2, size.height * .51f)
    val tilt = view.tilt
    val extent = minOf(size.width * .43f, (size.height - 66f * density).coerceAtLeast(0f) / (2 * tilt))
    private val rotation = view.rotation * (PI.toFloat() / 180f)
    private val elapsed = view.elapsed

    private fun position(body: OrbitBody): Offset {
        val angle = body.phase + rotation + elapsed * body.speed
        return if (body == OrbitBody.MOON) {
            position(OrbitBody.EARTH) + Offset(cos(angle) * 24f * density, sin(angle) * 19f * density)
        } else {
            val r = extent * body.radius
            sun + Offset(cos(angle) * r, sin(angle) * r * tilt)
        }
    }

    val positions = OrbitBody.entries.associateWith(::position)

    fun destinationAt(touch: Offset): Destination? {
        val closest = positions.minBy { (_, position) -> (position - touch).getDistanceSquared() }
        return closest.key.destination?.takeIf { (closest.value - touch).getDistance() <= 24f * density }
    }

    fun positionOf(destination: Destination) = positions.getValue(landingBodies.getValue(destination))
}

private val landingBodies = OrbitBody.entries.mapNotNull { body -> body.destination?.let { it to body } }.toMap()
internal val orbitalLandingSites = landingBodies.keys.toList()

internal class OrbitFlight(val start: Offset, val end: Offset, rise: Float) {
    val control = Offset(
        (start.x + end.x) / 2 + if (start == end) rise / 2 else 0f,
        (minOf(start.y, end.y) - rise).coerceAtLeast(rise / 2),
    )

    fun point(t: Float): Offset {
        val u = 1f - t
        return start * (u * u) + control * (2f * u * t) + end * (t * t)
    }

    fun angle(t: Float): Float {
        val direction = (control - start) * (1f - t) + (end - control) * t
        return 90f + atan2(direction.y, direction.x) * (180f / PI.toFloat())
    }
}

@Composable
internal fun OrbitMap(
    view: OrbitView,
    state: Progress,
    assets: AssetManager,
    highlighted: Destination?,
    flyingTo: Destination? = null,
    flightProgress: () -> Float = { 0f },
    onMapTouch: () -> Unit = {},
    choose: ((Destination) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    val images = remember(assets) { Destination.entries.associateWith(assets::planet) }
    val rocket = if (flyingTo != null) remember(assets, state.vehicle, state.cat) { assets.rocket(state.vehicle, state.cat) } else null
    val measurer = rememberTextMeasurer(cacheSize = 24)
    val onTouch by rememberUpdatedState(onMapTouch)
    val onChoose by rememberUpdatedState(choose)
    val interactive = choose != null
    Box(Modifier.fillMaxWidth().height(284.dp).clip(shape).background(OrbitNight).border(1.dp, Rim, shape)) {
        val gesture = if (interactive) Modifier.pointerInput(view) {
            awaitEachGesture {
                val down = awaitFirstDown()
                onTouch()
                val initialRotation = view.rotation
                val initialTilt = view.tilt
                var dragging = false
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (change.isConsumed) break
                    if (!change.pressed) {
                        if (!dragging) {
                            val projection = OrbitProjection(Size(size.width.toFloat(), size.height.toFloat()), density, view)
                            projection.destinationAt(change.position)?.let { onChoose?.invoke(it) }
                            change.consume()
                        }
                        break
                    }
                    val delta = change.position - down.position
                    if (dragging || delta.getDistance() > viewConfiguration.touchSlop) {
                        dragging = true
                        view.rotation = (initialRotation + delta.x / density * 1.15f).mod(360f)
                        view.tilt = (initialTilt + delta.y / density * .003f).coerceIn(.37f, .83f)
                        change.consume()
                    }
                }
            }
        } else Modifier
        Canvas(Modifier.matchParentSize().then(gesture)
            .spoken(if (interactive) "Model Układu Słonecznego. Wybierz cel z listy lub dotknij planety." else "Trasa rakiety")) {
            val projection = OrbitProjection(size, density, view)
            val sun = projection.sun
            val extent = projection.extent
            drawCircle(Brush.radialGradient(listOf(Color(0xFF34365A), Color.Transparent), sun, extent + 24.dp.toPx()),
                extent + 24.dp.toPx(), sun)
            repeat(54) { index ->
                val at = Offset((index * 71 % 197) / 197f * size.width, (index * 43 % 211) / 211f * size.height)
                if (index % 9 == 0) twinkle(Lavender.copy(alpha = .36f), at.x, at.y, 2.5.dp.toPx())
                else drawCircle(Mist.copy(alpha = .32f), .65.dp.toPx(), at)
            }
            for (body in OrbitBody.entries) {
                if (body.radius == 0f) continue
                val r = extent * body.radius
                val vertical = r * projection.tilt
                val selected = highlighted != null && highlighted == body.destination
                drawOval(if (selected) Gold.copy(alpha = .58f) else Rim,
                    sun - Offset(r, vertical), Size(r * 2, vertical * 2), style = Stroke(1.dp.toPx()))
            }
            val earth = projection.positions.getValue(OrbitBody.EARTH)
            val moon = projection.positions.getValue(OrbitBody.MOON)
            drawLine(Mint.copy(alpha = .7f), earth, moon, 1.dp.toPx())
            val sunRadius = 15.dp.toPx()
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF4A3), Color(0xFFFFA64E)),
                sun - Offset(sunRadius * .22f, sunRadius * .27f), sunRadius * 1.45f), sunRadius, sun)
            for ((body, at) in projection.positions.entries.sortedBy { it.value.y }) {
                val diameter = body.diameter.dp.toPx()
                val destination = body.destination
                val outline = when {
                    destination != null && destination == highlighted -> Gold
                    body == OrbitBody.MOON -> Mint
                    destination == state.location -> SkyBlue
                    else -> null
                }
                if (outline != null) drawCircle(outline, diameter / 2 + 1.dp.toPx(), at, style = Stroke(1.4.dp.toPx()))
                if (destination == null) drawDecorativePlanet(body, at, diameter / 2)
                else mapSprite(images.getValue(destination), at, diameter)
            }

            fun mapLabel(value: String, at: Offset, tint: Color, above: Boolean = false) {
                val text = measurer.measure(value, style = TextStyle(color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                val margin = 8.dp.toPx()
                val left = (at.x - text.size.width / 2f).coerceIn(margin, (size.width - text.size.width - margin).coerceAtLeast(margin))
                val top = if (above) at.y - text.size.height - 16.dp.toPx() else at.y + 14.dp.toPx()
                val origin = Offset(left, top.coerceIn(24.dp.toPx(), (size.height - text.size.height - 25.dp.toPx()).coerceAtLeast(24.dp.toPx())))
                val pad = 3.dp.toPx()
                drawRoundRect(OrbitNight.copy(alpha = .93f), origin - Offset(pad, 0f),
                    Size(text.size.width + pad * 2, text.size.height.toFloat()), CornerRadius(pad))
                drawText(text, topLeft = origin)
            }
            if (highlighted != null && highlighted != Destination.MOON) {
                val at = projection.positionOf(highlighted)
                mapLabel(highlighted.label, at, Gold, (at - moon).getDistance() < 50.dp.toPx())
            }
            mapLabel("Baza CatX", moon, Mint)

            if (flyingTo != null && rocket != null) {
                val flight = OrbitFlight(projection.positionOf(state.location), projection.positionOf(flyingTo), 55.dp.toPx())
                val path = Path().apply {
                    moveTo(flight.start.x, flight.start.y)
                    quadraticTo(flight.control.x, flight.control.y, flight.end.x, flight.end.y)
                }
                drawPath(path, Gold.copy(alpha = .19f), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(path, Gold, style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx()))))
                val fraction = flightProgress()
                val position = flight.point(fraction)
                rotate(flight.angle(fraction), position) {
                    mapSprite(rocket, position, 54.dp.toPx() * (1f - .30f * fraction))
                }
            }
        }
        Box(Modifier.align(Alignment.TopStart).padding(10.dp)) {
            Eyebrow("SCHEMAT ORBIT", Mist)
        }
        if (interactive) {
            Text("Przeciągnij, by obrócić", Modifier.align(Alignment.BottomStart).padding(10.dp), color = Lavender, fontSize = 11.sp)
        }
    }
}

private val OrbitNight = Color(0xFF101733)

private fun DrawScope.mapSprite(image: ImageBitmap, at: Offset, diameter: Float) {
    val pixels = diameter.roundToInt().coerceAtLeast(1)
    drawImage(image, dstOffset = IntOffset((at.x - pixels / 2f).roundToInt(), (at.y - pixels / 2f).roundToInt()),
        dstSize = IntSize(pixels, pixels))
}

private fun DrawScope.drawDecorativePlanet(body: OrbitBody, at: Offset, radius: Float) {
    val colors = when (body) {
        OrbitBody.MERCURY -> listOf(Color(0xFFC7B3AC), Color(0xFF665A74))
        OrbitBody.EARTH -> listOf(Color(0xFF79D8FF), Color(0xFF2E58CB))
        OrbitBody.URANUS -> listOf(Color(0xFFADF5EE), Color(0xFF4599AD))
        else -> return
    }
    drawCircle(Brush.radialGradient(colors, at - Offset(radius * .24f, radius * .35f), radius * 1.48f), radius, at)
    when (body) {
        OrbitBody.MERCURY -> {
            drawCircle(Color(0xFF736278), radius * .25f, at - Offset(radius * .4f, radius * .21f))
            drawCircle(Color(0xFF78667E), radius * .17f, at + Offset(radius * .35f, radius * .38f))
        }
        OrbitBody.EARTH -> {
            drawOval(Mint, at - Offset(radius * .73f, radius * .55f), Size(radius * .94f, radius * .54f))
            drawOval(Color(0xFF52BD90), at + Offset(-radius * .17f, radius * .03f), Size(radius * .43f, radius * .71f))
        }
        OrbitBody.URANUS -> rotate(-50f, at) {
            drawOval(Color(0xFFBBF5EA).copy(alpha = .62f), at - Offset(radius * 1.3f, radius * .24f),
                Size(radius * 2.6f, radius * .48f), style = Stroke(1.dp.toPx()))
        }
    }
}
