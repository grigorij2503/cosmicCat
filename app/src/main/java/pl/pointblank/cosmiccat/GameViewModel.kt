package pl.pointblank.cosmiccat

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = GameStore(application.filesDir)

    var progress by mutableStateOf(Progress())
        private set
    var screen by mutableStateOf<ScreenState>(ScreenState.Loading)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var arrival by mutableStateOf<Destination?>(null)
        private set
    internal var recovery by mutableStateOf<GameLoad.Recovery?>(null)
        private set
    internal var exploration by mutableStateOf<ExplorationSession?>(null)
        private set

    init { load() }

    fun load() = loadOrRecover()

    fun recoverSave() {
        recovery?.let { loadOrRecover(it) }
    }

    private fun loadOrRecover(choice: GameLoad.Recovery? = null) {
        if (screen != ScreenState.Loading || busy) return
        busy = true
        error = null
        recovery = null
        viewModelScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) {
                    if (choice == null) store.load() else GameLoad.Ready(store.recover(choice))
                }
                when (loaded) {
                    is GameLoad.Ready -> {
                        progress = loaded.progress
                        navigate(progress.checkpoint())
                    }
                    is GameLoad.Recovery -> recovery = loaded
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: UnsupportedSaveVersion) {
                error = "Ten zapis pochodzi z nowszej wersji gry. Zaktualizuj aplikację."
            } catch (_: Exception) {
                error = if (choice == null) "Nie udało się otworzyć zapisu gry."
                    else "Nie udało się odzyskać zapisu. Spróbuj ponownie."
            } finally {
                busy = false
            }
        }
    }

    private fun navigate(next: ScreenState) {
        exploration = if (next == ScreenState.Exploration) exploration ?: ExplorationSession(progress) else null
        screen = next
    }

    private fun update(
        message: String = "Zapis się nie udał. Spróbuj jeszcze raz.",
        next: (Progress) -> ScreenState = { screen },
        change: (Progress) -> Progress,
    ) {
        if (busy) return
        busy = true
        error = null
        val current = progress
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) { store.update(current, change) }
                progress = saved
                navigate(next(saved))
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                error = message
                val route = screen
                if (route is ScreenState.Flight) navigate(ScreenState.Navigation(route.returning))
            } finally {
                busy = false
            }
        }
    }

    fun dismissError() { error = null }

    fun saveCat(cat: CatStyle) = update(next = { ScreenState.Hub }) {
        it.copy(started = true, cat = cat.copy(name = cat.name.trim().ifBlank { CatStyle().name }))
    }

    fun saveVehicle(vehicle: Vehicle) = update(next = { ScreenState.Hub }) { it.copy(vehicle = vehicle) }

    fun importPhoto(slot: PhotoSlot, uri: Uri) = update("Nie udało się wczytać obrazka. Wybierz inny obrazek.") {
        val photo = importSprite(getApplication(), uri)
        when (slot) {
            PhotoSlot.CAT -> it.copy(cat = it.cat.copy(photo = photo))
            PhotoSlot.ROCKET -> it.copy(vehicle = it.vehicle.copy(photo = photo))
        }
    }

    fun removePhoto(slot: PhotoSlot) = update {
        when (slot) {
            PhotoSlot.CAT -> it.copy(cat = it.cat.copy(photo = null))
            PhotoSlot.ROCKET -> it.copy(vehicle = it.vehicle.copy(photo = null))
        }
    }

    fun open(screen: ScreenState) {
        if (!busy) {
            error = null
            navigate(screen)
        }
    }

    fun launch(destination: Destination) {
        val route = screen as? ScreenState.Navigation ?: return
        open(ScreenState.Flight(destination, route.returning))
    }

    fun land(flight: ScreenState.Flight) {
        if (screen != flight) return
        update(next = { state ->
            arrival = flight.destination
            state.checkpoint()
        }) { it.landAt(flight.destination) }
    }

    fun takeSupply(destination: Destination, stage: TrailStage) {
        if (screen == ScreenState.Exploration) update { it.takeSupply(destination, stage) }
    }

    fun solveStation(destination: Destination, answer: List<Int>) {
        if (screen == ScreenState.Exploration) update { it.solveStation(destination, answer) }
    }

    fun finishTrail(destination: Destination) {
        if (screen != ScreenState.Exploration) return
        update(next = Progress::checkpoint) { it.finishTrail(destination) }
    }

    fun replayTrail(destination: Destination) {
        if (screen == ScreenState.Exploration) update { it.replayTrail(destination) }
    }

    fun collectSticker(destination: Destination) {
        if (screen == ScreenState.Exploration) update { it.collectSticker(destination) }
    }

    fun collectSeed(destination: Destination) {
        if (screen == ScreenState.Exploration) update { it.collectSeed(destination) }
    }

    fun tendPlant(destination: Destination) {
        if (screen == ScreenState.Greenhouse) update { it.tendPlant(destination) }
    }

    fun fulfillRequest(alien: Alien, answer: List<Int>) {
        if (screen == ScreenState.FriendsHomes) update { it.fulfillRequest(alien, answer) }
    }

    fun buy(building: Building) = update { it.build(building) }

    fun closeReward() = update(next = { ScreenState.Hub }) { it.copy(rewardPending = false) }

    fun back() {
        if (busy) return
        when (val current = screen) {
            ScreenState.Hub, ScreenState.Loading, ScreenState.Reward -> Unit
            is ScreenState.Creator -> if (current.editing) open(ScreenState.Hub)
            is ScreenState.Flight -> open(ScreenState.Navigation(current.returning))
            is ScreenState.Navigation -> open(progress.checkpoint())
            ScreenState.Exploration, ScreenState.DiscoverySummary -> open(ScreenState.Navigation(progress.found))
            ScreenState.Greenhouse, ScreenState.FriendsHomes -> open(ScreenState.Spaceport)
            else -> open(ScreenState.Hub)
        }
    }
}
