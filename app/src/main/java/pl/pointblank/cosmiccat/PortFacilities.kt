package pl.pointblank.cosmiccat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal fun Building.perk(level: Int) = when (this) {
    Building.LAB -> if (level == 1) "Jedna wskazówka w każdej zagadce." else "$level wskazówki w każdej zagadce."
    Building.HANGAR -> when (level) {
        1 -> "Dodatkowy skok w powietrzu."
        2 -> "Silniejszy dodatkowy skok."
        else -> "Najmocniejszy dodatkowy skok."
    }
    Building.GREENHOUSE -> if (level == 1) "Jedna grządka na kosmiczne nasiona." else "$level grządki na kosmiczne nasiona."
    Building.HOMES -> "Zadania od przyjaciół i ${level + 2} monety za każde."
    Building.TOWER -> when (level) {
        1 -> "Wskazuje strefę z ukrytą naklejką."
        2 -> "Podaje dokładniejszą wskazówkę o skrytce."
        else -> "Zaznacza skrytkę migającą gwiazdką."
    }
}

@Composable
fun GreenhouseScreen(state: Progress, game: GameViewModel) {
    val capacity = state.portLevel(Building.GREENHOUSE)
    val active = state.plants.values.count { it != PlantStage.BLOOM }
    GamePage("BAZA  /  SZKLARNIA", "Kosmiczny ogród",
        "Siej nasiona z wypraw i podlewaj je, aż zakwitną.", state.coins, game::back) {
        Box(Modifier.fillMaxWidth().height(143.dp)) {
            GardenScene(state, Modifier.matchParentSize())
            Box(Modifier.align(Alignment.TopStart).padding(9.dp)) { Tag("GRZĄDKI  $active / $capacity", Mint) }
        }
        if (state.seeds.isEmpty()) {
            PanelCard(Modifier.fillMaxWidth(), Mint) {
                Text("Wypatruj zielonych sadzonek za bramami na planetach.", color = Snow, fontSize = 16.sp)
            }
        } else {
            Eyebrow("ROŚLINY Z WYPRAW", Mint)
        }
        trails.values.filter { it.destination in state.seeds }.forEach { trail ->
            val plant = state.plants[trail.destination]
            PanelCard(Modifier.fillMaxWidth(), Mint) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    PortraitStage(Mint, Modifier.size(70.dp)) {
                        PlantArt(trail.destination, plant, Modifier.size(63.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(trail.plant, fontSize = 19.sp, fontWeight = FontWeight.Black, color = Snow)
                        val phase = when (plant) {
                            null -> "Nasiono"
                            PlantStage.SPROUT -> "Kiełek"
                            PlantStage.BUD -> "Pąk"
                            PlantStage.BLOOM -> "Kwitnie"
                        }
                        Text("$phase · ${trail.destination.label}", fontSize = 13.sp, color = Mist)
                    }
                }
                when {
                    plant == PlantStage.BLOOM -> Tag("ZAKWITŁA  ·  3 MONETY", Mint)
                    plant == null && active >= capacity -> Text("Podlej rosnącą roślinę, aby zwolnić grządkę.",
                        fontSize = 14.sp, color = Mist)
                    else -> Action(if (plant == null) "ZASADŹ NASIONO" else "PODLEJ",
                        { game.tendPlant(trail.destination) }, Modifier.fillMaxWidth(), Mint, enabled = !game.busy)
                }
            }
        }
    }
}

@Composable
fun FriendsHomesScreen(state: Progress, assets: AssetManager, game: GameViewModel) {
    var selected by rememberSaveable { mutableStateOf<Alien?>(null) }
    val reward = 2 + state.portLevel(Building.HOMES)
    GamePage("BAZA  /  DOMKI", "Prośby przyjaciół",
        "Odwiedź kosmitów i pomóż im w krótkich zadaniach.", state.coins, game::back) {
        if (state.friends.isEmpty()) {
            PanelCard(Modifier.fillMaxWidth(), Coral) {
                HabitatArt(Coral, Modifier.fillMaxWidth().height(110.dp))
                Text("Rozwiąż zagadkę kosmity na jego planecie. Potem zajrzyj do niego tutaj!", fontSize = 16.sp,
                    color = Snow, textAlign = TextAlign.Center)
            }
        }
        Alien.entries.filter { it in state.friends }.forEach { alien ->
            val challenge = homeChallenge(alien)
            val tint = alien.color.color()
            PanelCard(Modifier.fillMaxWidth(), tint) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    PortraitStage(tint, Modifier.size(111.dp, 86.dp)) {
                        HabitatArt(tint, Modifier.align(Alignment.CenterStart).size(81.dp, 79.dp))
                        Sprite(remember(alien) { assets.alien(alien) },
                            Modifier.align(Alignment.BottomEnd).size(59.dp).padding(3.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(alien.label, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Snow)
                        Text(challenge.title, fontSize = 15.sp, color = Mist)
                    }
                }
                if (alien in state.requests) Tag("✓  POMOC UDZIELONA", Mint)
                else Action("POMÓŻ  ·  $reward ${if (reward < 5) "MONETY" else "MONET"}", { selected = alien },
                    Modifier.fillMaxWidth(), tint, enabled = !game.busy)
            }
        }
    }
    selected?.let { alien ->
        ChallengeDialog(homeChallenge(alien), alien, assets, state.portLevel(Building.LAB), onClose = { selected = null }) {
            game.fulfillRequest(alien, it)
            selected = null
        }
    }
}
