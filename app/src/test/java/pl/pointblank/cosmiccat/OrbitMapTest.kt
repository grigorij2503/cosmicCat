package pl.pointblank.cosmiccat

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.*
import org.junit.Test

class OrbitMapTest {
    @Test
    fun smallMapKeepsBodiesOnScreenAtEachInclinationAndRotation() {
        for (tilt in listOf(.37f, .65f, .83f)) {
            for (angle in 0..360 step 30) {
                val projection = OrbitProjection(Size(288f, 284f), 1f, OrbitView(angle.toFloat(), tilt, 2f))
                for ((body, point) in projection.positions) {
                    val radius = body.diameter / 2
                    assertTrue("${body.name} at $angle", point.x in radius..(288f - radius))
                    assertTrue("${body.name} at $angle", point.y in radius..(284f - radius))
                }
                val earth = projection.positions.getValue(OrbitBody.EARTH)
                val moon = projection.positionOf(Destination.MOON)
                assertTrue((moon - earth).getDistance() in 18.9f..24.1f)
            }
        }
    }

    @Test
    fun touchChoosesTheNearestBodyAndLeavesDecorativeBodiesInert() {
        val projection = OrbitProjection(Size(288f, 284f), 1f, OrbitView())
        assertEquals(Destination.MOON, projection.destinationAt(projection.positionOf(Destination.MOON)))
        assertEquals(Destination.MARS, projection.destinationAt(projection.positionOf(Destination.MARS) + Offset(6f, 0f)))
        assertNull(projection.destinationAt(projection.positions.getValue(OrbitBody.EARTH)))
        assertNull(projection.destinationAt(Offset.Zero))
        assertEquals(Destination.entries.toSet(), orbitalLandingSites.toSet())
    }

    @Test
    fun flightStartsAtTheCurrentBodyAndEndsAtTheTargetOnTheRotatedMap() {
        val view = OrbitView(angle = 135f, inclination = .76f, time = 1f)
        val projection = OrbitProjection(Size(310f, 284f), 1f, view)
        val moon = projection.positionOf(Destination.MOON)
        val mars = projection.positionOf(Destination.MARS)
        val outward = OrbitFlight(moon, mars, 55f)
        val returnFlight = OrbitFlight(mars, moon, 55f)

        assertEquals(moon, outward.point(0f))
        assertEquals(mars, outward.point(1f))
        assertEquals(mars, returnFlight.point(0f))
        assertEquals(moon, returnFlight.point(1f))
        assertTrue(outward.point(.5f).y < (moon.y + mars.y) / 2)
    }
}
