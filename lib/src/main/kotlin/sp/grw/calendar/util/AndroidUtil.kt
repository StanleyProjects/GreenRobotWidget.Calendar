package sp.grw.calendar.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

private val techRect = Rect()
private val techRectF = RectF()

internal object AndroidUtil {
    fun Paint.getTextHeight(text: String): Int {
        getTextBounds(text, 0, text.length, techRect)
        return techRect.height()
    }

    fun Canvas.drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Float,
        paint: Paint
    ) {
        techRectF.left = left
        techRectF.top = top
        techRectF.right = right
        techRectF.bottom = bottom
        drawRoundRect(techRectF, radius, radius, paint)
    }
}
