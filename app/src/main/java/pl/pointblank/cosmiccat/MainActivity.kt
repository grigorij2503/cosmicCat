package pl.pointblank.cosmiccat

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        val assets = AssetManager(applicationContext)
        setContent { CosmicTheme { CosmicGame(assets) } }
    }
}

@Composable
private fun CosmicGame(assets: AssetManager, game: GameViewModel = viewModel()) {
    var introFinished by rememberSaveable { mutableStateOf(false) }
    if (!introFinished) {
        PointBlankSplashScreen { introFinished = true }
        return
    }

    val state = game.progress
    val screen = game.screen
    BackHandler(screen != ScreenState.Hub && screen != ScreenState.Creator()) { game.back() }
    Box(Modifier.fillMaxSize()) {
        Starfield(Modifier.matchParentSize())
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            when (screen) {
                ScreenState.Loading -> if (game.busy) CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ScreenState.Creator -> CharacterCreatorScreen(screen.editing, state.cat, assets, game)
                ScreenState.Hub -> MissionBriefingScreen(state, game.arrival, assets, game)
                is ScreenState.Navigation -> NavigationScreen(screen.returning, state, assets, game)
                is ScreenState.Flight -> FlightScreen(screen, state, assets, game)
                ScreenState.Exploration -> key(state.location) {
                    PlanetPlatformerScreen(state, assets, game, checkNotNull(game.exploration))
                }
                ScreenState.DiscoverySummary -> DiscoverySummaryScreen(state, assets, game)
                ScreenState.Reward -> RewardScreen(state, assets, game)
                ScreenState.Spaceport -> SpaceportBuilderScreen(state, game)
                ScreenState.Workshop -> VehicleBuilderScreen(state, assets, game)
                ScreenState.Album -> AlienFriendsScreen(state, assets, game)
                ScreenState.Greenhouse -> GreenhouseScreen(state, game)
                ScreenState.FriendsHomes -> FriendsHomesScreen(state, assets, game)
            }
        }
    }
    game.recovery?.let { recovery ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Zapis gry jest uszkodzony") },
            text = {
                Text(if (recovery.canRestore) "Możesz wrócić do poprzedniego zapisu."
                    else "Poprzedni zapis jest niedostępny. Rozpoczęcie nowej gry zachowa kopię uszkodzonego pliku.")
            },
            confirmButton = {
                TextButton(onClick = game::recoverSave) {
                    Text(if (recovery.canRestore) "ODZYSKAJ ZAPIS" else "ZACZNIJ OD NOWA")
                }
            },
            dismissButton = { TextButton(onClick = game::load) { Text("PONÓW ODCZYT") } },
        )
    }
    game.error?.let { error ->
        AlertDialog(
            onDismissRequest = { if (screen != ScreenState.Loading) game.dismissError() },
            title = { Text("Ups, mała usterka!") },
            text = { Text(error) },
            confirmButton = {
                val action = remember(screen) {
                    if (screen == ScreenState.Loading) game::load else game::dismissError
                }
                TextButton(onClick = action) { Text(if (screen == ScreenState.Loading) "SPRÓBUJ PONOWNIE" else "OKEJ") }
            },
        )
    }
}
