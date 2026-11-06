package pl.pointblank.cosmiccat

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

val Midnight = Color(0xFF0C1230)
val Panel = Color(0xFF202B51)
val Rim = Color(0xFF495782)
val Mist = Color(0xFFBAC9EE)
val Snow = Color(0xFFFFF8ED)
val Gold = Color(0xFFFFD675)
val Mint = Color(0xFF86F1D1)
val Coral = Color(0xFFFF9BB7)
val Lavender = Color(0xFFD5BFFF)
val SkyBlue = Color(0xFF92DCFF)

fun colorOf(rgb: Int) = Color(ink(rgb))
fun PaintColor.color() = colorOf(rgb)

@Composable
fun CosmicTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(
        primary = Gold, onPrimary = Midnight,
        secondary = Mint, onSecondary = Midnight,
        surface = Panel, onSurface = Snow, onSurfaceVariant = Mist,
        surfaceVariant = Panel, surfaceContainerHigh = Panel,
        outline = Rim, background = Midnight, onBackground = Snow,
        error = Coral, onError = Midnight,
    ),
    content = content,
)

private val stars = Random(261).let { random ->
    List(82) { Triple(random.nextFloat(), random.nextFloat(), random.nextFloat()) }
}

@Composable
fun Starfield(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Brush.verticalGradient(listOf(Midnight, Color(0xFF182344), Color(0xFF251C48))))) {
        drawCircle(Brush.radialGradient(listOf(Color(0x555854B1), Color.Transparent), Offset(size.width, size.height * .34f),
            size.width * .83f), size.width * .83f, Offset(size.width, size.height * .34f))
        stars.forEachIndexed { index, (x, y, shine) ->
            val at = Offset(x * size.width, y * size.height)
            val tint = if (index % 3 == 0) Lavender else SkyBlue
            if (index % 13 == 0) {
                val r = (3 + 2 * shine).dp.toPx()
                drawLine(tint.copy(alpha = .64f), at - Offset(r, 0f), at + Offset(r, 0f), 1.dp.toPx(), StrokeCap.Round)
                drawLine(tint.copy(alpha = .64f), at - Offset(0f, r), at + Offset(0f, r), 1.dp.toPx(), StrokeCap.Round)
            } else drawCircle(tint.copy(alpha = .21f + shine * .4f), (.6f + shine * 1.3f).dp.toPx(), at)
        }
    }
}

@Composable
fun GamePage(
    section: String,
    title: String,
    subtitle: String? = null,
    coins: Int? = null,
    back: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 690.dp).fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 9.dp, bottom = if (footer != null) 10.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MissionRail(coins = coins, back = back)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Eyebrow(section, Mint)
                    Text(title, fontSize = 36.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, color = Snow)
                    if (subtitle != null) Text(subtitle, fontSize = 16.sp, lineHeight = 21.sp, color = Mist)
                }
                content()
            }
            if (footer != null) {
                Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 9.dp)) { footer() }
            }
        }
    }
}

@Composable
fun MissionRail(coins: Int? = null, back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        if (back != null) {
            OutlinedButton(onClick = back, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Rim)) {
                Text("‹", color = Snow, fontSize = 30.sp,
                    modifier = Modifier.spoken("Wróć"))
            }
        }
        Text("✦  COSMIC CAT", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 14.sp,
            color = Gold, modifier = Modifier.weight(1f))
        if (coins != null) Tag("●  $coins", Gold, description = "Monety: $coins")
    }
}

@Composable
fun Eyebrow(text: String, color: Color = Mist) {
    Text(text, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp, color = color)
}

@Composable
internal fun PrivacyPolicyLink(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val url = stringResource(R.string.privacy_policy_url)
    TextButton(
        modifier = modifier,
        onClick = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.privacy_policy_open_error, Toast.LENGTH_SHORT).show()
            }
        },
    ) {
        Text(stringResource(R.string.privacy_policy), color = SkyBlue)
    }
}

@Composable
fun Tag(text: String, color: Color = Mint, description: String = text) {
    Box(Modifier.background(color.copy(alpha = .13f), RoundedCornerShape(50))
        .border(1.dp, color.copy(alpha = .36f), RoundedCornerShape(50))
        .padding(horizontal = 11.dp, vertical = 5.dp)) {
        Text(text, color = color, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.spoken(description))
    }
}

@Composable
fun PanelCard(
    modifier: Modifier = Modifier,
    accent: Color = Rim,
    padding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(21.dp)
    Column(modifier.background(Panel.copy(alpha = .93f), shape).border(1.dp, accent.copy(alpha = .68f), shape)
        .padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun Action(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Gold,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Midnight),
        shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
        Text(label, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

@Composable
fun SmallAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    description: String = label,
    size: Int = 14,
    tint: Color = Color.Unspecified,
) {
    OutlinedButton(onClick = onClick, enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Mint else Rim),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) Mint.copy(alpha = .19f) else Panel,
            contentColor = if (selected) Mint else Snow),
    ) {
        Text(label, fontSize = size.sp, lineHeight = (size + 4).sp, fontWeight = FontWeight.Bold, color = tint,
            textAlign = TextAlign.Center, modifier = Modifier.spoken(description))
    }
}

@Composable
fun Sprite(bitmap: ImageBitmap, modifier: Modifier = Modifier, description: String? = null) {
    Image(bitmap, contentDescription = description, modifier = modifier)
}

fun Modifier.spoken(description: String): Modifier = semantics {
    contentDescription = description
}
