package pl.pointblank.cosmiccat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpaceportHubScreen(state: Progress, assets: AssetManager) {
    PanelCard(Modifier.fillMaxWidth(), Mint) {
        Eyebrow("✦  TWÓJ PORT KOSMICZNY", Mint)
        PortSkyline(state, Modifier.fillMaxWidth().height(113.dp))
        val blooms = state.plants.filterValues { it == PlantStage.BLOOM }.keys
        if (state.friends.isNotEmpty() || blooms.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                state.friends.sortedBy { it.ordinal }.forEach { alien ->
                    Sprite(remember(alien) { assets.alien(alien) }, Modifier.size(40.dp), "Gość: ${alien.label}")
                }
                blooms.sortedBy { it.ordinal }.forEach { destination ->
                    PlantArt(destination, PlantStage.BLOOM, Modifier.size(40.dp).spoken(trails.getValue(destination).plant))
                }
            }
        }
    }
}

@Composable
fun SpaceportBuilderScreen(state: Progress, game: GameViewModel) {
    GamePage("BAZA  /  BUDOWA", "Zbuduj swój port", "Wydaj monety na budynki. Każdy możesz ulepszyć do trzeciego poziomu.",
        state.coins, game::back) {
        Box(Modifier.fillMaxWidth().height(133.dp)) {
            PortSkyline(state, Modifier.matchParentSize())
            Box(Modifier.align(Alignment.TopStart).padding(9.dp)) {
                Tag("MODUŁY  ${state.buildings.count { it.value > 0 }} / ${Building.entries.size}", Mint)
            }
        }
        Building.entries.forEach { building ->
            val level = state.portLevel(building)
            val price = state.price(building)
            val tint = building.color.color()
            PanelCard(Modifier.fillMaxWidth(), tint) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    PortraitStage(tint, Modifier.size(90.dp, 72.dp)) {
                        BuildingArt(building, level, Modifier.fillMaxSize().padding(3.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(building.label, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Snow)
                        Text(if (level == 0) "Wolne miejsce" else "Poziom $level z $MAX_LEVEL", fontSize = 14.sp, color = Mist)
                        Row(Modifier.padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(MAX_LEVEL) { index ->
                                Box(Modifier.size(18.dp, 4.dp)
                                    .background(if (index < level) tint else Rim, RoundedCornerShape(50)))
                            }
                        }
                    }
                }
                Text(building.perk(level.coerceAtLeast(1)), fontSize = 14.sp, lineHeight = 19.sp,
                    color = if (level > 0) Mint else Mist)
                if (level > 0 && level < MAX_LEVEL) {
                    Text("Po ulepszeniu: ${building.perk(level + 1)}", fontSize = 12.sp, color = Mist)
                }
                when {
                    level == MAX_LEVEL -> Tag("✓  NAJWYŻSZY POZIOM", Mint)
                    state.coins < price -> Text("Potrzeba $price monet. Brakuje ${price - state.coins}.", color = Mist, fontSize = 14.sp)
                    else -> Action("${if (level == 0) "ZBUDUJ" else "ULEPSZ"}  ·  $price MONET",
                        { game.buy(building) }, Modifier.fillMaxWidth(), Mint, enabled = !game.busy)
                }
                if (level > 0) {
                    when (building) {
                        Building.GREENHOUSE -> SmallAction("WEJDŹ DO SZKLARNI",
                            { game.open(ScreenState.Greenhouse) }, Modifier.fillMaxWidth())
                        Building.HOMES -> SmallAction("ODWIEDŹ PRZYJACIÓŁ",
                            { game.open(ScreenState.FriendsHomes) }, Modifier.fillMaxWidth())
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleBuilderScreen(state: Progress, assets: AssetManager, game: GameViewModel) {
    val saved = state.vehicle
    var nose by rememberSaveable { mutableStateOf(saved.nose) }
    var fins by rememberSaveable { mutableStateOf(saved.fins) }
    var paint by rememberSaveable { mutableStateOf(saved.paint) }
    var wheels by rememberSaveable { mutableStateOf(saved.wheels) }
    var boots by rememberSaveable { mutableStateOf(saved.boots) }
    val draft = Vehicle(nose, fins, paint, wheels, boots, saved.photo)
    GamePage("BAZA  /  WARSZTAT", "Warsztat Pojazdów", "Złóż rakietę, łazik i wybierz buty do skakania.", state.coins, game::back) {
        val rocket = remember(draft, state.cat) { assets.rocket(draft, state.cat) }
        val rover = remember(draft.paint, draft.wheels) { assets.rover(draft) }
        PreviewStage(Modifier.fillMaxWidth().height(186.dp), "PODGLĄD NA ŻYWO", paint.color()) {
            Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Sprite(rocket, Modifier.weight(1f).height(151.dp), "Podgląd rakiety")
                Sprite(rover, Modifier.weight(1f).height(148.dp), "Podgląd łazika")
            }
        }
        PhotoActions(PhotoSlot.ROCKET, saved.photo != null, game)
        PanelCard(Modifier.fillMaxWidth()) {
            Eyebrow("RAKIETA", SkyBlue)
            PartSelector("Czubek", nose.label) { nose = Nose.entries.step(nose, it) }
            PartSelector("Skrzydła", fins.label) { fins = Fins.entries.step(fins, it) }
            PartSelector("Kolor kadłuba", paint.label) { paint = PaintColor.entries.step(paint, it) }
        }
        PanelCard(Modifier.fillMaxWidth()) {
            Eyebrow("ŁAZIK I BUTY", Mint)
            PartSelector("Koła łazika", wheels.label) { wheels = Wheels.entries.step(wheels, it) }
            PartSelector("Buty", boots.label) { boots = Boots.entries.step(boots, it) }
            Text("Łazik przyspiesza jazdę, a sprężynowe buty podnoszą wysokość skoku.", color = Mist, fontSize = 14.sp)
        }
        Action("ZAPISZ POJAZDY", { game.saveVehicle(draft) }, Modifier.fillMaxWidth(), enabled = !game.busy)
    }
}

@Composable
private fun PartSelector(label: String, value: String, change: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SmallAction("‹", { change(-1) }, Modifier.width(48.dp), description = "Poprzednia opcja. $label", size = 23)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Mist, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(value, color = Snow, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        SmallAction("›", { change(1) }, Modifier.width(48.dp), description = "Następna opcja. $label", size = 23)
    }
}

@Composable
fun AlienFriendsScreen(state: Progress, assets: AssetManager, game: GameViewModel) {
    GamePage("BAZA  /  ALBUM", "Kosmiczni przyjaciele", "Poznaj kosmitów i zapisuj ciekawostki z podróży.", state.coins, game::back) {
        Tag("PRZYJACIELE  ${state.friends.size} / ${Alien.entries.size}", Coral)
        PanelCard(Modifier.fillMaxWidth(), Lavender) {
            Eyebrow("KOLEKCJA NAKLEJEK  ${state.stickers.size} / ${trails.size}", Lavender)
            trails.values.chunked(3).forEach { strip ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    strip.forEach { trail ->
                        val collected = trail.destination in state.stickers
                        Column(Modifier.weight(1f).padding(vertical = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            StickerArt(trail.destination, Modifier.size(49.dp)
                                .spoken(if (collected) trail.sticker else "Nieodkryta naklejka"), collected)
                            Text(if (collected) trail.sticker else trail.destination.label,
                                color = if (collected) Snow else Mist, fontSize = 12.sp, lineHeight = 14.sp,
                                fontWeight = if (collected) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            if (state.stickers.isEmpty()) Text("Szukaj skrytek na wysokich półkach podczas wypraw.",
                fontSize = 13.sp, color = Mist)
        }
        Alien.entries.forEach { alien ->
            val known = alien in state.friends
            PanelCard(Modifier.fillMaxWidth(), alien.color.color()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    PortraitStage(alien.color.color(), Modifier.size(71.dp)) {
                        Sprite(remember(alien) { assets.alien(alien) }, Modifier.size(63.dp))
                    }
                    Column {
                        Text(alien.label, color = Snow, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(alien.home.label, color = Mist, fontSize = 14.sp)
                        Text(if (known) "✓ Przyjaciel w porcie" else "Czeka na spotkanie", color = if (known) Mint else Gold, fontSize = 13.sp)
                    }
                }
                if (alien.home in state.visited) Text(alien.home.fact, color = Mist, fontSize = 14.sp, lineHeight = 19.sp)
            }
        }
        if (state.visited.any { Alien.on(it) == null }) Eyebrow("Z NOTATNIKA PODRÓŻY")
        state.visited.filter { Alien.on(it) == null }.forEach { destination ->
            PanelCard(Modifier.fillMaxWidth()) {
                Eyebrow(destination.title, SkyBlue)
                Text(destination.fact, color = Snow, fontSize = 15.sp, lineHeight = 20.sp)
            }
        }
    }
}
