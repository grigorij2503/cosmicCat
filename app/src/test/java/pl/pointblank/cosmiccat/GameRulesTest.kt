package pl.pointblank.cosmiccat

import org.junit.Assert.*
import org.junit.Test

class GameRulesTest {
    private val mars = trails.getValue(Destination.MARS)
    @Test
    fun destinationAcceptsMixedCaseAndPolishKeyboardVariants() {
        assertEquals(Destination.MOON, Destination.parse("  KSiĘŻyC  "))
        assertEquals(Destination.MOON, Destination.parse("ksiezyc"))
        assertEquals(Destination.MARS, Destination.parse("mars "))
        assertEquals(Destination.PLUTO, Destination.parse("PLUTON"))
        assertNull(Destination.parse("  "))
        assertNull(Destination.parse("Marss"))
    }

    @Test
    fun missionRewardIsAddedOnceOnReturningToMoon() {
        val discovered = Progress(found = true, location = Destination.MARS)
        val detour = discovered.landAt(Destination.PLUTO)
        assertTrue(detour.found)
        assertEquals(0, detour.coins)
        val paid = detour.landAt(Destination.MOON)
        assertEquals(REWARD, paid.coins)
        assertTrue(paid.rewardPending)
        assertFalse(paid.found)
        assertEquals(Destination.PLUTO, paid.mission.destination)
        assertEquals(paid, paid.landAt(Destination.MOON))
    }

    @Test
    fun detourAfterDiscoveryResumesExplorationAndKeepsTheRewardPendingUntilLandingAtBase() {
        val discovered = Progress(started = true, found = true, location = Destination.MARS)
        assertEquals(ScreenState.DiscoverySummary, discovered.checkpoint())
        val detour = discovered.landAt(Destination.VENUS)
        assertEquals(ScreenState.Exploration, detour.checkpoint())
        assertEquals(ScreenState.Exploration, GameSaveJson.decode(GameSaveJson.encode(detour)).checkpoint())
        assertEquals(ScreenState.DiscoverySummary, detour.landAt(Destination.MARS).checkpoint())
        val returned = detour.landAt(Destination.MOON)
        assertEquals(ScreenState.Reward, returned.checkpoint())
        assertEquals(REWARD, returned.coins)
    }

    @Test
    fun bothGendersGetTheCorrectMeteoriteMessage() {
        assertEquals("ZNALAZŁEŚ ZŁOTY METEORYT!", Discovery.METEOR.result(Gender.BOY))
        assertEquals("ZNALAZŁAŚ ZŁOTY METEORYT!", Discovery.METEOR.result(Gender.GIRL))
    }

    @Test
    fun upgradesCheckBalanceAndLevelCap() {
        val building = Building.LAB
        val broke = Progress(coins = 9)
        assertEquals(broke, broke.build(building))
        val initial = Progress(coins = 50)
        val level1 = initial.build(building)
        val level2 = level1.build(building)
        val level3 = level2.build(building)
        assertEquals(40, level1.coins)
        assertEquals(24, level2.coins)
        assertEquals(2, level3.coins)
        assertEquals(MAX_LEVEL, level3.buildings[building])
        assertEquals(level3, level3.build(building))
    }

    @Test
    fun rockBlocksRunningButCanBeClearedByJumping() {
        val world = PlatformWorld(Vehicle(), mars)
        world.right = true
        repeat(120) { world.step(1f / 60) }
        assertEquals(mars.obstacles.first().left - 16f, world.x, .1f)
        world.jump()
        repeat(90) { world.step(1f / 60) }
        assertTrue(world.x > mars.obstacles.first().right)
        assertEquals(GROUND, world.feet, .1f)
    }

    @Test
    fun upgradedBootsJumpHigherAndRoverMovesFaster() {
        val classic = PlatformWorld(Vehicle(), mars)
        val boots = PlatformWorld(Vehicle(boots = Boots.JET), mars)
        classic.jump()
        boots.jump()
        repeat(37) {
            classic.step(1f / 60)
            boots.step(1f / 60)
        }
        assertTrue(boots.feet < classic.feet - 20f)
        val runner = PlatformWorld(Vehicle(), mars)
        val rover = PlatformWorld(Vehicle(wheels = Wheels.MOON), mars).apply { this.rover = true }
        runner.right = true
        rover.right = true
        repeat(16) {
            runner.step(1f / 60)
            rover.step(1f / 60)
        }
        assertTrue(rover.x > runner.x + 25f)
    }
}
