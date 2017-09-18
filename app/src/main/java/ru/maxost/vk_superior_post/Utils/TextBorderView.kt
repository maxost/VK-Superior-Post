package ru.maxost.vk_superior_post.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import io.reactivex.Observable
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import java.util.*

/**
 * Created by Maxim Ostrovidov on 15.09.17.
 * (c) White Soft
 */
class TextBorderView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private var state: MyEditTextState? = null

    fun setState(state: MyEditTextState) {
        SwitchLog.scream(state.toString())
        this.state = state
        invalidate()
    }

    private val colorWhite = ContextCompat.getColor(context, R.color.white)
    private val colorWhiteTransparent = ContextCompat.getColor(context, R.color.whiteTransparent)
    private val colorShadow = ContextCompat.getColor(context, R.color.shadow)
    private val colorTransparent = ContextCompat.getColor(context, R.color.transparent)
    private val hTopOffset = 6.dp2px(context)
    private val hBottomOffset = 8.dp2px(context)
    private val wOffset = 12.dp2px(context)
    private val points = mutableListOf<PointF>()
    private val path = Path()
    private val shadowPath = Path()

    private val paint = Paint().apply {
        isDither = true
        strokeWidth = 10f
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        pathEffect = CornerPathEffect(10f)
        isAntiAlias = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
//        SwitchLog.scream("onLayout top: $top")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        SwitchLog.scream("onSizeChanged h: $h oldh: $oldh")
    }

    override fun onDraw(canvas: Canvas?) {
//        SwitchLog.scream("onDraw")
        if (state == null || state!!.text.isEmpty()) {
            super.onDraw(canvas)
            return
        }

        if (state?.textStyle == TextStyle.WHITE_WITH_BACKGROUND) drawBorder(canvas, colorWhiteTransparent)
        if (state?.textStyle == TextStyle.BLACK_WITH_BACKGROUND) drawBorder(canvas, colorWhite)

        super.onDraw(canvas)
    }

    private fun drawBorder(canvas: Canvas?, color: Int) {
        if (canvas == null) return

        path.reset()
        val lines = state!!.linesList
        val textStartPoint = state!!.startPoint
        val borderStartPoint = IntArray(2).apply { getLocationOnScreen(this) }
//        SwitchLog.scream("textStartPoint ${textStartPoint[1]} borderStartPoint: ${borderStartPoint[1]}")

        //batch lines and draw it
        val properLines = lines.map { convertRect(it, textStartPoint, borderStartPoint) }
        val dividerIndexes = properLines.withIndex().filter { it.value.width() == 0f }.map { it.index }
        properLines.withIndex().forEach { item ->
            if (properLines.size == 1) {
                createBorderPath(path, properLines)
                SwitchLog.scream("first item")
            } else if (dividerIndexes.contains(item.index) && item.index > 0 || item.index == properLines.lastIndex) {
                val leftBoundedList = properLines.subList(0, item.index + 1)
                val lastEmptyLine = leftBoundedList.dropLast(1).indexOfLast { it.width() == 0f }
                val properLastEmptyLine = if (lastEmptyLine > -1) lastEmptyLine else 0
                val resultList = leftBoundedList.subList(properLastEmptyLine, leftBoundedList.lastIndex + 1)
                createBorderPath(path, resultList.filter { it.width() > 0f })
            }
        }

        shadowPath.reset()
        shadowPath.addPath(path)
        shadowPath.offset(0f, 4f)
        shadowPath.addPath(path)
        shadowPath.fillType = Path.FillType.EVEN_ODD
        paint.color = colorShadow
        canvas.drawPath(shadowPath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paint.color = colorTransparent
        canvas.drawPath(path, paint)

        paint.xfermode = null
        paint.color = color
        canvas.drawPath(path, paint)
    }

    private fun convertRect(fromRect: RectF, fromViewStart: IntArray, toViewStart: IntArray): RectF {
        val xShift = fromViewStart[0] - toViewStart[0]
        val yShift = fromViewStart[1] - toViewStart[1]
        return RectF().apply {
            left = fromRect.left + xShift
            right = fromRect.right + xShift
            top = fromRect.top + yShift
            bottom = fromRect.bottom + yShift
        }
    }

    //TODO incorrect on pre-M
    private fun createBorderPath(path: Path, linesList: List<RectF>): Path {
        points.clear()

        for (lineIndex in 0 until linesList.size) {

            val rect = linesList[lineIndex]
//            SwitchLog.scream("lineIndex: $lineIndex rect left: ${rect.left} right: ${rect.right} top: ${rect.top} bottom: ${rect.bottom}")
            if (lineIndex == 0) path.moveTo(rect.centerX(), rect.top - hTopOffset)

            if (linesList.size == 1) {
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.centerX(), rect.top - hTopOffset))
                break
            }

            if (lineIndex == 0) {
                points.add(PointF(rect.centerX(), rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }

            if (lineIndex == linesList.size - 1) {
                if (rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.centerX(), rect.bottom + hBottomOffset))
                continue
            }

            if (lineIndex > 0 && lineIndex < linesList.size - 1) {
                if (rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }
        }
        points.forEach { path.lineTo(it.x, it.y) }

        //left side
        if (linesList.size != 1 && points.isNotEmpty()) {
            val centerX = points.first().x
            points.reversed().forEach { path.lineTo(centerX - (it.x - centerX), it.y) }
        }

        return path
    }
}