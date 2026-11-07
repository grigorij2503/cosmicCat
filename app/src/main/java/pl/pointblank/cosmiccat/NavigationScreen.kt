package pl.pointblank.cosmiccat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NavigationScreen(
    navigation: ScreenState.Navigation, state: Progress, assets: AssetManager, game: GameViewModel, orbit: OrbitView,
) {
    val returning = navigation.returning
    var typed by rememberSaveable { mutableStateOf("") }
    var errors by rememberSaveable { mutableIntStateOf(0) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var showHint by rememberSaveable { mutableStateOf(returning) }
    var moving by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }
    val focus = LocalFocusManager.current
    val target = when {
        returning -> Destination.MOON
        navigation.freeFlight -> null
        else -> state.assignmentDestination
    }
    val choose: (Destination) -> Unit = {
        moving = false
        typed = it.label
        showError = false
        focus.clearFocus()
    }
    val submit: () -> Unit = {
        moving = false
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
    LaunchedEffect(moving) {
        if (moving) {
            var previous = withFrameMillis { it }
            while (true) {
                withFrameMillis { now ->
                    if (moving) orbit.elapsed += (now - previous).coerceIn(0L, 40L) / 5000f
                    previous = now
                }
            }
        }
    }
    GamePage(
        if (returning) "06  /  POWRÓT DO BAZY" else "03  /  NAWIGACJA",
        "GDZIE LECISZ?",
        if (returning) "CatX czeka w bazie na Księżycu." else "Dotknij celu na mapie, wybierz z listy lub wpisz nazwę.",
        state.coins, game::back,
        footer = { Action("LEĆ!", submit, Modifier.fillMaxWidth(), enabled = typed.isNotBlank()) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OrbitMap(orbit, state, assets, Destination.parse(typed) ?: target.takeIf { showHint },
                onMapTouch = { moving = false }, choose = choose)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Obrót", color = Mist, fontSize = 12.sp)
                Slider(value = orbit.rotation, onValueChange = {
                    moving = false
                    orbit.rotation = it
                },
                    modifier = Modifier.weight(1f).spoken("Obrót układu"), valueRange = 0f..360f)
                SmallAction(if (moving) "PAUZA" else "RUCH PLANET", { moving = !moving },
                    Modifier.width(115.dp), selected = moving, size = 11)
            }
        }
        PanelCard(Modifier.fillMaxWidth(), if (showError) Coral else Rim) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (target != null) {
                    SmallAction("?", { showHint = !showHint }, Modifier.size(48.dp), selected = showHint,
                        description = "Podpowiedź celu", size = 22)
                }
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it.take(40); showError = false },
                    label = { Text("Nazwa celu") },
                    singleLine = true, isError = showError,
                    modifier = Modifier.weight(1f).graphicsLayer { translationX = shake.value.dp.toPx() },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { submit() }),
                )
                LandingSitePicker(choose)
            }
            if (showError) Text("Wybierz nazwę jednego z lądowisk na mapie.", fontSize = 16.sp, color = Coral, fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (showHint && target != null) {
                Text("Cel lotu: ${target.label}", color = Mint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LandingSitePicker(choose: (Destination) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SmallAction("LISTA", { expanded = true }, Modifier.width(64.dp), description = "Lista lądowisk", size = 11)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            orbitalLandingSites.forEach { destination ->
                DropdownMenuItem(
                    text = { Text(if (destination == Destination.MOON) "Księżyc · Baza CatX" else destination.label) },
                    onClick = {
                        expanded = false
                        choose(destination)
                    },
                )
            }
        }
    }
}

@Composable
internal fun FlightScreen(
    flight: ScreenState.Flight, state: Progress, assets: AssetManager, game: GameViewModel, orbit: OrbitView,
) {
    val animation = remember(flight) { Animatable(0f) }
    LaunchedEffect(flight) {
        animation.animateTo(1f, tween(1650, easing = FastOutSlowInEasing))
        game.land(flight)
    }
    GamePage("✦  /  LOT RAKIETĄ", "Czas na lot!", "Cel: ${flight.destination.label}", state.coins) {
        OrbitMap(orbit, state, assets, flight.destination, flight.destination, flightProgress = { animation.value })
        PanelCard(Modifier.fillMaxWidth(), Gold) {
            Eyebrow("NAPĘD GOTOWY", Gold)
            Text("Zapięte pasy? Lecimy!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Snow)
        }
    }
}
