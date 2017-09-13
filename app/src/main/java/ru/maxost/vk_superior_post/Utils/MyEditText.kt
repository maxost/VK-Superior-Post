package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.R

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
class MyEditText @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : AppCompatEditText(context, attributeSet) {

    var isInterceptingTouches = true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val color1 = ContextCompat.getColor(context, R.color.blue)
    private val color2 = ContextCompat.getColor(context, R.color.shadow)

    override fun onDraw(canvas: Canvas?) {
        val path = Path()

        var lineIndex = 0
        while (lineIndex < lineCount) {
            val rect = getLineRect(lineIndex)
//            rect.bottom += 12.dp2px(context)
//            rect.left -= 12.dp2px(context)
//            rect.top -= 12.dp2px(context)
//            rect.right += 12.dp2px(context)
            path.addRoundRect(RectF(rect), 10f, 10f, Path.Direction.CW)
            lineIndex++
        }

        paint.color = color1
        paint.isDither = true
//        paint.strokeWidth = 30f
//        paint.style = Paint.Style.STROKE
//        paint.strokeJoin = Paint.Join.ROUND
//        paint.strokeCap = Paint.Cap.ROUND
//        paint.pathEffect = CornerPathEffect(10f)
        paint.isAntiAlias = true
        canvas?.drawPath(path, paint)

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if(isInterceptingTouches) super.onTouchEvent(event) else false
    }

    private fun getLineRect(lineIndex: Int): Rect {
        val rect = Rect()
        getLineBounds(lineIndex, rect)

        // getLineBounds returns incorrect width of the line
        // so we will calculate it separately
        val lineWidth = getLineWidth(lineIndex)
        val centerX = rect.centerX()
        if(lineWidth > 0) {
            rect.right = centerX + getLineWidth(lineIndex) / 2
            rect.left = centerX - getLineWidth(lineIndex) / 2
        } else {
            rect.right = centerX
            rect.left = centerX
        }
        return rect
    }

    private fun getLineWidth(lineIndex: Int): Int {
        if(layout==null) return 0
        val lineStart = layout.getLineStart(lineIndex)
        val lineEnd = layout.getLineEnd(lineIndex)
        val textLine = text.subSequence(lineStart, lineEnd)
        if(textLine.isBlank()) return 0
        val rect = Rect()
        getPaint().getTextBounds(textLine.toString(), 0, textLine.length, rect)
        return rect.width() + calculateLeadingAndTailingSpacesWidth(textLine.toString())
    }

    private fun calculateLeadingAndTailingSpacesWidth(text: String): Int {
        val count = text.takeWhile { Character.isWhitespace(it) }.length +
                text.takeLastWhile { Character.isWhitespace(it) }.length
        val spaceWidth = getPaint().measureText(" ").toInt()
        return count * spaceWidth
    }
}