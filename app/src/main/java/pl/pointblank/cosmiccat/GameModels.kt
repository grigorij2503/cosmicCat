package pl.pointblank.cosmiccat

import java.text.Normalizer
import java.util.Locale

enum class Gender {
    BOY, GIRL;

    fun say(boy: String, girl: String) = if (this == BOY) boy else girl
}

enum class PaintColor(val label: String, val rgb: Int) {
    ORANGE("Pomarańcz", 0xF39B5E), MINT("Mięta", 0x65DCAD),
    PURPLE("Fiolet", 0xAB90FB), WHITE("Biel", 0xFFF5E1),
    GRAY("Szary", 0xA9B6CD), BLACK("Czarny", 0x424764),
    BLUE("Błękit", 0x66CBFA), PINK("Róż", 0xF781B1),
    RED("Czerwień", 0xF86871), GOLD("Złoto", 0xFFD272),
}

val furColors = PaintColor.entries.take(6)
val hairColors = listOf(PaintColor.ORANGE, PaintColor.MINT, PaintColor.PURPLE, PaintColor.BLACK)
val suitColors = listOf(PaintColor.PURPLE, PaintColor.BLUE, PaintColor.PINK, PaintColor.MINT, PaintColor.RED, PaintColor.GOLD)

enum class Hair(val label: String) { NONE("Brak"), TUFTS("Kitki"), BRAIDS("Warkocze"), LONG("Długie") }
enum class Nose(val label: String) { ROUND("Okrągły"), POINTED("Szpic"), DOUBLE("Podwójny") }
enum class Fins(val label: String) { WIDE("Szerokie"), SWEPT("Strzałki"), PODS("Boczne silniki") }
enum class Wheels(val label: String) { CLASSIC("Klasyczne"), GRIP("Terenowe"), MOON("Księżycowe") }
enum class Boots(val label: String) { CLASSIC("Klasyczne"), JET("Sprężynowe") }
enum class PhotoSlot { CAT, ROCKET }

data class CatStyle(
    val name: String = "COSMIC CAT",
    val gender: Gender = Gender.BOY,
    val fur: PaintColor = PaintColor.ORANGE,
    val hair: Hair = Hair.NONE,
    val hairColor: PaintColor = PaintColor.PURPLE,
    val suit: PaintColor = PaintColor.PURPLE,
    val photo: String? = null,
)

data class Vehicle(
    val nose: Nose = Nose.ROUND,
    val fins: Fins = Fins.WIDE,
    val paint: PaintColor = PaintColor.BLUE,
    val wheels: Wheels = Wheels.CLASSIC,
    val boots: Boots = Boots.CLASSIC,
    val photo: String? = null,
)

enum class Destination(
    val label: String, val color: Int, val ground: Int, val sky: Int,
    val fact: String, val cloudStation: Boolean = false,
) {
    MOON("Księżyc", 0xCDD7F1, 0xA2AACD, 0x222D61, "Ślady astronautów mogą pozostać na Księżycu bardzo długo, bo nie ma tam wiatru ani deszczu."),
    MARS("Mars", 0xFF855E, 0xCE694E, 0x582F57, "Mars jest czerwony, bo minerały żelaza w jego pyle rdzewieją. Podobnie rdzewieje mokry rower!"),
    PLUTO("Pluton", 0xFFDB8E, 0x7AC8F0, 0x293C76, "Pluton jest planetą karłowatą. Część jego powierzchni pokrywa zamarznięty azot."),
    VENUS("Wenus", 0xFFB76E, 0xC78B59, 0x634365, "Wenus obraca się w przeciwną stronę niż Ziemia. Słońce wschodzi tam na zachodzie!"),
    JUPITER("Jowisz", 0xEDAF90, 0xD4958B, 0x6B476C, "Jowisz to gazowy olbrzym bez stałej powierzchni. Jego Wielka Czerwona Plama to ogromna burza.", true),
    SATURN("Saturn", 0xF4CA8D, 0xD8A87C, 0x584F78, "Pierścienie Saturna składają się z mnóstwa kawałków lodu i skał.", true),
    NEPTUNE("Neptun", 0x7DA8FF, 0x698AD4, 0x293E83, "Na Neptunie wieją najszybsze wiatry w Układzie Słonecznym. Badamy je z powietrza, bo planeta nie ma stałej powierzchni.", true);

    val title get() = label.uppercase(Locale.ROOT)

    companion object {
        private fun normalize(input: String): String = Normalizer
            .normalize(input.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('ł', 'l')

        fun parse(input: String) = entries.firstOrNull { normalize(it.label) == normalize(input) }
    }
}

enum class Discovery(val item: String, val command: String, val nextStep: String) {
    METEOR("ZŁOTY METEORYT", "ZNAJDŹ ZŁOTY METEORYT", "2. DAJ GO CATXOWI"),
    ICE("PRÓBKĘ LODU", "ZBIERZ PRÓBKĘ LODU", "2. DAJ JĄ CATXOWI"),
    TELESCOPE("TELESKOP", "NAPRAW TELESKOP", "2. OPOWIEDZ O TYM CATXOWI"),
    FRIEND("KOSMITĘ", "ZAPRZYJAŹNIJ SIĘ Z KOSMITĄ", "2. OPOWIEDZ O TYM CATXOWI"),
    WIND("WIATR", "ZMIERZ PRĘDKOŚĆ WIATRU", "2. POKAŻ WYNIK CATXOWI"),
    RING("FRAGMENT LODU", "ZNAJDŹ LÓD Z PIERŚCIENI", "2. DAJ GO CATXOWI");

    fun result(gender: Gender): String {
        val verb = when (this) {
            TELESCOPE -> gender.say("NAPRAWIŁEŚ", "NAPRAWIŁAŚ")
            FRIEND -> gender.say("POZNAŁEŚ", "POZNAŁAŚ")
            WIND -> gender.say("ZBADAŁEŚ", "ZBADAŁAŚ")
            else -> gender.say("ZNALAZŁEŚ", "ZNALAZŁAŚ")
        }
        return "$verb $item!"
    }
}

data class Mission(val destination: Destination, val discovery: Discovery)

val missions = listOf(
    Mission(Destination.MARS, Discovery.METEOR), Mission(Destination.PLUTO, Discovery.ICE),
    Mission(Destination.VENUS, Discovery.TELESCOPE), Mission(Destination.JUPITER, Discovery.FRIEND),
    Mission(Destination.NEPTUNE, Discovery.WIND), Mission(Destination.SATURN, Discovery.RING),
)

internal val missionsByDestination = missions.associateBy(Mission::destination)

enum class Alien(val home: Destination, val label: String, val color: PaintColor) {
    MIKI(Destination.MARS, "Miki", PaintColor.MINT),
    PUSZEK(Destination.PLUTO, "Puszek", PaintColor.PINK),
    ISKRA(Destination.VENUS, "Iskra", PaintColor.GOLD),
    KROPKA(Destination.JUPITER, "Kropka", PaintColor.PURPLE),
    BABEL(Destination.NEPTUNE, "Bąbel", PaintColor.BLUE);

    companion object {
        fun on(destination: Destination) = entries.firstOrNull { it.home == destination }
    }
}

enum class Building(val label: String, val symbol: String, val color: PaintColor, val price: Int) {
    LAB("Laboratorium Naukowe", "⚗", PaintColor.MINT, 10),
    HANGAR("Hangar Rakietowy", "🚀", PaintColor.BLUE, 12),
    GREENHOUSE("Kosmiczna Szklarnia", "🌱", PaintColor.GOLD, 12),
    HOMES("Domki dla Kosmitów", "🏠", PaintColor.PINK, 24),
    TOWER("Wieża Obserwacyjna", "🔭", PaintColor.PURPLE, 24);
}

const val REWARD = 12
const val MAX_LEVEL = 3

data class Progress(
    val started: Boolean = false,
    val cat: CatStyle = CatStyle(),
    val vehicle: Vehicle = Vehicle(),
    val location: Destination = Destination.MOON,
    val completed: Int = 0,
    val found: Boolean = false,
    val rewardPending: Boolean = false,
    val coins: Int = 0,
    val buildings: Map<Building, Int> = emptyMap(),
    val friends: Set<Alien> = emptySet(),
    val visited: Set<Destination> = emptySet(),
    val trailStages: Map<Destination, TrailStage> = emptyMap(),
    val stickers: Set<Destination> = emptySet(),
    val seeds: Set<Destination> = emptySet(),
    val plants: Map<Destination, PlantStage> = emptyMap(),
    val requests: Set<Alien> = emptySet(),
    val activeMission: Destination = missions.first().destination,
) {
    val mission get() = missionsByDestination.getValue(activeMission)
    val level get() = missions.indexOf(mission) + 1
    val expedition get() = completed / missions.size + 1

    fun landAt(destination: Destination): Progress {
        val landed = copy(location = destination, visited = visited + destination)
        return if (destination == Destination.MOON && found) {
            landed.copy(completed = completed + 1, found = false, rewardPending = true, coins = coins + REWARD,
                trailStages = trailStages - activeMission, activeMission = missions[level % missions.size].destination)
        } else landed
    }

    fun price(building: Building) = building.price + 6 * (buildings[building] ?: 0)

    fun build(building: Building): Progress {
        val level = buildings[building] ?: 0
        val price = price(building)
        return if (level < MAX_LEVEL && coins >= price) {
            copy(coins = coins - price, buildings = buildings + (building to level + 1))
        } else this
    }
}

internal fun Progress.checkpoint(): ScreenState = when {
    !started -> ScreenState.Creator()
    rewardPending -> ScreenState.Reward
    location == Destination.MOON -> ScreenState.Hub
    found && location == activeMission -> ScreenState.DiscoverySummary
    else -> ScreenState.Exploration
}

sealed interface ScreenState {
    data object Loading : ScreenState
    data class Creator(val editing: Boolean = false) : ScreenState
    data object Hub : ScreenState
    data class Navigation(val returning: Boolean = false) : ScreenState
    data class Flight(val destination: Destination, val returning: Boolean) : ScreenState
    data object Exploration : ScreenState
    data object DiscoverySummary : ScreenState
    data object Reward : ScreenState
    data object Spaceport : ScreenState
    data object Workshop : ScreenState
    data object Album : ScreenState
    data object Greenhouse : ScreenState
    data object FriendsHomes : ScreenState
}
