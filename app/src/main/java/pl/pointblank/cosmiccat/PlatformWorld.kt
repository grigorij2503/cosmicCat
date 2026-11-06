package pl.pointblank.cosmiccat

import kotlin.math.abs

internal const val WORLD_HEIGHT = 410f
internal const val GROUND = 322f

internal data class Rock(val left: Float, val width: Float, val height: Float) {
    val right get() = left + width
    val top get() = GROUND - height
}

internal class PlatformWorld(
    private val vehicle: Vehicle,
    private val trail: Trail,
    private val hangar: Int = 0,
    stage: TrailStage = TrailStage.FIRST_SUPPLY,
    startAtGoal: Boolean = false,
) {
    var x = if (startAtGoal) trail.goalX else trail.resumeAt(stage)
        private set
    var feet = GROUND
        private set
    var facing = 1
        private set
    var rover = false
    var gateOpen = stage >= TrailStage.GOAL
    var left = false
    var right = false
    private var velocityY = 0f
    private var grounded = true
    private var airJumpUsed = false
    private var jumpBuffer = 0f
    private var tapDirection = 0
    private var tapTime = 0f

    fun jump() { jumpBuffer = .19f }

    fun nudge(direction: Int) {
        tapDirection = direction
        tapTime = .17f
    }

    fun step(dt: Float) {
        val held = (if (right) 1 else 0) - (if (left) 1 else 0)
        val direction = if (held != 0) held else if (tapTime > 0) tapDirection else 0
        tapTime -= dt
        if (direction != 0) facing = direction
        if (jumpBuffer > 0f && grounded) {
            velocityY = if (!rover && vehicle.boots == Boots.JET) -403f else -348f
            jumpBuffer = 0f
            grounded = false
        } else if (jumpBuffer > 0f && hangar > 0 && !rover && !airJumpUsed) {
            velocityY = -(252f + 35f * (hangar - 1))
            jumpBuffer = 0f
            airJumpUsed = true
        }
        jumpBuffer -= dt
        val radius = if (rover) 25f else 16f
        val speed = if (!rover) 225f else when (vehicle.wheels) {
            Wheels.CLASSIC -> 300f
            Wheels.GRIP -> 330f
            Wheels.MOON -> 365f
        }
        var nextX = (x + direction * speed * dt).coerceIn(30f, trail.width - 30f)
        if (!gateOpen) {
            val edge = trail.gateX - radius - 14f
            nextX = nextX.coerceAtMost(edge)
        }
        for (rock in trail.obstacles) {
            if (feet > rock.top + .1f && nextX + radius > rock.left && nextX - radius < rock.right) {
                if (x + radius <= rock.left && direction > 0) nextX = rock.left - radius
                if (x - radius >= rock.right && direction < 0) nextX = rock.right + radius
            }
        }
        x = nextX
        velocityY += 490f * dt
        val nextFeet = feet + velocityY * dt
        var floor = GROUND
        for (rock in trail.obstacles) {
            if (x + radius > rock.left && x - radius < rock.right && feet <= rock.top + .1f) {
                floor = minOf(floor, rock.top)
            }
        }
        grounded = nextFeet >= floor && velocityY >= 0f
        feet = if (grounded) floor else nextFeet
        if (grounded) {
            velocityY = 0f
            airJumpUsed = false
        }
    }

    fun touches(at: Float, width: Float = 44f): Boolean =
        abs(x - at) < width && feet >= GROUND - 63f

    fun atSecret(): Boolean =
        grounded && abs(x - trail.secretX) < 45f && abs(feet - trail.secretRock.top) < 2f
}
