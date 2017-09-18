package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import java.io.Serializable

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
data class MyEditTextState(val text: String,
                           val startPoint: IntArray,
                           var linesList: List<RectF>,
                           val textStyle: TextStyle) : Serializable

class MyEditText @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : AppCompatEditText(context, attributeSet) {

    var isInterceptingTouches = true
    private val colorShadow = ContextCompat.getColor(context, R.color.shadow)

    var textStyle = TextStyle.WHITE
        set(value) {
            SwitchLog.scream("$value")
            field = value

            setShadowLayer(0f, 0f, 0f, 0)
            if (text.isBlank()) return

            when (textStyle) {
                TextStyle.BLACK -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.black))
                }
                TextStyle.WHITE -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.white))
                    setShadowLayer(5f, 0f, 5f, colorShadow)
                }
                TextStyle.BLACK_WITH_BACKGROUND -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.black))
                }
                TextStyle.WHITE_WITH_BACKGROUND -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.white))
                    setShadowLayer(5f, 0f, 5f, colorShadow)
                }
            }
        }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) return super.onTouchEvent(event)
        val textTouched = getState().linesList.any {
            it.right += 40
            it.left -= 40
            it.top -= 40
            it.bottom += 40
            it.contains(event.x, event.y)
        }
        return if (isInterceptingTouches && text.isBlank() || isInterceptingTouches && textTouched) super.onTouchEvent(event) else false
    }

    fun getState(): MyEditTextState {
        val lines = (0 until lineCount).mapTo(mutableListOf()) { getLineRect(it) }
        val startCoord = IntArray(2).apply { getLocationOnScreen(this) }
        return MyEditTextState(
                text = text.toString(),
                startPoint = intArrayOf(startCoord[0], startCoord[1]),
                linesList = lines,
                textStyle = textStyle)
    }

    private fun getLineRect(lineIndex: Int): RectF {
        val rect = Rect()
        getLineBounds(lineIndex, rect)

        // getLineBounds returns incorrect width of the line
        // so we will calculate it separately
        val lineWidth = getLineWidth(lineIndex)
        val centerX = rect.centerX()
        if (lineWidth > 0) {
            rect.right = centerX + getLineWidth(lineIndex) / 2
            rect.left = centerX - getLineWidth(lineIndex) / 2
        } else {
            rect.right = centerX
            rect.left = centerX
        }
        return RectF(rect)
    }

    private fun getLineWidth(lineIndex: Int): Int {
        if (layout == null) return 0
        val lineStart = layout.getLineStart(lineIndex)
        val lineEnd = layout.getLineEnd(lineIndex)
        val textLine = text.subSequence(lineStart, lineEnd)
        if (textLine.isBlank()) return 0
        val rect = Rect()
        paint.getTextBounds(textLine.toString(), 0, textLine.length, rect)
        return rect.width() + calculateLeadingAndTailingSpacesWidth(textLine.toString())
    }

    private fun calculateLeadingAndTailingSpacesWidth(text: String): Int {
        val count = text.takeWhile { Character.isWhitespace(it) }.length +
                text.takeLastWhile { Character.isWhitespace(it) }.length
        val spaceWidth = paint.measureText(" ").toInt()
        return count * spaceWidth
    }
}