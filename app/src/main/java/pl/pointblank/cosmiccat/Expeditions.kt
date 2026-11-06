package pl.pointblank.cosmiccat

enum class TrailStage { FIRST_SUPPLY, SECOND_SUPPLY, PUZZLE, GOAL, COMPLETE }

internal data class Supply(val x: Float, val name: String, val glyph: String)

internal data class Trail(
    val mission: Mission,
    val width: Float,
    val first: Supply,
    val second: Supply,
    val stationX: Float,
    val gateX: Float,
    val goalX: Float,
    val secretRock: Rock,
    val seedX: Float,
    val areas: List<String>,
    val sticker: String,
    val plant: String,
    val rocks: List<Rock>,
) {
    val destination get() = mission.destination
    val obstacles = (rocks + secretRock).sortedBy(Rock::left)
    val secretX get() = (secretRock.left + secretRock.right) / 2
    val challenge get() = stationChallenge(destination)
    val finalAction get() = if (mission.discovery == Discovery.FRIEND) "ODBIERZ SYGNAŁ DRONA" else mission.discovery.command

    fun areaAt(x: Float) = when {
        x < second.x - 125 -> 0
        x < gateX -> 1
        else -> 2
    }

    fun resumeAt(stage: TrailStage) = when (stage) {
        TrailStage.FIRST_SUPPLY, TrailStage.COMPLETE -> 108f
        TrailStage.SECOND_SUPPLY -> first.x
        TrailStage.PUZZLE -> second.x
        TrailStage.GOAL -> stationX
    }

    fun objective(stage: TrailStage) = when (stage) {
        TrailStage.FIRST_SUPPLY -> "1/3  ·  Szukaj: ${first.name}"
        TrailStage.SECOND_SUPPLY -> "1/3  ·  Szukaj: ${second.name}"
        TrailStage.PUZZLE -> "2/3  ·  Rozwiąż zagadkę przy bramie"
        TrailStage.GOAL -> "3/3  ·  $finalAction"
        TrailStage.COMPLETE -> "✓  Wyprawa ukończona. Rozejrzyj się za sekretem!"
    }

    fun secretHint(level: Int) = when (level) {
        1 -> "Wieża: sekret czeka w strefie „${areas[areaAt(secretX)]}”."
        2 -> "Wieża: wejdź na wysoką półkę z fioletową szczeliną w strefie „${areas[areaAt(secretX)]}”."
        else -> "Wieża: na planszy przy sekrecie miga fioletowa gwiazdka."
    }
}

internal val trails = listOf(
    Trail(missionsByDestination.getValue(Destination.MARS), 3550f, Supply(490f, "Bateria z 4 iskrami", "🔋"), Supply(1200f, "Bateria z 5 iskrami", "🔋"),
        1750f, 1990f, 3140f, Rock(2330f, 151f, 76f), 2710f,
        listOf("Czerwone lądowisko", "Warsztat Mikiego", "Krater Meteorytu"), "Korona krateru", "Marsowa stokrotka",
        listOf(Rock(266f, 65f, 34f), Rock(785f, 101f, 50f), Rock(1400f, 82f, 56f), Rock(2850f, 101f, 43f))),
    Trail(missionsByDestination.getValue(Destination.PLUTO), 3280f, Supply(410f, "Śnieżny kryształ", "❄"), Supply(1040f, "Lodowy kryształ", "◆"),
        1530f, 1780f, 2920f, Rock(1300f, 147f, 88f), 2560f,
        listOf("Zmarznięta równina", "Śluza Puszka", "Lodowe ogrody"), "Śnieżna kometa", "Lodowy dzwonek",
        listOf(Rock(220f, 81f, 37f), Rock(610f, 101f, 54f), Rock(820f, 69f, 45f), Rock(2650f, 89f, 64f))),
    Trail(missionsByDestination.getValue(Destination.VENUS), 3800f, Supply(530f, "Pierwsze lusterko", "◈"), Supply(1260f, "Drugie lusterko", "◈"),
        1790f, 2090f, 3410f, Rock(2650f, 174f, 83f), 3070f,
        listOf("Złote skały", "Pracownia Iskry", "Stary teleskop"), "Złote lusterko", "Wenusowa gwiazda",
        listOf(Rock(285f, 84f, 55f), Rock(858f, 111f, 65f), Rock(1480f, 71f, 39f), Rock(2280f, 93f, 49f), Rock(3190f, 80f, 43f))),
    Trail(missionsByDestination.getValue(Destination.JUPITER), 4000f, Supply(575f, "Śmigło drona", "✦"), Supply(1320f, "Nadajnik drona", "◉"),
        1880f, 2220f, 3630f, Rock(2760f, 165f, 87f), 3230f,
        listOf("Pomost lądowania", "Sterownia Kropki", "Taras burzowy"), "Mały dron", "Chmurowy groszek",
        listOf(Rock(325f, 120f, 52f), Rock(814f, 82f, 42f), Rock(1060f, 91f, 55f), Rock(1570f, 111f, 44f),
            Rock(2460f, 101f, 58f), Rock(3390f, 86f, 63f))),
    Trail(missionsByDestination.getValue(Destination.NEPTUNE), 3650f, Supply(470f, "Czujnik wiatru", "◉"), Supply(1190f, "Zapasowy akumulator", "🔋"),
        1720f, 2020f, 3310f, Rock(935f, 153f, 85f), 2900f,
        listOf("Błękitne molo", "Stacja Bąbla", "Taras wiatru"), "Wstążka wichru", "Wietrzny pączek",
        listOf(Rock(251f, 80f, 52f), Rock(767f, 117f, 60f), Rock(1410f, 80f, 52f), Rock(2230f, 95f, 47f), Rock(3080f, 89f, 57f))),
    Trail(missionsByDestination.getValue(Destination.SATURN), 3920f, Supply(520f, "Próbka z małym odłamkiem", "◇"), Supply(1230f, "Próbka z dużym odłamkiem", "◆"),
        1850f, 2150f, 3520f, Rock(2730f, 168f, 85f), 3140f,
        listOf("Złoty pomost", "Sortownia CatXa", "Okno na pierścienie"), "Srebrny pierścień", "Pierścieniowa róża",
        listOf(Rock(310f, 111f, 50f), Rock(843f, 110f, 55f), Rock(1475f, 114f, 64f), Rock(2400f, 101f, 41f),
            Rock(3310f, 92f, 57f))),
).associateBy(Trail::destination)

enum class PlantStage { SPROUT, BUD, BLOOM }

internal fun Progress.stageAt(destination: Destination) = trailStages[destination] ?: TrailStage.FIRST_SUPPLY
internal fun Progress.portLevel(building: Building) = buildings[building] ?: 0

internal fun Progress.takeSupply(destination: Destination, stage: TrailStage): Progress {
    if (location != destination || destination !in trails || stageAt(destination) != stage) return this
    val next = when (stage) {
        TrailStage.FIRST_SUPPLY -> TrailStage.SECOND_SUPPLY
        TrailStage.SECOND_SUPPLY -> TrailStage.PUZZLE
        else -> return this
    }
    return copy(trailStages = trailStages + (destination to next))
}

internal fun Progress.solveStation(destination: Destination, answer: List<Int>): Progress {
    val trail = trails[destination] ?: return this
    if (location != destination || stageAt(destination) != TrailStage.PUZZLE || !trail.challenge.accepts(answer)) return this
    val alien = Alien.on(destination)
    return copy(trailStages = trailStages + (destination to TrailStage.GOAL),
        friends = if (alien == null) friends else friends + alien)
}

internal fun Progress.finishTrail(destination: Destination): Progress {
    if (location != destination) return this
    val stage = stageAt(destination)
    if (stage == TrailStage.COMPLETE && mission.destination == destination) return copy(found = true)
    if (stage != TrailStage.GOAL) return this
    return copy(trailStages = trailStages + (destination to TrailStage.COMPLETE),
        found = found || mission.destination == destination)
}

internal fun Progress.replayTrail(destination: Destination): Progress =
    if (location == destination && stageAt(destination) == TrailStage.COMPLETE) {
        copy(trailStages = trailStages - destination)
    } else this

internal fun Progress.collectSticker(destination: Destination): Progress =
    if (location == destination && destination in trails) copy(stickers = stickers + destination) else this

internal fun Progress.collectSeed(destination: Destination): Progress =
    if (location == destination && destination in trails) copy(seeds = seeds + destination) else this

internal fun Progress.tendPlant(destination: Destination): Progress {
    val capacity = portLevel(Building.GREENHOUSE)
    if (location != Destination.MOON || capacity == 0 || destination !in seeds) return this
    val next = when (plants[destination]) {
        null -> {
            if (plants.values.count { it != PlantStage.BLOOM } >= capacity) return this
            PlantStage.SPROUT
        }
        PlantStage.SPROUT -> PlantStage.BUD
        PlantStage.BUD -> PlantStage.BLOOM
        PlantStage.BLOOM -> return this
    }
    return copy(plants = plants + (destination to next), coins = coins + if (next == PlantStage.BLOOM) 3 else 0)
}

internal fun Progress.fulfillRequest(alien: Alien, answer: List<Int>): Progress {
    val level = portLevel(Building.HOMES)
    if (location != Destination.MOON || level == 0 || alien !in friends || alien in requests ||
        !homeChallenge(alien).accepts(answer)) return this
    return copy(requests = requests + alien, coins = coins + 2 + level)
}
