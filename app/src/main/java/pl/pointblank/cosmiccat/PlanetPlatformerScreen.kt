package pl.pointblank.cosmiccat

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlin.math.sin
import kotlin.random.Random

private enum class Nearby { NONE, SUPPLY, STATION, GOAL, SEED, SECRET, REPLAY }
internal enum class ExpeditionDialog { PUZZLE, SECRET, TOWER, FRIEND }

internal class ExplorationSession(state: Progress) {
    private val trail = trails.getValue(state.location)
    private val stage = state.explorationStage(state.location)
    val world = PlatformWorld(
        state.vehicle, trail, state.portLevel(Building.HANGAR), stage,
        startAtGoal = state.found && stage == TrailStage.COMPLETE && state.activeMission == state.location,
    )
    var dialog by mutableStateOf<ExpeditionDialog?>(null)
    var driving by mutableStateOf(false)
        private set

    fun toggleRover() {
        driving = !driving
        world.rover = driving
    }
}

@Composable
internal fun PlanetPlatformerScreen(
    state: Progress, assets: AssetManager, game: GameViewModel, session: ExplorationSession,
) {
    val destination = state.location
    val trail = trails.getValue(destination)
    val contract = state.contractAt(destination)
    val stage = state.explorationStage(destination)
    val supply = when (stage) {
        TrailStage.FIRST_SUPPLY -> trail.first
        TrailStage.SECOND_SUPPLY -> trail.second
        else -> null
    }
    val discovery = trail.mission.discovery
    val automaticPickup = discovery == Discovery.METEOR || discovery == Discovery.ICE || discovery == Discovery.RING
    val reportAtLanding = stage == TrailStage.COMPLETE && !state.found && state.assignmentDestination == destination
    val secretCollected = destination in state.stickers
    val seedCollected = destination in state.seeds
    val alien = Alien.on(destination)
    val hangar = state.portLevel(Building.HANGAR)
    val tower = state.portLevel(Building.TOWER)
    val world = session.world
    val cat = remember(state.cat) { assets.cat(state.cat) }
    val rover = remember(state.vehicle) { assets.rover(state.vehicle) }
    val alienSprite = remember(alien) { alien?.let(assets::alien) }
    val goal = remember(discovery) { assets.discovery(discovery) }
    val orb = remember(destination) { assets.planet(destination) }
    var clock by remember { mutableFloatStateOf(0f) }
    var near by remember { mutableStateOf(Nearby.NONE) }
    var area by remember { mutableIntStateOf(trail.areaAt(world.x)) }
    var direction by remember { mutableStateOf("") }
    val driving = session.driving
    var dialog by session::dialog
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val objectiveX = supply?.x ?: when (stage) {
        TrailStage.PUZZLE -> trail.stationX
        TrailStage.GOAL -> trail.goalX
        TrailStage.COMPLETE -> if (reportAtLanding) 108f else null
        else -> null
    }
    LaunchedEffect(near, stage, game.busy) {
        if (near == Nearby.GOAL && stage == TrailStage.GOAL && automaticPickup && !game.busy && game.error == null) {
            game.finishTrail(destination)
        }
    }
    LaunchedEffect(world, stage, game.busy, dialog, seedCollected, secretCollected, lifecycle) {
        world.gateOpen = stage >= TrailStage.GOAL
        if (dialog != null || game.busy) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var last = 0L
            try {
                while (true) {
                    withInfiniteAnimationFrameMillis { frame ->
                        if (last > 0) {
                            val dt = ((frame - last) / 1000f).coerceIn(0f, .034f)
                            world.step(dt)
                            clock += dt
                            area = trail.areaAt(world.x)
                            direction = when {
                                objectiveX == null -> ""
                                objectiveX < world.x - 70f -> "←  "
                                objectiveX > world.x + 70f -> "→  "
                                else -> ""
                            }
                            near = when {
                                supply != null && world.touches(supply.x, 58f) -> Nearby.SUPPLY
                                world.touches(trail.stationX, 68f) -> Nearby.STATION
                                stage == TrailStage.GOAL && world.touches(trail.goalX, 59f) -> Nearby.GOAL
                                !seedCollected && world.touches(trail.seedX, 51f) -> Nearby.SEED
                                !secretCollected && world.atSecret() -> Nearby.SECRET
                                stage == TrailStage.COMPLETE && contract == null && world.touches(108f, 72f) -> Nearby.REPLAY
                                else -> Nearby.NONE
                            }
                        }
                        last = frame
                    }
                }
            } finally {
                world.left = false
                world.right = false
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 520.dp
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            MissionRail(state.coins, game::back)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("${area + 1}/3  ·  ${trail.areas[area]}", Mint)
                    Text(destination.title, color = Snow, fontSize = if (compact) 20.sp else 26.sp,
                        lineHeight = 27.sp, fontWeight = FontWeight.Black)
                }
                if (tower > 0 && !secretCollected) {
                    SmallAction("🔭", { dialog = ExpeditionDialog.TOWER }, Modifier.width(48.dp),
                        description = "Wskazówka z wieży", size = 19)
                }
                SmallAction(if (driving) "WYSIĄDŹ" else "ŁAZIK", session::toggleRover,
                    Modifier.widthIn(min = 67.dp), selected = driving, enabled = !game.busy, size = 12)
            }
            val objective = when {
                reportAtLanding -> "Zgłoś wcześniejsze odkrycie przy lądowisku."
                contract != null && !state.found -> "Zlecenie: ${contract.title}. Podejdź do stanowiska."
                else -> trail.objective(stage)
            }
            Text(direction + objective, color = if (stage == TrailStage.COMPLETE) Mint else Gold,
                fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp)
            val frame = RoundedCornerShape(17.dp)
            Canvas(Modifier.fillMaxWidth().weight(1f).clip(frame).border(1.dp, Rim, frame)
                .spoken("Plansza eksploracji. $objective")) {
                val t = clock
                val unit = minOf(size.height / WORLD_HEIGHT, size.width / 436f)
                if (unit <= 0f) return@Canvas
                val width = size.width / unit
                val skyExtension = size.height / unit - WORLD_HEIGHT
                val camera = (world.x - width * .32f).coerceIn(0f, (trail.width - width).coerceAtLeast(0f))
                withTransform({ scale(unit, unit, Offset.Zero); translate(-camera, skyExtension) }) {
                    terrain(trail, orb, camera, width, skyExtension, t)
                    val view = camera - 120f..camera + width + 120f
                    if (supply != null && supply.x in view) {
                        supplyCrate(supply.x, colorOf(destination.color))
                        marker(supply.x, GROUND - 90f, t)
                    }
                    if (trail.gateX in view) drawGate(trail.gateX, stage >= TrailStage.GOAL, t)
                    if (trail.stationX in view) {
                        // Leave room beside the console for the player to stand during a conversation.
                        val speakerX = trail.stationX + if (alienSprite != null) 54f else 0f
                        if (alienSprite != null) bitmap(alienSprite, speakerX, GROUND + 5f, 113f)
                        else supplyCrate(speakerX, SkyBlue)
                        if (stage == TrailStage.PUZZLE) marker(speakerX, GROUND - 110f, t)
                    }
                    if (stage == TrailStage.GOAL && trail.goalX in view) {
                        val center = Offset(trail.goalX, GROUND - 37)
                        drawCircle(Brush.radialGradient(listOf(Gold.copy(alpha = .49f), Color.Transparent),
                            center, 72f), 72f, center)
                        if (discovery == Discovery.FRIEND) supplyCrate(trail.goalX, SkyBlue)
                        else bitmap(goal, trail.goalX, GROUND + 3, 120f)
                        marker(trail.goalX, GROUND - 113, t)
                    }
                    if (!seedCollected && trail.seedX in view) drawSeed(trail.seedX)
                    if (!secretCollected && trail.secretX in view) {
                        secretEntrance(trail, t, tower == 3)
                    }
                    val player = if (driving) rover else cat
                    val playerSize = if (driving) 118f else 95f
                    val spriteFeet = world.feet + if (driving) 22f else 7f
                    withTransform({ scale(world.facing.toFloat(), 1f, Offset(world.x, spriteFeet)) }) {
                        bitmap(player, world.x, spriteFeet, playerSize)
                    }
                    if (driving) bitmap(cat, world.x + 12f * world.facing, spriteFeet - 64f, 47f)
                }
            }
            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                val button = Modifier.fillMaxWidth()
                val ready = !game.busy
                when (near) {
                    Nearby.SUPPLY -> if (supply != null) {
                        Action("${supply.glyph}  Zabierz: ${supply.name}", {
                            near = Nearby.NONE
                            game.takeSupply(destination, stage)
                        },
                            button, Mint, enabled = ready)
                    }
                    Nearby.STATION -> when {
                        stage == TrailStage.PUZZLE -> Action(
                            if (alien == null) "OTWÓRZ KONSOLĘ" else "POROZMAWIAJ: ${alien.label}",
                            { dialog = ExpeditionDialog.PUZZLE }, button, Mint, enabled = ready)
                        supply != null -> Text("Przynieś oba przedmioty, żeby otworzyć przejście.",
                            color = Mist, fontSize = 13.sp, textAlign = TextAlign.Center)
                        alien != null -> Action("✋ PIĄTKA Z KOSMITĄ", { dialog = ExpeditionDialog.FRIEND },
                            button, Mint, enabled = ready)
                        else -> Unit
                    }
                    Nearby.GOAL -> if (stage == TrailStage.GOAL) {
                        Action(trail.finalAction, { game.finishTrail(destination) }, button, Gold, enabled = ready)
                    }
                    Nearby.SEED -> Action("🌱 ZABIERZ NASIONKO", { game.collectSeed(destination) }, button, Mint, enabled = ready)
                    Nearby.SECRET -> Action("✦ ZAJRZYJ W SZCZELINĘ", { dialog = ExpeditionDialog.SECRET },
                        button, Lavender, enabled = ready)
                    Nearby.REPLAY -> if (reportAtLanding) {
                        Action("ZGŁOŚ ODKRYCIE CATXOWI", { game.finishTrail(destination) }, button, Gold, enabled = ready)
                    } else Action("POWTÓRZ WYPRAWĘ", { game.replayTrail(destination) }, button, Mint, enabled = ready)
                    Nearby.NONE -> Text(when {
                        stage == TrailStage.COMPLETE || state.found -> "Wróć do rakiety strzałką u góry albo szukaj sekretów."
                        hangar > 0 && !driving -> "Naciśnij skok drugi raz w powietrzu, by użyć jetpacka."
                        else -> "Przytrzymaj strzałkę, aby się ruszać."
                    }, color = Mist, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FlightControl("←", "Lewo", SkyBlue, Modifier.weight(1f), { world.left = it }, { world.nudge(-1) })
                FlightControl("→", "Prawo", SkyBlue, Modifier.weight(1f), { world.right = it }, { world.nudge(1) })
                FlightControl("⇡", "Skok / Jetpack", Gold, Modifier.weight(1.25f), { if (it) world.jump() }, world::jump)
            }
        }
    }
    val close = { dialog = null }
    when (dialog) {
        ExpeditionDialog.PUZZLE -> ChallengeDialog(contract?.challenge ?: trail.challenge, alien, assets, state.portLevel(Building.LAB), close) {
            game.solveStation(destination, it)
            close()
        }
        ExpeditionDialog.SECRET -> AlertDialog(onDismissRequest = close,
            title = { Text("Sekretna skrytka!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreviewStage(Modifier.fillMaxWidth().height(144.dp), "NAKLEJKA DO ALBUMU", Lavender) {
                        StickerArt(destination, Modifier.size(92.dp))
                    }
                    Text(trail.sticker, color = Snow, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    game.collectSticker(destination)
                    close()
                }) { Text("ZABIERZ NAKLEJKĘ") }
            },
            dismissButton = { TextButton(onClick = close) { Text("WRÓĆ") } })
        ExpeditionDialog.TOWER -> AlertDialog(onDismissRequest = close, title = { Text("Sygnał z wieży") },
            text = { Text(trail.secretHint(tower)) },
            confirmButton = { TextButton(onClick = close) { Text("OKEJ") } })
        ExpeditionDialog.FRIEND -> AlertDialog(onDismissRequest = close, title = { Text("✋ Piątka od ${alien?.label}!") },
            text = { Text("Twój przyjaciel może Cię odwiedzić na Księżycu.") },
            confirmButton = { TextButton(onClick = close) { Text("OKEJ") } })
        null -> Unit
    }
}

@Composable
private fun FlightControl(
    glyph: String, label: String, tint: Color, modifier: Modifier,
    held: (Boolean) -> Unit, tap: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val onHeld by rememberUpdatedState(held)
    val onTap by rememberUpdatedState(tap)
    val shape = RoundedCornerShape(15.dp)
    Column(modifier.heightIn(min = 65.dp)
        .background(if (pressed) tint else tint.copy(alpha = .17f), shape)
        .border(1.dp, tint.copy(alpha = .67f), shape)
        .semantics(mergeDescendants = true) {
            role = Role.Button
            onClick(label) { onTap(); true }
        }
        .focusable()
        .pointerInput(Unit) {
            detectTapGestures(onPress = {
                pressed = true
                onHeld(true)
                try { tryAwaitRelease() } finally {
                    pressed = false
                    onHeld(false)
                }
            })
        }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, color = if (pressed) Midnight else tint, fontSize = 25.sp, lineHeight = 25.sp, fontWeight = FontWeight.Black)
        Text(label, color = if (pressed) Midnight else Snow, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
    }
}

private val terrainStars = Random(85).let { random ->
    List(160) { Offset(random.nextFloat() * 2200f, random.nextFloat() * 215f) }
}

private fun DrawScope.terrain(trail: Trail, orb: ImageBitmap, camera: Float, width: Float, skyExtension: Float, time: Float) {
    val planet = trail.destination
    val soil = colorOf(planet.ground)
    val dusk = colorOf(planet.sky)
    val horizonEnd = camera + width + 180f
    expeditionSky(planet, camera, width, skyExtension)
    for ((index, star) in terrainStars.withIndex()) {
        val x = star.x + camera * .64f
        if (x in camera - 3..camera + width + 3) drawCircle(
            Mist.copy(alpha = if (index % 7 == 0) .75f else .37f),
            if (index % 7 == 0) 2.2f else 1f, Offset(x, star.y - skyExtension * (1 - star.y / 215f)))
    }
    bitmap(orb, 333f + camera * .93f, 186f - skyExtension * .45f, 174f)
    if (planet.cloudStation) {
        repeat((trail.width / 84).toInt() + 2) { index ->
            val x = index * 84f + camera * .23f
            if (x !in camera - 175f..horizonEnd) return@repeat
            val y = 203f + (index * 29 % 67)
            val bank = when (planet) {
                Destination.JUPITER -> 211f
                Destination.NEPTUNE -> 122f
                else -> 171f
            }
            val drift = if (planet == Destination.NEPTUNE) sin(time * .7f + index) * 13f else 0f
            drawOval(soil.copy(alpha = .42f), Offset(x + drift, y), Size(bank, 49f))
            drawOval(Snow.copy(alpha = .11f), Offset(x + 12 + drift, y - 13), Size(bank * .8f, 32f))
        }
        val metal = soil.mix(Panel, .65f)
        drawRect(Brush.verticalGradient(listOf(metal, Midnight), GROUND, WORLD_HEIGHT), Offset(camera, GROUND),
            Size(width, WORLD_HEIGHT - GROUND))
        val trim = when (planet) { Destination.JUPITER -> Coral; Destination.NEPTUNE -> SkyBlue; else -> Gold }
        drawRect(trim, Offset(camera, GROUND), Size(width, 4f))
        repeat((trail.width / 94).toInt() + 2) { index ->
            val x = index * 94f
            if (x !in camera - 100f..horizonEnd) return@repeat
            drawRoundRect(Midnight, Offset(x + 8f, GROUND + 15), Size(71f, 34f), CornerRadius(7f))
            drawRoundRect(trim.copy(alpha = .7f), Offset(x + 17f, GROUND + 29), Size(22f, 4f), CornerRadius(2f))
            if (planet == Destination.SATURN) {
                repeat(3) { i -> stripe(trim.copy(alpha = .49f), x + 48 + i * 7, GROUND + 38, x + 55 + i * 7, GROUND + 25) }
            } else if (planet == Destination.NEPTUNE) {
                stripe(SkyBlue.copy(alpha = .46f), x + 52, GROUND + 35, x + 70, GROUND + 27, 2f)
            }
            stripe(trim.copy(alpha = .23f), x + 6f, GROUND + 58, x + 85f, GROUND + 58)
        }
    } else {
        repeat(2) { layer ->
            val offset = camera * (.53f - .18f * layer)
            val step = when (planet) { Destination.PLUTO -> 116; Destination.VENUS -> 185; else -> 151 }
            val firstPeak = ((camera - offset) / step).toInt() - 3
            val lastPeak = ((horizonEnd - offset) / step).toInt() + 3
            val mountains = Path().apply {
                moveTo(camera - 230f, GROUND)
                for (index in firstPeak..lastPeak) {
                    val x = offset + index * step - 180
                    val peak = 147f + ((index * 43 + planet.ordinal * 19).mod(72)) + layer * 40f
                    lineTo(x, 275f + layer * 13)
                    when (planet) {
                        Destination.PLUTO -> { lineTo(x + 47, peak); lineTo(x + 71, peak + 23f) }
                        Destination.VENUS -> quadraticTo(x + step * .49f, peak - 77f, x + step, 278f + layer * 13)
                        else -> { lineTo(x + 35, peak); lineTo(x + 88, peak + 9); lineTo(x + 110, peak + 44) }
                    }
                }
                lineTo(horizonEnd + 260f, GROUND)
                close()
            }
            drawPath(mountains, dusk.mix(soil, if (layer == 0) .27f else .58f))
        }
        drawRect(Brush.verticalGradient(listOf(soil, soil.lerpTo(Midnight, .64f)), GROUND, WORLD_HEIGHT),
            Offset(camera, GROUND), Size(width, WORLD_HEIGHT - GROUND))
        drawRect(soil.lerpTo(Snow, .28f), Offset(camera, GROUND), Size(width, 4f))
        repeat((trail.width / 51).toInt() + 1) { index ->
            val x = index * 51f
            if (x !in camera - 70f..horizonEnd) return@repeat
            val y = GROUND + 15f + index * 13 % 59
            drawOval(soil.lerpTo(Midnight, .42f), Offset(x, y), Size(23f + index % 4 * 12, 6f))
            stripe(soil.mix(Snow, .36f).copy(alpha = .40f), x + 6f, y + 9f, x + 25 + index % 4 * 11, y + 7f, 1.5f)
        }
    }
    expeditionLandmarks(trail, camera, width, time)
    for (rock in trail.obstacles) {
        if (rock.right < camera || rock.left > horizonEnd) continue
        val ice = planet == Destination.PLUTO
        val shape = Path().apply {
            moveTo(rock.left, GROUND)
            lineTo(rock.left, rock.top + 15f)
            lineTo(rock.left + if (ice) 18f else 11f, rock.top)
            lineTo(rock.right - if (ice) 25f else 12f, rock.top)
            lineTo(rock.right, rock.top + 14f)
            lineTo(rock.right, GROUND)
            close()
        }
        val color = if (ice) SkyBlue else if (planet.cloudStation) Color(0xFF8498C0) else soil
        drawPath(shape, Brush.verticalGradient(listOf(color.lerpTo(Snow, .38f), color.lerpTo(Midnight, .36f)),
            rock.top, GROUND))
        drawPath(shape, Midnight.copy(alpha = .64f), style = Stroke(2.5f))
        drawLine(color.lerpTo(Snow, .57f), Offset(rock.left + 20, rock.top + 4), Offset(rock.right - 15, rock.top + 4), 2f)
    }
}

private fun DrawScope.supplyCrate(x: Float, tint: Color) {
    drawRoundRect(Midnight, Offset(x - 33f, GROUND - 46f), Size(66f, 46f), CornerRadius(9f))
    drawRoundRect(tint, Offset(x - 28f, GROUND - 41f), Size(56f, 36f), CornerRadius(7f))
    drawRoundRect(tint.lerpTo(Midnight, .57f), Offset(x - 28f, GROUND - 22f), Size(56f, 17f), CornerRadius(5f))
    drawRoundRect(Gold, Offset(x - 7f, GROUND - 26f), Size(14f, 11f), CornerRadius(3f))
}

private fun DrawScope.drawGate(x: Float, opened: Boolean, time: Float) {
    drawRoundRect(Midnight, Offset(x - 30f, GROUND - 280f), Size(60f, 16f), CornerRadius(6f))
    drawRoundRect(if (opened) Mint else Coral, Offset(x - 15f, GROUND - 275f), Size(30f, 6f), CornerRadius(3f))
    drawRoundRect(Midnight, Offset(x - 35f, GROUND - 13f), Size(70f, 13f), CornerRadius(4f))
    if (!opened) {
        drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Coral.copy(alpha = .65f), Color.Transparent), x - 17, x + 17),
            Offset(x - 17f, GROUND - 264f), Size(34f, 252f))
        for (offset in 0..7) {
            val y = GROUND - 18f - ((offset * 31 + time * 40f) % 240f)
            drawLine(Snow.copy(alpha = .55f), Offset(x - 7f, y), Offset(x + 7f, y), 2.5f)
        }
    }
}

private fun DrawScope.drawSeed(x: Float) {
    drawOval(Mint.copy(alpha = .25f), Offset(x - 30f, GROUND - 43f), Size(60f, 45f))
    drawLine(Mint, Offset(x, GROUND - 8f), Offset(x, GROUND - 32f), 4f)
    drawOval(Mint, Offset(x - 19f, GROUND - 28f), Size(19f, 10f))
    drawOval(Gold, Offset(x, GROUND - 36f), Size(19f, 12f))
}

private fun DrawScope.secretEntrance(trail: Trail, time: Float, highlight: Boolean) {
    val x = trail.secretX
    val top = trail.secretRock.top
    drawRoundRect(Midnight, Offset(x - 21f, top + 6f), Size(42f, 44f), CornerRadius(18f))
    drawLine(Lavender.copy(alpha = .65f), Offset(x - 8f, top + 17f), Offset(x + 8f, top + 34f), 3f)
    if (highlight) {
        val y = top - 31f + 4f * sin(time * 6f)
        drawLine(Lavender, Offset(x - 11f, y), Offset(x + 11f, y), 4f)
        drawLine(Lavender, Offset(x, y - 11f), Offset(x, y + 11f), 4f)
    }
}

private fun Color.lerpTo(other: Color, amount: Float) = androidx.compose.ui.graphics.lerp(this, other, amount)

private fun DrawScope.bitmap(image: ImageBitmap, x: Float, feet: Float, size: Float) {
    val pixels = size.toInt().coerceAtLeast(1)
    drawImage(image, dstOffset = IntOffset((x - size / 2).toInt(), (feet - size).toInt()), dstSize = IntSize(pixels, pixels))
}

private fun DrawScope.marker(x: Float, y: Float, t: Float) {
    val y0 = y + 6 * sin(t * 6f)
    val arrow = Path().apply {
        moveTo(x - 15, y0 + 13); lineTo(x - 5, y0 + 13); lineTo(x - 5, y0)
        lineTo(x + 5, y0); lineTo(x + 5, y0 + 13); lineTo(x + 15, y0 + 13)
        lineTo(x, y0 + 29); close()
    }
    drawPath(arrow, Gold)
    drawPath(arrow, Midnight, style = Stroke(2f))
}
