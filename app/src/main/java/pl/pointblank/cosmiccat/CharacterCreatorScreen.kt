package pl.pointblank.cosmiccat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal fun <T> List<T>.step(current: T, direction: Int = 1): T =
    this[(indexOf(current) + direction).mod(size)]

@Composable
fun CharacterCreatorScreen(editing: Boolean, saved: CatStyle, assets: AssetManager, game: GameViewModel) {
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var gender by rememberSaveable { mutableStateOf(saved.gender) }
    var fur by rememberSaveable { mutableStateOf(saved.fur) }
    var hair by rememberSaveable { mutableStateOf(saved.hair) }
    var hairColor by rememberSaveable { mutableStateOf(saved.hairColor) }
    var suit by rememberSaveable { mutableStateOf(saved.suit) }
    val cat = CatStyle(name, gender, fur, hair, hairColor, suit, saved.photo)
    val focus = LocalFocusManager.current

    GamePage("01  /  KREATOR", "Koto-astronauta", "Stwórz swojego kota i ruszaj w kosmos!",
        back = game::back.takeIf { editing },
        footer = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it.take(24) }, modifier = Modifier.weight(1f),
                    label = { Text("NAZWA:") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                )
                Action(if (editing) "ZAPISZ KOTKA" else "START!", {
                    focus.clearFocus()
                    game.saveCat(cat)
                }, Modifier.widthIn(min = 98.dp, max = 144.dp), enabled = !game.busy)
            }
        },
    ) {
        PreviewStage(Modifier.fillMaxWidth().height(208.dp), "TWÓJ ASTRONAUTA", suit.color()) {
            val bitmap = remember(cat.fur, cat.hair, cat.hairColor, cat.suit, cat.photo) { assets.cat(cat) }
            Sprite(bitmap, Modifier.size(186.dp), "Twój koto-astronauta")
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CreatorTile("FUTRO", Modifier.weight(1f).fillMaxHeight()) {
                ColorControl(fur, "futra") { fur = furColors.step(fur, it) }
            }
            CreatorTile("SKAFANDER", Modifier.weight(1f).fillMaxHeight()) {
                ColorControl(suit, "skafandra") { suit = suitColors.step(suit, it) }
            }
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CreatorTile("WŁOSY", Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallAction(hair.label, { hair = Hair.entries.step(hair) }, Modifier.weight(1.4f),
                        description = "Styl włosów: ${hair.label}. Zmień styl.", size = 12)
                    SmallAction("●", { hairColor = hairColors.step(hairColor) }, Modifier.weight(1f),
                        description = "Kolor włosów: ${hairColor.label}. Zmień kolor.", size = 21,
                        tint = if (hairColor == PaintColor.BLACK) Mist else hairColor.color())
                }
                Text("Kolor: ${hairColor.label}", fontSize = 11.sp, color = Mist, textAlign = TextAlign.Center)
            }
            CreatorTile("PŁEĆ", Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallAction("♂", { gender = Gender.BOY }, Modifier.weight(1f), selected = gender == Gender.BOY,
                        description = "Chłopiec", size = 22)
                    SmallAction("♀", { gender = Gender.GIRL }, Modifier.weight(1f), selected = gender == Gender.GIRL,
                        description = "Dziewczynka", size = 22)
                }
                Text(gender.say("Chłopiec", "Dziewczynka"), fontSize = 11.sp, color = Mist, textAlign = TextAlign.Center)
            }
        }
        PhotoActions(PhotoSlot.CAT, cat.photo != null, game)
        PrivacyPolicyLink(Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun CreatorTile(label: String, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(13.dp)
    Column(modifier.background(Panel, shape).border(1.dp, Rim, shape).padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Eyebrow(label, SkyBlue)
        content()
    }
}

@Composable
private fun ColorControl(color: PaintColor, part: String, change: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        SmallAction("‹", { change(-1) }, Modifier.weight(1f),
            description = "Poprzedni kolor $part", size = 24)
        Box(Modifier.weight(.66f), contentAlignment = Alignment.Center) {
            Box(Modifier.size(25.dp).background(color.color(), CircleShape).border(1.5.dp, Snow, CircleShape))
        }
        SmallAction("›", { change(1) }, Modifier.weight(1f),
            description = "Następny kolor $part", size = 24)
    }
    Text(color.label, color = Mist, fontSize = 11.sp, modifier = Modifier.spoken("Kolor $part: ${color.label}"))
}

@Composable
fun PreviewStage(modifier: Modifier, label: String, accent: Color, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Box(modifier.clip(shape).background(Panel.copy(alpha = .88f)).border(1.dp, Rim, shape)) {
        Canvas(Modifier.matchParentSize()) {
            val glow = Offset(size.width / 2, size.height * .49f)
            val radius = size.minDimension * .47f
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .28f), Color.Transparent), glow, radius), radius, glow)
            drawCircle(accent.copy(alpha = .22f), radius * .74f, glow, style = Stroke(1.dp.toPx()))
            drawCircle(accent.copy(alpha = .15f), radius * .93f, glow, style = Stroke(1.dp.toPx()))
            for ((x, y) in listOf(.2f to .33f, .78f to .28f, .79f to .71f, .25f to .74f, .87f to .49f)) {
                drawCircle(accent.copy(alpha = .73f), 2.dp.toPx(), Offset(size.width * x, size.height * y))
            }
        }
        Box(Modifier.align(Alignment.TopStart).padding(11.dp)) { Tag(label, accent) }
        Box(Modifier.matchParentSize().padding(top = 41.dp), contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun PhotoActions(slot: PhotoSlot, hasPhoto: Boolean, game: GameViewModel) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) game.importPhoto(slot, uri)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SmallAction("📷 Wgraj własny obrazek", {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }, Modifier.weight(1f), enabled = !game.busy)
        if (hasPhoto) SmallAction("Usuń", { game.removePhoto(slot) }, Modifier.widthIn(min = 57.dp), enabled = !game.busy,
            description = "Usuń własny obrazek")
    }
}
