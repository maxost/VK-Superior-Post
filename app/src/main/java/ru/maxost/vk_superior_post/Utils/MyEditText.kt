package ru.maxost.vk_superior_post.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
class MyEditText @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : AppCompatEditText(context, attributeSet) {

    var isInterceptingTouches = true

    var textStyle = TextStyle.WHITE
        set(value) {
            SwitchLog.scream("$value")
            field = value

            setShadowLayer(0f, 0f, 0f, 0)
            if(text.isBlank()) return

            when(textStyle) {
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
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colorWhite = ContextCompat.getColor(context, R.color.white)
    private val colorWhiteTransparent = ContextCompat.getColor(context, R.color.whiteTransparent)
    private val colorShadow = ContextCompat.getColor(context, R.color.shadow)
    private val hTopOffset = 6.dp2px(context)
    private val hBottomOffset = 8.dp2px(context)
    private val wOffset = 12.dp2px(context)
    private val points = mutableListOf<PointF>()

    private val currentBorderPath = Path()
    private var lastText = ""

    //TODO multiple enters
    //TODO multiple spaces
    //TODO smooth edges

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        SwitchLog.scream("onDraw")
        if(text.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        if(textStyle == TextStyle.WHITE_WITH_BACKGROUND) drawBorder(canvas, colorWhiteTransparent)
        if(textStyle == TextStyle.BLACK_WITH_BACKGROUND) drawBorder(canvas, colorWhite)

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

    private fun drawBorder(canvas: Canvas?, color: Int) {
        if(canvas == null) return

        paint.apply {
            this.color = color
            isDither = true
            strokeWidth = 10f
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            pathEffect = CornerPathEffect(10f)
//            this.setShadowLayer(5f, 0f, 5f, colorShadow)
//            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            isAntiAlias = true
        }

//        if(text.toString() != lastText) {
            SwitchLog.scream("text: ${text} lastText: $lastText")
            createBorderPath(currentBorderPath)
            lastText = text.toString()
//        }

        canvas.drawPath(currentBorderPath, paint)
    }

    private fun createBorderPath(path: Path): Path {
        SwitchLog.scream("createBorderPath")
        points.clear()
        path.reset()

        for (lineIndex in 0 until lineCount) {
            if(getLineWidth(lineIndex) == 0) continue

            val rect = getLineRect(lineIndex)
            SwitchLog.scream("rect left: ${rect.left} right: ${rect.right}")

            if(lineCount == 1 || (lineIndex == 0 && lineIndex + 1 < lineCount && getLineWidth(lineIndex + 1) == 0)) {
                path.moveTo(rect.left - wOffset + 20, rect.top - hTopOffset)
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.top - hTopOffset))
                break
            }

            if (lineIndex == 0) {
                path.moveTo(rect.centerX(), rect.top - hTopOffset)
                points.add(PointF(rect.centerX(), rect.top - hTopOffset))
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
        points.forEach { path.lineTo(it.x, it.y) }

        //draw left side
        if(lineCount != 1 && points.isNotEmpty() && getLineWidth(1) != 0) {
            val centerX = points.first().x
            points.reversed().forEach {
                path.lineTo(centerX - (it.x - centerX), it.y)
            }
        }

        path.close()
        return path
    }
}