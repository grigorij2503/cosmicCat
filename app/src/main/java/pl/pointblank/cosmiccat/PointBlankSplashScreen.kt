package pl.pointblank.cosmiccat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val orbitron = FontFamily(
    Font(R.font.orbitron),
    Font(
        resId = R.font.orbitron,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(900)),
    ),
)

@Composable
internal fun PointBlankSplashScreen(onAnimationFinished: () -> Unit) {
    val assembly = remember { Animatable(0f) }
    val captionAlpha = remember { Animatable(0f) }
    val particles = remember { logoParticles() }
    var showCaption by remember { mutableStateOf(false) }
    val finish by rememberUpdatedState(onAnimationFinished)

    LaunchedEffect(Unit) {
        assembly.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        showCaption = true
        captionAlpha.animateTo(1f, tween(400))
        delay(800)
        finish()
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(colorResource(R.color.point_blank_splash_background)),
    ) {
        Canvas(Modifier.matchParentSize()) {
            // Keep the Planszowsky proportions without clipping the wordmark on smaller displays.
            val unit = minOf(size.width / 1080f, size.height / 1040f)
            val pixel = Size(8f * unit, 8f * unit)
            val glow = Size(12f * unit, 12f * unit)
            val fraction = assembly.value
            particles.forEach { particle ->
                val at = center + (particle.start + (particle.target - particle.start) * fraction) * unit
                if (fraction > .9f) {
                    drawRect(particle.color.copy(alpha = .4f), at - Offset(2f * unit, 2f * unit), glow)
                }
                drawRect(particle.color, at, pixel)
            }
        }

        if (showCaption) {
            Column(
                Modifier.align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 12.dp, vertical = if (maxHeight < 390.dp) 26.dp else 80.dp)
                    .graphicsLayer { alpha = captionAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.point_blank_studio),
                    color = Color.White,
                    fontFamily = orbitron,
                    fontSize = 14.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.point_blank_production),
                    color = Color.Gray,
                    fontFamily = orbitron,
                    fontSize = 10.sp,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private data class LogoParticle(val start: Offset, val target: Offset, val color: Color)

private fun logoParticles(): List<LogoParticle> = buildList {
    fun word(text: String, startX: Float, color: Color) {
        text.forEachIndexed { letter, char ->
            pixelLetters.getValue(char).forEachIndexed { y, row ->
                row.forEachIndexed { x, pixel ->
                    if (pixel == 'X') {
                        val start = Offset(Random.nextInt(-1500, 1500).toFloat(), Random.nextInt(-1500, 1500).toFloat())
                        val target = Offset(startX + letter * 70f + x * 12f, y * 12f - 50f)
                        add(LogoParticle(start, target, color))
                    }
                }
            }
        }
    }
    word("POINT-", -400f, Color(0xFFFF1744))
    word("BLANK", 50f, Color(0xFF00E5FF))
}

private val pixelLetters = mapOf(
    'P' to listOf("XXXX ", "X   X", "XXXX ", "X    ", "X    "),
    'O' to listOf(" XXX ", "X   X", "X   X", "X   X", " XXX "),
    'I' to listOf(" XXX ", "  X  ", "  X  ", "  X  ", " XXX "),
    'N' to listOf("X   X", "XX  X", "X X X", "X  XX", "X   X"),
    'T' to listOf("XXXXX", "  X  ", "  X  ", "  X  ", "  X  "),
    '-' to listOf("     ", "     ", " XXX ", "     ", "     "),
    'B' to listOf("XXXX ", "X   X", "XXXX ", "X   X", "XXXX "),
    'L' to listOf("X    ", "X    ", "X    ", "X    ", "XXXXX"),
    'A' to listOf(" XXX ", "X   X", "XXXXX", "X   X", "X   X"),
    'K' to listOf("X  X ", "X X  ", "XX   ", "X X  ", "X  X "),
)
