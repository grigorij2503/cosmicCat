package pl.pointblank.cosmiccat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

private val mapPositions = mapOf(
    Destination.NEPTUNE to Offset(.19f, .18f),
    Destination.MARS to Offset(.48f, .16f),
    Destination.PLUTO to Offset(.79f, .22f),
    Destination.VENUS to Offset(.48f, .49f),
    Destination.MOON to Offset(.79f, .52f),
    Destination.JUPITER to Offset(.49f, .78f),
    Destination.SATURN to Offset(.79f, .79f),
)
private val rocketPad = Offset(.18f, .59f)
private val arcTop = Offset(.38f, .02f)

@Composable
fun NavigationScreen(returning: Boolean, state: Progress, assets: AssetManager, game: GameViewModel) {
    var typed by rememberSaveable { mutableStateOf("") }
    var errors by rememberSaveable { mutableIntStateOf(0) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var showHint by rememberSaveable { mutableStateOf(returning) }
    val shake = remember { Animatable(0f) }
    val focus = LocalFocusManager.current
    val target = if (returning) Destination.MOON else state.mission.destination
    val submit: () -> Unit = {
        val destination = Destination.parse(typed)
        if (destination == null) {
            showError = true
            errors++
        } else {
            focus.clearFocus()
            game.launch(destination)
        }
    }
    LaunchedEffect(errors) {
        if (errors > 0 && showError) {
            shake.snapTo(0f)
            shake.animateTo(0f, keyframes {
                durationMillis = 300
                -7f at 50
                7f at 110
                -4f at 175
                3f at 230
            })
        }
    }
    GamePage(
        if (returning) "06  /  POWRÓT DO BAZY" else "03  /  MAPA GWIEZDNA",
        "GDZIE LECISZ?",
        if (returning) "CatX czeka w bazie na Księżycu." else "Wpisz cel misji albo nazwę innej planety z mapy.",
        state.coins, game::back,
    ) {
        PanelCard(Modifier.fillMaxWidth(), if (showError) Coral else Rim) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallAction("?", { showHint = !showHint }, Modifier.size(48.dp), selected = showHint,
                    description = "Podpowiedź celu", size = 22)
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it.take(40); showError = false },
                    label = { Text("Wpisz:") },
                    singleLine = true, isError = showError,
                    modifier = Modifier.weight(1f).graphicsLayer { translationX = shake.value.dp.toPx() },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { submit() }),
                )
            }
            if (showError) Text("To nie jest nazwa planety", fontSize = 17.sp, color = Coral, fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (showHint) Text("Cel misji: ${target.title}", color = Mint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Action("LEĆ!", submit, Modifier.fillMaxWidth())
        }
        SpaceMap(state, assets, Destination.parse(typed) ?: target.takeIf { showHint || returning })
    }
}

@Composable
fun FlightScreen(flight: ScreenState.Flight, state: Progress, assets: AssetManager, game: GameViewModel) {
    val animation = remember(flight) { Animatable(0f) }
    LaunchedEffect(flight) {
        animation.animateTo(1f, tween(1650, easing = FastOutSlowInEasing))
        game.land(flight)
    }
    GamePage("✦  /  LOT RAKIETĄ", "Czas na lot!", "Cel: ${flight.destination.label}", state.coins) {
        SpaceMap(state, assets, flight.destination, flight.destination, animation)
        PanelCard(Modifier.fillMaxWidth(), Gold) {
            Eyebrow("NAPĘD GOTOWY", Gold)
            Text("Zapięte pasy? Lecimy!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Snow)
        }
    }
}

@Composable
private fun SpaceMap(
    state: Progress, assets: AssetManager, hint: Destination?,
    flyingTo: Destination? = null, animation: Animatable<Float, *>? = null,
) {
    val shape = RoundedCornerShape(22.dp)
    val rocket = remember(state.vehicle, state.cat) { assets.rocket(state.vehicle, state.cat) }
    val images = remember { Destination.entries.associateWith(assets::planet) }
    BoxWithConstraints(Modifier.fillMaxWidth().height(302.dp).clip(shape).background(Color(0xFF151D3D))
        .border(1.dp, Rim, shape)) {
        val w = maxWidth
        val h = maxHeight
        val target = flyingTo?.let(mapPositions::getValue)
        Canvas(Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            fun point(p: Offset) = Offset(p.x * width, p.y * height)
            val center = point(Offset(.33f, .63f))
            val extent = maxOf(width, height) * .69f
            drawCircle(Brush.radialGradient(listOf(Color(0xFF364276), Color(0xFF202A52), Color.Transparent),
                center, extent), extent, center)
            rotate(-18f, center) {
                for (i in 0..3) {
                    val orbitW = width * (.31f + i * .28f)
                    val orbitH = height * (.24f + i * .24f)
                    drawOval(Lavender.copy(alpha = .24f - i * .03f),
                        center - Offset(orbitW / 2, orbitH / 2), Size(orbitW, orbitH),
                        style = Stroke(1.dp.toPx()))
                }
            }
            repeat(47) { index ->
                val at = Offset(((index * 37 + 6) % 99) / 100f * width, ((index * 61 + 3) % 97) / 100f * height)
                if (index % 7 == 0) twinkle(Lavender.copy(alpha = .58f), at.x, at.y, 3.2.dp.toPx())
                else drawCircle(Mist.copy(alpha = .35f), .8.dp.toPx(), at)
            }
            if (target != null) {
                val start = point(rocketPad)
                val end = point(target)
                val control = point(arcTop)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(control.x, control.y, end.x, end.y)
                }
                drawPath(path, Gold.copy(alpha = .19f), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(path, Gold, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 6.dp.toPx()))))
            }
        }
        mapPositions.forEach { (destination, point) ->
            val selected = destination == hint
            val tint = colorOf(destination.color)
            Column(Modifier.offset(w * point.x - 39.dp, h * point.y - 32.dp).width(78.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                val glow = if (selected) Modifier.background(Brush.radialGradient(listOf(tint.copy(alpha = .35f),
                    tint.copy(alpha = .15f))), CircleShape).border(1.4.dp, tint, CircleShape) else Modifier
                Sprite(images.getValue(destination), Modifier.size(59.dp).then(glow))
                Text(destination.title, color = if (selected) tint else Snow, fontWeight = FontWeight.Black,
                    fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Center)
            }
        }
        val rocketSize = if (target == null) 108.dp else 73.dp
        Sprite(rocket, Modifier
            .offset(w * rocketPad.x - rocketSize / 2, h * rocketPad.y - rocketSize / 2)
            .size(rocketSize)
            .graphicsLayer {
                if (target != null && animation != null) {
                    val t = animation.value
                    val u = 1f - t
                    val p = rocketPad * (u * u) + arcTop * (2f * u * t) + target * (t * t)
                    translationX = (w * (p.x - rocketPad.x)).toPx()
                    translationY = (h * (p.y - rocketPad.y)).toPx()
                    val velocity = (arcTop - rocketPad) * u + (target - arcTop) * t
                    rotationZ = 90f + Math.toDegrees(atan2((h * velocity.y).value, (w * velocity.x).value).toDouble()).toFloat()
                    scaleX = 1f - .37f * t
                    scaleY = scaleX
                }
            }, "Rakieta gracza")
    }
}
