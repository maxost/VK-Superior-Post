package ru.maxost.vk_superior_post.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Switch
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.R

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
class MyEditText @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : AppCompatEditText(context, attributeSet) {

    var isInterceptingTouches = true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val color1 = ContextCompat.getColor(context, R.color.whiteTransparent)
    private val hTopOffset = 6.dp2px(context)
    private val hBottomOffset = 8.dp2px(context)
    private val wOffset = 12.dp2px(context)
    private val path = Path()
    private val points = mutableListOf<PointF>()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        path.reset()
        points.clear()

        //TODO need proper height
        //TODO multiple enters

        for (lineIndex in 0 until lineCount) {
            if(text.isEmpty()) {
                super.onDraw(canvas)
                return
            }
            if(getLineWidth(lineIndex) == 0) continue

            val rect = getLineRect(lineIndex)

            if(lineCount == 1 || (lineIndex == 0 && lineIndex + 1 < lineCount && getLineWidth(lineIndex + 1) == 0)) {
                path.moveTo(rect.left - wOffset + 20, rect.top - hTopOffset)
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.top - hTopOffset))
                break
            } else if(lineIndex == 0) path.moveTo(rect.centerX(), rect.top - hTopOffset)

            if (lineIndex == 0) {
                points.add(PointF(rect.left - wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }

            if (lineIndex == lineCount - 1 || (lineIndex + 1 < lineCount && getLineWidth(lineIndex + 1) == 0)) {
                if(rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.centerX(), rect.bottom + hBottomOffset))
                continue
            }

            if (lineIndex > 0 && lineIndex < lineCount - 1) {
                if(rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }
        }

        for (lineIndex in lineCount-1 downTo 0) {
            if(lineCount == 1 || points.isEmpty()) break

            //TODO reverse list
        }

        points.forEach { path.lineTo(it.x, it.y) }
        path.close()

        paint.color = color1
        paint.isDither = true
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.pathEffect = CornerPathEffect(10f)
        paint.isAntiAlias = true
        canvas?.drawPath(path, paint)

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (isInterceptingTouches) super.onTouchEvent(event) else false
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