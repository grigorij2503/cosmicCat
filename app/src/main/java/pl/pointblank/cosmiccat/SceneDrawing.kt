package pl.pointblank.cosmiccat

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp

internal fun Color.mix(other: Color, fraction: Float) = lerp(this, other, fraction)

internal fun DrawScope.sceneAt(x: Float, y: Float, scale: Float, draw: DrawScope.() -> Unit) =
    withTransform({ translate(x, y); scale(scale, scale, Offset.Zero) }, draw)

internal fun DrawScope.tile(color: Color, x: Float, y: Float, w: Float, h: Float, radius: Float = 4f) =
    drawRoundRect(color, Offset(x, y), Size(w, h), CornerRadius(radius))

internal fun DrawScope.ellipse(color: Color, x: Float, y: Float, w: Float, h: Float) =
    drawOval(color, Offset(x, y), Size(w, h))

internal fun DrawScope.stripe(color: Color, x: Float, y: Float, endX: Float, endY: Float, width: Float = 2f) =
    drawLine(color, Offset(x, y), Offset(endX, endY), width, StrokeCap.Round)

internal fun DrawScope.outlinedTile(tint: Color, x: Float, y: Float, w: Float, h: Float, radius: Float) {
    val at = Offset(x, y)
    val size = Size(w, h)
    val corners = CornerRadius(radius)
    drawRoundRect(Brush.verticalGradient(listOf(tint.mix(Snow, .36f), tint.mix(Midnight, .38f)), y, y + h),
        at, size, corners)
    drawRoundRect(Midnight, at, size, corners, style = Stroke(2.2f))
}

internal fun DrawScope.shape(color: Color, outline: Float = 0f, path: Path.() -> Unit) {
    val figure = Path().apply(path)
    drawPath(figure, color)
    if (outline > 0f) drawPath(figure, Midnight, style = Stroke(outline, join = StrokeJoin.Round))
}

internal fun DrawScope.twinkle(color: Color, x: Float, y: Float, radius: Float) = shape(color) {
    moveTo(x, y - radius)
    quadraticTo(x + radius * .15f, y - radius * .15f, x + radius, y)
    quadraticTo(x + radius * .15f, y + radius * .15f, x, y + radius)
    quadraticTo(x - radius * .15f, y + radius * .15f, x - radius, y)
    quadraticTo(x - radius * .15f, y - radius * .15f, x, y - radius)
    close()
}
