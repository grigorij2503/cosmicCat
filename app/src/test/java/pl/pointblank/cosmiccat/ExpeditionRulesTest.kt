package pl.pointblank.cosmiccat

import org.junit.Assert.*
import org.junit.Test

class ExpeditionRulesTest {
    private val mars = Destination.MARS
    private val pluto = Destination.PLUTO
    private val marsTrail = trails.getValue(mars)

    @Test
    fun marsRequiresBothSuppliesAndTheCorrectPuzzleAnswer() {
        val started = Progress(location = mars)
        assertEquals(started, started.takeSupply(mars, TrailStage.SECOND_SUPPLY))
        assertEquals(started, started.finishTrail(mars))
        val first = started.takeSupply(mars, TrailStage.FIRST_SUPPLY)
        assertEquals(first, first.takeSupply(mars, TrailStage.FIRST_SUPPLY))
        val second = first.takeSupply(mars, TrailStage.SECOND_SUPPLY)
        assertEquals(second, second.solveStation(mars, listOf(0, 1)))
        val solved = second.solveStation(mars, listOf(2, 1))
        assertEquals(TrailStage.GOAL, solved.stageAt(mars))
        assertTrue(Alien.MIKI in solved.friends)
        assertFalse(solved.found)
        val finished = solved.finishTrail(mars)
        assertTrue(finished.found)
        assertEquals(TrailStage.COMPLETE, finished.stageAt(mars))
        val returned = finished.landAt(Destination.MOON)
        assertEquals(REWARD, returned.coins)
        assertEquals(TrailStage.FIRST_SUPPLY, returned.stageAt(mars))
    }

    @Test
    fun revisitingMarsFromAnOldPlutoSaveDoesNotMoveTheCampaign() {
        val old = Progress(completed = 1, activeMission = pluto, coins = 2,
            friends = setOf(Alien.MIKI), buildings = mapOf(Building.LAB to 1))
        val landed = old.landAt(mars)
        val done = landed.takeSupply(mars, TrailStage.FIRST_SUPPLY).takeSupply(mars, TrailStage.SECOND_SUPPLY)
            .solveStation(mars, listOf(1, 2)).finishTrail(mars).collectSticker(mars).collectSeed(mars)
        assertFalse(done.found)
        assertEquals(1, done.portLevel(Building.LAB))
        assertEquals(TrailStage.COMPLETE, done.stageAt(mars))
        val replay = done.replayTrail(mars)
        assertEquals(TrailStage.FIRST_SUPPLY, replay.stageAt(mars))
        assertEquals(done.stickers, replay.stickers)
        assertEquals(done.seeds, replay.seeds)
        val returned = replay.landAt(Destination.MOON)
        assertEquals(2, returned.coins)
        assertEquals(pluto, returned.mission.destination)
        val reported = done.copy(completed = missions.size, activeMission = mars).finishTrail(mars)
        assertTrue(reported.found)
        assertEquals(2 + REWARD, reported.landAt(Destination.MOON).coins)
    }

    @Test
    fun eachStationAndHomeChallengeHasItsOwnValidSolution() {
        val drone = listOf(Move.RIGHT, Move.UP, Move.RIGHT, Move.UP, Move.RIGHT).map(Move::ordinal)
        val mirroredDrone = listOf(Move.LEFT, Move.UP, Move.LEFT, Move.UP, Move.LEFT).map(Move::ordinal)
        val solutions = mapOf(mars to listOf(1, 2), pluto to listOf(ShapeToken.CIRCLE.ordinal),
            Destination.VENUS to listOf(0, 1), Destination.JUPITER to drone,
            Destination.NEPTUNE to listOf(1), Destination.SATURN to listOf(2, 0, 1))
        for ((destination, solution) in solutions) {
            val challenge = stationChallenge(destination)
            assertTrue(destination.name, challenge.accepts(solution))
            assertFalse(destination.name, challenge.accepts(emptyList()))
        }
        val homeAnswers = mapOf(Alien.MIKI to listOf(1, 2), Alien.PUSZEK to listOf(ShapeToken.DIAMOND.ordinal),
            Alien.ISKRA to listOf(0, 0), Alien.KROPKA to mirroredDrone, Alien.BABEL to listOf(1))
        for ((alien, solution) in homeAnswers) {
            assertTrue(alien.name, homeChallenge(alien).accepts(solution))
        }
        assertFalse(stationChallenge(Destination.VENUS).accepts(listOf(0, 0)))
        assertFalse(stationChallenge(Destination.JUPITER).accepts(listOf(Move.UP.ordinal) + drone))
        assertFalse(stationChallenge(mars).accepts(listOf(2, 2)))
    }

    @Test
    fun gardenCapacityAndRewardsAreEnforcedAndPaidOnce() {
        val save = Progress(seeds = setOf(mars, pluto))
        assertEquals(save, save.tendPlant(mars))
        val garden = save.copy(buildings = mapOf(Building.GREENHOUSE to 1))
        val first = garden.tendPlant(mars)
        assertEquals(PlantStage.SPROUT, first.plants[mars])
        assertEquals(first, first.tendPlant(pluto))
        val bloom = first.tendPlant(mars).tendPlant(mars)
        assertEquals(PlantStage.BLOOM, bloom.plants[mars])
        assertEquals(3, bloom.coins)
        assertEquals(bloom, bloom.tendPlant(mars))
        assertEquals(PlantStage.SPROUT, bloom.tendPlant(pluto).plants[pluto])
    }

    @Test
    fun onlyOwnedHomesPayForSolvingAFriendsRequestOnce() {
        val save = Progress(friends = setOf(Alien.MIKI))
        assertEquals(save, save.fulfillRequest(Alien.MIKI, listOf(1, 2)))
        val houses = save.copy(buildings = mapOf(Building.HOMES to 2))
        assertEquals(houses, houses.fulfillRequest(Alien.BABEL, listOf(1)))
        assertEquals(houses, houses.fulfillRequest(Alien.MIKI, listOf(0, 1)))
        val paid = houses.fulfillRequest(Alien.MIKI, listOf(1, 2))
        assertEquals(4, paid.coins)
        assertEquals(paid, paid.fulfillRequest(Alien.MIKI, listOf(2, 1)))
    }

    @Test
    fun gateBlocksEvenAJetpackUntilTheConsoleUnlocksIt() {
        val world = PlatformWorld(Vehicle(), marsTrail, hangar = 3, stage = TrailStage.GOAL)
        world.gateOpen = false
        world.right = true
        repeat(90) { world.step(1f / 60) }
        world.jump()
        repeat(35) { world.step(1f / 60) }
        world.jump()
        repeat(25) { world.step(1f / 60) }
        assertTrue(world.x < marsTrail.gateX)
        world.gateOpen = true
        repeat(35) { world.step(1f / 60) }
        assertTrue(world.x > marsTrail.gateX)
    }

    @Test
    fun hangarAddsExactlyOneJumpBeforeLanding() {
        val standard = PlatformWorld(Vehicle(), marsTrail)
        val jetpack = PlatformWorld(Vehicle(), marsTrail, hangar = 1)
        val twice = PlatformWorld(Vehicle(), marsTrail, hangar = 1)
        val worlds = listOf(standard, jetpack, twice)
        worlds.forEach(PlatformWorld::jump)
        repeat(41) { worlds.forEach { it.step(1f / 60) } }
        worlds.forEach(PlatformWorld::jump)
        repeat(12) { worlds.forEach { it.step(1f / 60) } }
        assertTrue(jetpack.feet < standard.feet - 25f)
        jetpack.jump()
        repeat(12) { worlds.forEach { it.step(1f / 60) } }
        assertEquals(twice.feet, jetpack.feet, .1f)
    }

    @Test
    fun eachTrailAndItsSecretLedgeAreReachableWithoutUpgrades() {
        for (trail in trails.values) {
            val world = PlatformWorld(Vehicle(), trail).apply { gateOpen = true }
            assertTrue("${trail.destination} first supply", moveTo(world, trail.first.x) { world.touches(trail.first.x, 7f) })
            assertTrue("${trail.destination} second supply", moveTo(world, trail.second.x) { world.touches(trail.second.x, 7f) })
            assertTrue("${trail.destination} secret", moveTo(world, trail.secretX, world::atSecret))
            assertTrue("${trail.destination} goal", moveTo(world, trail.goalX) { world.touches(trail.goalX, 7f) })
        }
    }

    private fun moveTo(world: PlatformWorld, target: Float, arrived: () -> Boolean): Boolean {
        var previous = world.x
        var stuck = 0
        repeat(1400) {
            if (arrived()) return true
            world.left = world.x > target + 5f
            world.right = world.x < target - 5f
            if ((world.left || world.right) && world.x == previous) stuck++ else stuck = 0
            if (stuck == 3) world.jump()
            previous = world.x
            world.step(1f / 60)
        }
        return false
    }
}
