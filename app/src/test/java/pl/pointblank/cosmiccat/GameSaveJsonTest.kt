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
}
