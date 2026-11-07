package pl.pointblank.cosmiccat

import org.junit.Assert.*
import org.junit.Test

class GameSaveJsonTest {
    @Test
    fun versionOneSaveKeepsItsCharacterVehicleBuildingsAndPendingReward() {
        val old = GameSaveJson.decode("""
            {
              "started": true,
              "location": "MOON",
              "completed": 1,
              "found": false,
              "rewardPending": true,
              "coins": 12,
              "friends": ["MIKI"],
              "visited": ["MOON", "MARS"],
              "buildings": {"LAB": 1},
              "cat": {"name": "Luna", "gender": "GIRL", "fur": "PURPLE",
                "photo": "sprite-445b7987-19f8-401e-baa2-cf1066c5d0b4.png"},
              "vehicle": {"nose": "POINTED", "wheels": "GRIP"}
            }
        """.trimIndent())

        assertEquals("Luna", old.cat.name)
        assertEquals(Gender.GIRL, old.cat.gender)
        assertEquals(PaintColor.PURPLE, old.cat.fur)
        assertEquals("sprite-445b7987-19f8-401e-baa2-cf1066c5d0b4.png", old.cat.photo)
        assertEquals(Nose.POINTED, old.vehicle.nose)
        assertEquals(Wheels.GRIP, old.vehicle.wheels)
        assertEquals(setOf(Alien.MIKI), old.friends)
        assertEquals(setOf(Destination.MOON, Destination.MARS), old.visited)
        assertEquals(1, old.portLevel(Building.LAB))
        assertEquals(12, old.coins)
        assertTrue(old.rewardPending)
        assertEquals(Destination.PLUTO, old.mission.destination)
        assertEquals(TrailStage.FIRST_SUPPLY, old.stageAt(Destination.MARS))
        assertTrue(old.plants.isEmpty())
    }

    @Test
    fun newMilestonesAndOptionalRewardsSurviveRoundTrip() {
        val saved = Progress(started = true, completed = 1, coins = 7, location = Destination.PLUTO,
            activeMission = Destination.PLUTO,
            buildings = Building.entries.associateWith { 1 }, friends = setOf(Alien.MIKI),
            trailStages = mapOf(Destination.PLUTO to TrailStage.PUZZLE, Destination.MARS to TrailStage.COMPLETE),
            stickers = setOf(Destination.MARS), seeds = setOf(Destination.MARS, Destination.VENUS),
            plants = mapOf(Destination.MARS to PlantStage.BLOOM, Destination.VENUS to PlantStage.BUD),
            requests = setOf(Alien.MIKI))
        assertEquals(saved, GameSaveJson.decode(GameSaveJson.encode(saved)))
    }

    @Test
    fun savedMissionIdControlsTheActiveMissionAndWhichTrailIsCleared() {
        val before = Progress(started = true, completed = 6, activeMission = Destination.VENUS,
            location = Destination.VENUS, found = true, trailStages = mapOf(
                Destination.MARS to TrailStage.COMPLETE, Destination.VENUS to TrailStage.COMPLETE,
            ))
        val decoded = GameSaveJson.decode(GameSaveJson.encode(before))
        assertEquals(Destination.VENUS, decoded.mission.destination)
        val after = decoded.landAt(Destination.MOON)
        assertEquals(Destination.JUPITER, after.activeMission)
        assertEquals(mapOf(Destination.MARS to TrailStage.COMPLETE), after.trailStages)
    }

    @Test
    fun versionTwoCampaignUnlocksContractsAndPersistsTheAssignmentAndPendingReward() {
        val old = GameSaveJson.decode("""
            {"schemaVersion":2,"started":true,"completed":6,"activeMission":"MARS","location":"MOON",
            "coins":17,"cat":{"name":"Luna","gender":"GIRL"},"stickers":["MARS"],"seeds":["SATURN"],
            "buildings":{"LAB":2},"trailStages":{"VENUS":"SECOND_SUPPLY"}}
        """.trimIndent())
        assertTrue(old.campaignComplete)
        assertNull(old.assignmentDestination)
        assertEquals("Luna", old.cat.name)
        assertEquals(17, old.coins)
        assertEquals(2, old.portLevel(Building.LAB))
        assertEquals(setOf(Destination.MARS), old.stickers)
        assertEquals(setOf(Destination.SATURN), old.seeds)
        val drawn = old.copy(activeContract = Contract.VENUS_BEACONS, finishedContracts = setOf(Contract.MARS_POWER))
        val landed = drawn.landAt(Destination.VENUS)
        assertEquals(landed, GameSaveJson.decode(GameSaveJson.encode(landed)))
        val solved = landed.solveStation(Destination.VENUS, listOf(0))
        val reloaded = GameSaveJson.decode(GameSaveJson.encode(solved))
        assertEquals(ScreenState.DiscoverySummary, reloaded.checkpoint())
        val paid = reloaded.landAt(Destination.MOON)
        assertEquals(TrailStage.SECOND_SUPPLY, paid.stageAt(Destination.VENUS))
        assertEquals(setOf(Contract.MARS_POWER, Contract.VENUS_BEACONS), paid.finishedContracts)
        assertEquals(paid, GameSaveJson.decode(GameSaveJson.encode(paid)))
    }
}
