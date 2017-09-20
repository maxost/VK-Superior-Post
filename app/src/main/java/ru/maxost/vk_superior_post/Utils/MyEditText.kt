package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
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
    private val textShadowHeight = 1.dp2px(context).toFloat()

    var textStyle = TextStyle.WHITE
        set(value) {
            field = value
            when (textStyle) {
                TextStyle.BLACK, TextStyle.BLACK_WITH_BACKGROUND -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.black))
                    setHintTextColor(ContextCompat.getColor(this.context, R.color.blackTransparent))
                }
                TextStyle.WHITE, TextStyle.WHITE_WITH_BACKGROUND -> {
                    setTextColor(ContextCompat.getColor(this.context, R.color.white))
                    setHintTextColor(ContextCompat.getColor(this.context, R.color.whiteTransparent))
                }
            }
        }

    override fun onDraw(canvas: Canvas?) {
        if (text.isNotEmpty() && (textStyle == TextStyle.WHITE || textStyle == TextStyle.WHITE_WITH_BACKGROUND)) {
            setShadowLayer(textShadowHeight, 0f, textShadowHeight, colorShadow)
        } else {
            setShadowLayer(0f, 0f, 0f, 0)
        }
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
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
        rect.bottom = rect.top + lineHeight

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
        val textLine = text.subSequence(lineStart, lineEnd).toString().replace("\n", "")
        if (textLine.isBlank()) return 0
        val rect = Rect()
        paint.getTextBounds(textLine, 0, textLine.length, rect)
        return rect.width() + calculateLeadingAndTailingSpacesWidth(textLine)
    }

    private fun calculateLeadingAndTailingSpacesWidth(text: String): Int {
        val count = text.takeWhile { Character.isWhitespace(it) }.length +
                text.takeLastWhile { Character.isWhitespace(it) }.length
        val spaceWidth = paint.measureText(" ").toInt()
        return count * spaceWidth
    }
}