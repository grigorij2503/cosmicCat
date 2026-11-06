package pl.pointblank.cosmiccat

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import kotlin.math.roundToInt

internal fun ink(rgb: Int) = rgb or 0xFF000000.toInt()

internal fun shade(color: Int, to: Int, amount: Float): Int {
    fun mix(from: Int, end: Int) = (from + (end - from) * amount).roundToInt()
    return Color.rgb(mix(Color.red(color), Color.red(to)), mix(Color.green(color), Color.green(to)),
        mix(Color.blue(color), Color.blue(to)))
}

internal class BitmapFactoryGenerator(private val canvas: Canvas) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val edge = ink(0x30334E)

    private fun fill(path: Path, color: Int, outline: Float, glow: RectF?) {
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.shader = glow?.let {
            RadialGradient(it.left + it.width() * .31f, it.top + it.height() * .24f,
                maxOf(it.width(), it.height()) * .84f,
                intArrayOf(shade(color, Color.WHITE, .58f), color, shade(color, edge, .52f)),
                floatArrayOf(0f, .53f, 1f), Shader.TileMode.CLAMP)
        }
        canvas.drawPath(path, paint)
        paint.shader = null
        if (outline > 0) {
            paint.color = edge
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = outline
            paint.strokeJoin = Paint.Join.ROUND
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
        }
    }

    fun oval(l: Float, t: Float, r: Float, b: Float, color: Int, border: Float = 0f, lit: Boolean = false) {
        val rect = RectF(l, t, r, b)
        fill(Path().apply { addOval(rect, Path.Direction.CW) }, color, border, rect.takeIf { lit })
    }

    fun round(l: Float, t: Float, r: Float, b: Float, radius: Float, color: Int, border: Float = 0f, lit: Boolean = false) {
        val rect = RectF(l, t, r, b)
        fill(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }, color, border, rect.takeIf { lit })
    }

    @Suppress("DEPRECATION")
    fun shape(color: Int, border: Float = 0f, lit: Boolean = false, draw: Path.() -> Unit) {
        val path = Path().apply(draw)
        val bounds = if (lit) RectF().apply { path.computeBounds(this, true) } else null
        fill(path, color, border, bounds)
    }

    fun stroke(color: Int, width: Float, draw: Path.() -> Unit) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(Path().apply(draw), paint)
        paint.style = Paint.Style.FILL
    }

    fun image(bitmap: Bitmap, rect: RectF) {
        paint.color = Color.WHITE
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    fun maskedImage(bitmap: Bitmap, rect: RectF, circular: Boolean) {
        val scale = if (circular) {
            maxOf(rect.width() / bitmap.width, rect.height() / bitmap.height)
        } else {
            minOf(rect.width() / bitmap.width, rect.height() / bitmap.height)
        }
        val halfWidth = bitmap.width * scale / 2
        val halfHeight = bitmap.height * scale / 2
        canvas.withSave {
            if (circular) clipPath(Path().apply { addOval(rect, Path.Direction.CW) })
            image(bitmap, RectF(rect.centerX() - halfWidth, rect.centerY() - halfHeight,
                rect.centerX() + halfWidth, rect.centerY() + halfHeight))
        }
    }

    fun clipOval(l: Float, t: Float, r: Float, b: Float, block: () -> Unit) {
        canvas.withClip(Path().apply { addOval(l, t, r, b, Path.Direction.CW) }) { block() }
    }

    companion object {
        fun create(size: Int = 320, draw: BitmapFactoryGenerator.() -> Unit): Bitmap =
            createBitmap(size, size).also {
                val canvas = Canvas(it)
                canvas.scale(size / 320f, size / 320f)
                BitmapFactoryGenerator(canvas).draw()
            }
    }
}
