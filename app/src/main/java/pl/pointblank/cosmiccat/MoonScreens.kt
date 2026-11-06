package pl.pointblank.cosmiccat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MissionBriefingScreen(state: Progress, arrival: Destination?, assets: AssetManager, game: GameViewModel) {
    GamePage("02  /  BAZA NA KSIĘŻYCU", "Port Kosmiczny", "CatX czeka na Ciebie przy lądowisku.", state.coins) {
        if (arrival == Destination.MOON) Tag(state.cat.gender.say("Doleciałeś do bazy!", "Doleciałaś do bazy!"), SkyBlue)
        PanelCard(Modifier.fillMaxWidth(), Lavender) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("ODPRAWA  /  MISJA ${state.level}", Lavender)
                Tag(state.mission.destination.title, colorOf(state.mission.destination.color))
            }
            if (state.expedition > 1) Eyebrow("WYPRAWA ${state.expedition}")
            MoonComic(state, assets, state.mission.discovery.command)
            Action("RUSZ NA MISJĘ", { game.open(ScreenState.Navigation()) }, Modifier.fillMaxWidth())
        }
        Eyebrow("ODKRYWAJ SWOJĄ BAZĘ")
        HubLink("Budowa Portu Kosmicznego", "Budynki, bonusy i goście w bazie", "⌂", Mint, ScreenState.Spaceport, game)
        HubLink("Warsztat Pojazdów", "Złóż rakietę i łazik", "🚀", SkyBlue, ScreenState.Workshop, game)
        HubLink("Album Przyjaciół", "Poznaj kosmitów i ciekawostki", "✦", Coral, ScreenState.Album, game)
        SmallAction("ZMIEŃ KOTKA", { game.open(ScreenState.Creator(editing = true)) }, Modifier.fillMaxWidth())
        if (state.buildings.any { it.value > 0 } || state.friends.isNotEmpty()) SpaceportHubScreen(state, assets)
        PrivacyPolicyLink(Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun HubLink(title: String, subtitle: String, symbol: String, color: Color, route: ScreenState, game: GameViewModel) {
    val shape = RoundedCornerShape(17.dp)
    androidx.compose.material3.Surface(onClick = { game.open(route) }, modifier = Modifier.fillMaxWidth(),
        shape = shape, color = Panel, border = androidx.compose.foundation.BorderStroke(1.dp, Rim)) {
        Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(39.dp).background(color.copy(alpha = .14f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Text(symbol, fontSize = 23.sp, color = color)
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, color = Snow, fontSize = 16.sp)
                Text(subtitle, color = Mist, fontSize = 13.sp)
            }
            Text("›", fontSize = 28.sp, color = color)
        }
    }
}

@Composable
private fun MoonComic(state: Progress, assets: AssetManager, dialogue: String, celebrating: Boolean = false) {
    val cat = remember(state.cat) { assets.cat(state.cat) }
    val catX = remember { assets.catX() }
    val bubble = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 3.dp)
    val scene = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth().clip(scene).background(Midnight.copy(alpha = .65f))) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val glow = Offset(w * .65f, h * .55f)
            drawCircle(Brush.radialGradient(listOf(Lavender.copy(alpha = .19f), Color.Transparent), glow, w * .67f),
                w * .67f, glow)
            ellipse(Color(0xFF384774), -w * .29f, h * .71f, w * 1.55f, h * .53f)
            ellipse(Color(0xFF263458), -w * .22f, h * .81f, w * 1.38f, h * .34f)
            ellipse(Midnight.copy(alpha = .5f), w * .12f, h * .90f, w * .17f, h * .04f)
            ellipse(Midnight.copy(alpha = .45f), w * .68f, h * .88f, w * .14f, h * .035f)
            val skyTint = if (celebrating) Gold else Lavender
            for (i in 0..11) {
                val x = w * ((i * 39 + 8) % 100) / 100
                val y = h * ((i * 17 + 13) % 72) / 100
                if (i % 3 == 0) twinkle(skyTint.copy(alpha = .74f), x, y, 4.dp.toPx())
                else drawCircle(SkyBlue.copy(alpha = .65f), 1.1.dp.toPx(), Offset(x, y))
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (celebrating) Tag("MISJA GOTOWA", Mint)
                Sprite(cat, Modifier.fillMaxWidth().height(116.dp))
                Text(state.cat.name, color = Mint, fontSize = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            }
            Column(Modifier.weight(1.12f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().background(Gold, bubble).padding(horizontal = 9.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center) {
                    Text(dialogue, color = Midnight, fontSize = 15.sp, lineHeight = 18.sp,
                        fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                }
                Sprite(catX, Modifier.fillMaxWidth().height(116.dp), "Dowódca CatX w czerwonym skafandrze ze złotą odznaką")
                Text("Dowódca CatX", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DiscoverySummaryScreen(state: Progress, assets: AssetManager, game: GameViewModel) {
    val mission = state.mission
    val color = colorOf(mission.destination.color)
    GamePage("05  /  ODKRYCIE", "Kosmiczne odkrycie!", coins = state.coins) {
        PreviewStage(Modifier.fillMaxWidth().height(231.dp), mission.destination.title, color) {
            val item = remember(mission.discovery) { assets.discovery(mission.discovery) }
            Sprite(item, Modifier.size(208.dp), mission.discovery.item)
        }
        PanelCard(Modifier.fillMaxWidth(), Gold) {
            Text(mission.discovery.result(state.cat.gender), color = Gold,
                fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black)
            Text(mission.discovery.nextStep, color = Snow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        FactCard(mission.destination)
        Action("WRÓĆ DO RAKIETY", { game.open(ScreenState.Navigation(returning = true)) }, Modifier.fillMaxWidth())
        if (state.location == mission.destination && state.stageAt(mission.destination) == TrailStage.COMPLETE) {
            SmallAction("POSZUKAJ JESZCZE SEKRETÓW", { game.open(ScreenState.Exploration) }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun FactCard(destination: Destination) {
    PanelCard(Modifier.fillMaxWidth(), SkyBlue) {
        Eyebrow("✦  CIEKAWOSTKA", SkyBlue)
        Text(destination.fact, color = Snow, fontSize = 16.sp, lineHeight = 22.sp)
    }
}

@Composable
fun RewardScreen(state: Progress, assets: AssetManager, game: GameViewModel) {
    GamePage("07  /  NAGRODA", "Brawo, załogo!", "CatX ma dla Ciebie nagrodę.", state.coins) {
        PanelCard(Modifier.fillMaxWidth(), Gold) {
            MoonComic(state, assets, "BRAWO!", celebrating = true)
        }
        PanelCard(Modifier.fillMaxWidth(), Gold) {
            Text("NAGRODA: $REWARD MONET", color = Gold, fontSize = 26.sp,
                fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            CoinJar(Modifier.fillMaxWidth().height(133.dp))
            Text("Nowa misja już czeka. Monety możesz wydać w Porcie Kosmicznym.", color = Mist,
                fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        }
        Action("DO PORTU", game::closeReward, Modifier.fillMaxWidth(), enabled = !game.busy)
    }
}

@Composable
private fun CoinJar(modifier: Modifier) {
    val animation = remember { Animatable(0f) }
    LaunchedEffect(Unit) { animation.animateTo(1f, tween(1750)) }
    Canvas(modifier) {
        val cx = size.width / 2
        val h = size.height
        val unit = h / 140
        val jar = Path().apply {
            moveTo(cx - 26 * unit, h * .52f)
            cubicTo(cx - 23 * unit, h * .69f, cx - 56 * unit, h * .71f, cx - 50 * unit, h * .89f)
            quadraticTo(cx - 45 * unit, h * .98f, cx, h * .98f)
            quadraticTo(cx + 45 * unit, h * .98f, cx + 50 * unit, h * .89f)
            cubicTo(cx + 56 * unit, h * .71f, cx + 23 * unit, h * .69f, cx + 26 * unit, h * .52f)
            close()
        }
        drawPath(jar, Brush.verticalGradient(listOf(Mint, Color(0xFF448DC1)), h * .5f, h))
        drawPath(jar, Snow.copy(alpha = .55f), style = Stroke(1.5.dp.toPx()))
        drawRoundRect(Midnight, Offset(cx - 26 * unit, h * .49f), Size(52 * unit, 10 * unit), CornerRadius(5 * unit))
        drawRoundRect(SkyBlue, Offset(cx - 16 * unit, h * .77f), Size(32 * unit, 12 * unit), CornerRadius(6 * unit))
        repeat(REWARD) { index ->
            val t = (animation.value * 2f - index * .105f).coerceIn(0f, 1f)
            if (t > 0f && t < 1f) {
                val x = cx + sin(PI.toFloat() * t + index) * (1 - t) * size.width * .34f
                val y = h * (.02f + .52f * t * t)
                drawCircle(Gold, 10 * unit, Offset(x, y))
                drawCircle(Color(0xFFA3602F), 7 * unit, Offset(x, y), style = Stroke(unit))
                drawLine(Snow, Offset(x, y - 4 * unit), Offset(x, y + 4 * unit), 2 * unit)
            }
        }
    }
}
