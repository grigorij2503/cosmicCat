package pl.pointblank.cosmiccat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ChallengeDialog(
    challenge: Challenge,
    speaker: Alien?,
    assets: AssetManager,
    lab: Int,
    onClose: () -> Unit,
    onSolved: (List<Int>) -> Unit,
) {
    var answer by rememberSaveable(challenge) {
        mutableStateOf(if (challenge is Mirrors) ArrayList(challenge.initial) else arrayListOf())
    }
    var tried by rememberSaveable(challenge) { mutableStateOf(false) }
    var hints by rememberSaveable(challenge) { mutableIntStateOf(0) }
    val maxHints = minOf(lab, challenge.hints.size)
    fun select(values: List<Int>) {
        answer = ArrayList(values)
        tried = false
    }

    val tint = speaker?.color?.color() ?: SkyBlue
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PortraitStage(tint, Modifier.size(52.dp)) {
                    if (speaker != null) {
                        Sprite(remember(speaker) { assets.alien(speaker) }, Modifier.size(46.dp))
                    } else {
                        ConsoleBadge(tint, Modifier.size(39.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Eyebrow(speaker?.label ?: "KONSOLA CATXA", tint)
                    Text(challenge.title, color = Snow, fontWeight = FontWeight.Black, fontSize = 21.sp, lineHeight = 24.sp)
                }
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(challenge.instruction, color = Snow, fontSize = 16.sp, lineHeight = 21.sp)
                when (challenge) {
                    is Batteries -> {
                        Text("Razem: ${answer.sumOf { challenge.charges[it] }} / ${challenge.target} iskier",
                            color = Gold, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            challenge.charges.forEachIndexed { index, charge ->
                                SmallAction("$charge\n${"✦".repeat(charge)}", {
                                    if (index in answer) select(answer - index)
                                    else if (answer.size < 2) select(answer + index)
                                }, Modifier.weight(1f), selected = index in answer, size = 13,
                                    description = "Bateria: $charge iskier")
                            }
                        }
                    }
                    is CrystalPattern -> {
                        Row(Modifier.fillMaxWidth().spoken(
                            (challenge.shown.map(ShapeToken::label) + "brakujący kryształ").joinToString(", ")),
                            horizontalArrangement = Arrangement.SpaceEvenly) {
                            challenge.shown.forEach { token ->
                                Text(token.glyph, color = token.color.color(), fontSize = 22.sp)
                            }
                            Text("?", color = Gold, fontSize = 22.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            ShapeToken.entries.forEach { token ->
                                SmallAction("${token.glyph}\n${token.label}", { select(listOf(token.ordinal)) },
                                    Modifier.weight(1f), selected = token.ordinal in answer, size = 12,
                                    tint = token.color.color(), description = token.label)
                            }
                        }
                    }
                    is Mirrors -> {
                        MirrorBoard(challenge, answer)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            challenge.mirrors.forEachIndexed { index, _ ->
                                val slash = answer[index] == 0
                                SmallAction("Lusterko ${index + 1}  ${if (slash) "/" else "\\"}", {
                                    select(answer.mapIndexed { i, value -> if (i == index) 1 - value else value })
                                }, Modifier.weight(1f), size = 12,
                                    description = "Obróć lusterko ${index + 1}. ${if (slash) "W prawo" else "W lewo"}")
                            }
                        }
                    }
                    is DroneRoute -> {
                        DroneBoard(challenge, if (tried) challenge.trace(answer) else emptyList())
                        val commands = answer.joinToString("  ") { Move.entries[it].glyph }
                        Text(if (commands.isEmpty()) "Tu pojawią się strzałki" else commands, color = Mint,
                            fontSize = 18.sp, modifier = Modifier.spoken(if (answer.isEmpty()) "Trasa pusta"
                                else answer.joinToString(", ") { Move.entries[it].label }))
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Move.entries.forEach { move ->
                                SmallAction(move.glyph, { select(answer + move.ordinal) }, Modifier.weight(1f),
                                    enabled = answer.size < challenge.limit, description = move.label, size = 21)
                            }
                        }
                        if (answer.isNotEmpty()) {
                            TextButton(onClick = { select(answer.dropLast(1)) }) { Text("COFNIJ STRZAŁKĘ") }
                        }
                    }
                    is WindReadings -> challenge.readings.forEachIndexed { index, reading ->
                        WindChoice(index, reading, challenge.readings.max(), index in answer) { select(listOf(index)) }
                    }
                    IceSorting -> {
                        Text(answer.map { IceSorting.labels[it] }.joinToString("  →  ").ifEmpty { "Dotknij pierwszego odłamka" },
                            color = Mint, fontSize = 15.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            IceSorting.labels.forEachIndexed { index, label ->
                                SmallAction("${listOf("🔹", "💎", "◇")[index]}\n$label", { select(answer + index) },
                                    Modifier.weight(1f), selected = index in answer, enabled = index !in answer,
                                    size = 12, description = "$label odłamek")
                            }
                        }
                        if (answer.isNotEmpty()) {
                            TextButton(onClick = { select(emptyList()) }) { Text("UŁÓŻ OD NOWA") }
                        }
                    }
                }
                if (tried) Text("Jeszcze jedna próba? Możesz zmienić odpowiedź.", color = Gold, fontSize = 14.sp)
                if (hints > 0) {
                    Eyebrow("WSKAZÓWKA Z LABORATORIUM", SkyBlue)
                    HintPicture(challenge, hints)
                    Text(challenge.hints[hints - 1], color = Snow, fontSize = 14.sp)
                }
                if (hints < maxHints) {
                    SmallAction("💡 PODPOWIEDŹ ${hints + 1}/$maxHints", { hints++ }, Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Action("SPRAWDŹ", {
                if (challenge.accepts(answer)) onSolved(answer.toList()) else tried = true
            }, color = tint, enabled = when (challenge) {
                is Batteries -> answer.size == 2
                IceSorting -> answer.size == 3
                else -> answer.isNotEmpty()
            })
        },
        dismissButton = { TextButton(onClick = onClose) { Text("WRÓĆ") } },
    )
}

@Composable
private fun ConsoleBadge(tint: Color, modifier: Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / 40f
        sceneAt(0f, 0f, unit) {
            outlinedTile(tint, 4f, 6f, 32f, 28f, 7f)
            tile(Midnight, 8f, 10f, 24f, 15f)
            stripe(Mint, 11f, 19f, 16f, 19f)
            stripe(Mint, 16f, 19f, 19f, 15f)
            stripe(Mint, 19f, 15f, 23f, 19f)
            stripe(Mint, 23f, 19f, 29f, 19f)
            drawCircle(Gold, 1.8f, Offset(26f, 29f))
        }
    }
}

@Composable
private fun HintPicture(challenge: Challenge, level: Int) {
    val picture = when (challenge) {
        is Batteries -> {
            val known = challenge.charges.max()
            val missing = if (level == 3) (challenge.target - known).toString() else "?"
            "🔋 $known   +   🔋 $missing   =   ${challenge.target} ✦"
        }
        is CrystalPattern -> {
            val period = (1..challenge.shown.size).first { length ->
                challenge.shown.indices.all { challenge.shown[it] == challenge.shown[it % length] }
            }
            val group = challenge.shown.take(period).joinToString(" ") { it.glyph }
            "$group   │   $group   ↻"
        }
        is Mirrors -> if (level == 3) {
            if (challenge.receiverAtTop) "↑   /   →   /   ↑   🟡" else "↑   /   →   \\   ↓   🟡"
        } else "🔵   ↑   /   →   ?"
        is DroneRoute -> {
            val sideways = if (challenge.reverse) "←" else "→"
            listOf(sideways, "↑", sideways, "↑", sideways).take(2 * level - 1).joinToString("   ")
        }
        is WindReadings -> if (challenge.strongest) "▰▰▰▰   >   ▰" else "▰   <   ▰▰▰▰"
        IceSorting -> if (level == 3) "◇   →   🔹   →   💎" else "◇   →   ?   →   ?"
    }
    Text(picture, Modifier.fillMaxWidth().background(Midnight, RoundedCornerShape(8.dp)).padding(6.dp),
        color = Mint, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
}

@Composable
private fun MirrorBoard(challenge: Mirrors, answer: List<Int>) {
    val beam = challenge.trace(answer)
    val reached = beam.last() == challenge.receiver
    Canvas(Modifier.fillMaxWidth().height(130.dp).background(Midnight, RoundedCornerShape(12.dp))
        .spoken("Plansza światła. Czujnik jest ${if (challenge.receiverAtTop) "u góry" else "na dole"} po prawej stronie.")) {
        val dx = size.width / 4
        val dy = size.height / 4
        fun center(cell: Cell) = Offset((cell.x + .5f) * dx, (cell.y + .5f) * dy)
        for (i in 1..3) {
            drawLine(Rim, Offset(i * dx, 0f), Offset(i * dx, size.height))
            drawLine(Rim, Offset(0f, i * dy), Offset(size.width, i * dy))
        }
        for ((from, to) in beam.zipWithNext()) {
            drawLine(Gold, center(from), center(to), 4.dp.toPx(), StrokeCap.Round)
        }
        drawCircle(SkyBlue, 7.dp.toPx(), center(challenge.emitter))
        drawCircle(if (reached) Mint else Gold, 9.dp.toPx(), center(challenge.receiver))
        challenge.mirrors.forEachIndexed { index, cell ->
            val middle = center(cell)
            val half = Offset(11.dp.toPx(), 9.dp.toPx() * if (answer[index] == 0) -1 else 1)
            drawLine(Snow, middle - half, middle + half, 4.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun DroneBoard(challenge: DroneRoute, path: List<Cell>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(challenge.height) { y ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(challenge.width) { x ->
                    val cell = Cell(x, y)
                    val tint = when {
                        cell in challenge.barriers -> Coral.copy(alpha = .28f)
                        cell in path -> Mint.copy(alpha = .29f)
                        else -> Midnight
                    }
                    val glyph = when (cell) {
                        challenge.start -> "🤖"
                        challenge.target -> "📦"
                        in challenge.barriers -> "✕"
                        else -> "·"
                    }
                    val label = when (cell) {
                        challenge.start -> "Dron"
                        challenge.target -> "Skrzynka"
                        in challenge.barriers -> "Przeszkoda"
                        else -> "Wolne pole"
                    }
                    val shape = RoundedCornerShape(5.dp)
                    Box(Modifier.weight(1f).height(29.dp).background(tint, shape).border(1.dp, Rim, shape)
                        .spoken("$label. Rząd ${y + 1}, kolumna ${x + 1}."), contentAlignment = Alignment.Center) {
                        Text(glyph, color = Snow, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WindChoice(index: Int, reading: Int, max: Int, selected: Boolean, choose: () -> Unit) {
    val label = ('A' + index).toString()
    OutlinedButton(onClick = choose, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        .spoken("Czujnik $label, wynik $reading"), contentPadding = PaddingValues(horizontal = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(if (selected) "✓ $label" else label, color = Snow, fontWeight = FontWeight.Black)
            Box(Modifier.weight(1f).height(12.dp).background(Midnight, RoundedCornerShape(5.dp))) {
                Box(Modifier.fillMaxWidth(reading.toFloat() / max).fillMaxHeight()
                    .background(if (selected) Mint else SkyBlue, RoundedCornerShape(5.dp)))
            }
            Text("$reading", color = Snow, textAlign = TextAlign.End)
        }
    }
}
