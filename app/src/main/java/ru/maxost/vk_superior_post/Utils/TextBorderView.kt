package ru.maxost.vk_superior_post.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R

/**
 * Created by Maxim Ostrovidov on 15.09.17.
 * (c) White Soft
 */
class TextBorderView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    private var state: MyEditTextState? = null
    private var prevState: MyEditTextState? = null
    private var borderBitmap: Bitmap? = null
    private val colorWhite = ContextCompat.getColor(context, R.color.white)
    private val colorWhiteTransparent = ContextCompat.getColor(context, R.color.whiteTransparent)
    private val colorShadow = ContextCompat.getColor(context, R.color.shadow)
    private val colorTransparent = ContextCompat.getColor(context, R.color.transparent)
    private val hTopOffset = 4.dp2px(context)
    private val hBottomOffset = 4.dp2px(context)
    private val wOffset = 10.dp2px(context)
    private val points = mutableListOf<PointF>()
    private val path = Path()
    private val shadowPath = Path()
    private val shadowHeight = 1.dp2px(context).toFloat()
    private val cornerRadius = 4.dp2px(context).toFloat()
    private val paint = Paint().apply {
        isDither = true
        strokeWidth = 10f
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        pathEffect = CornerPathEffect(cornerRadius)
        isAntiAlias = true
    }

    fun setState(state: MyEditTextState) {
//        SwitchLog.scream(state.toString())
        this.state = state
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
//        SwitchLog.scream("onDraw")

        if (state == null || state!!.text.isEmpty()
                || state?.textStyle == TextStyle.WHITE
                || state?.textStyle == TextStyle.BLACK) return

        if(prevState?.text == state?.text && prevState?.textStyle == state?.textStyle && borderBitmap != null) {
            borderBitmap?.let { drawCanvas(canvas, it) }
            return
        }

        borderBitmap?.recycle()
        borderBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        borderBitmap?.let {
            if (state?.textStyle == TextStyle.WHITE_WITH_BACKGROUND) {
                drawBorder(colorWhiteTransparent, it)
                drawCanvas(canvas, it)
            }
            if (state?.textStyle == TextStyle.BLACK_WITH_BACKGROUND) {
                drawBorder(colorWhite, it)
                drawCanvas(canvas, it)
            }
        }
    }

    private fun drawCanvas(canvas: Canvas?, bitmap: Bitmap) {
        canvas!!.drawBitmap(bitmap, Matrix().apply {
            val translateY = (height.toFloat() - bitmap.height) / 2
            postTranslate(0f, translateY)
        }, null)
    }

    private fun drawBorder(color: Int, bitmap: Bitmap) {

        path.reset()
        val lines = state!!.linesList
        val textStartPoint = state!!.startPoint
        val borderStartPoint = IntArray(2).apply { getLocationOnScreen(this) }

        //batch lines and draw those batches separately
        val properLines = lines.map { convertRect(it, textStartPoint, borderStartPoint) }
        val dividerIndexes = properLines.withIndex().filter { it.value.width() == 0f }.map { it.index }
        properLines.withIndex().forEach { item ->
            if (properLines.size == 1) {
                createBorderPath(path, properLines.filter { it.width() > 0f })
            } else if (dividerIndexes.contains(item.index) && item.index > 0 || item.index == properLines.lastIndex) {
                val leftBoundedList = properLines.subList(0, item.index + 1)
                val lastEmptyLine = leftBoundedList.dropLast(1).indexOfLast { it.width() == 0f }
                val properLastEmptyLine = if (lastEmptyLine > -1) lastEmptyLine else 0
                val resultList = leftBoundedList.subList(properLastEmptyLine, leftBoundedList.lastIndex + 1)
                createBorderPath(path, resultList.filter { it.width() > 0f })
            }
        }

        val newCanvas = Canvas(bitmap)

        shadowPath.reset()
        shadowPath.addPath(path)
        shadowPath.offset(0f, shadowHeight)
        paint.color = colorShadow
        newCanvas.drawPath(shadowPath, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paint.color = colorTransparent
        newCanvas.drawPath(path, paint)

        paint.xfermode = null
        paint.color = color
        newCanvas.drawPath(path, paint)

        prevState = state
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

    private fun createBorderPath(path: Path, linesList: List<RectF>): Path {
        points.clear()

        for (lineIndex in 0 until linesList.size) {

            val rect = linesList[lineIndex]
            if (lineIndex == 0) path.moveTo(rect.centerX(), rect.top - hTopOffset)

            if (linesList.size == 1) {
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.left - wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.centerX(), rect.top - hTopOffset))
                break
            }

            //first
            if (lineIndex == 0) {
                points.add(PointF(rect.centerX(), rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.top - hTopOffset))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }

            //middle
            if (lineIndex > 0 && lineIndex < linesList.size - 1) {
                if (rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset*2))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom - hTopOffset))
                continue
            }

            //last
            if (lineIndex == linesList.size - 1) {
                if (rect.right + wOffset < points.last().x) {
                    points.add(PointF(points.last().x, points.last().y + hBottomOffset*2))
                }
                points.add(PointF(rect.right + wOffset, points.last().y))
                points.add(PointF(rect.right + wOffset, rect.bottom + hBottomOffset))
                points.add(PointF(rect.centerX(), rect.bottom + hBottomOffset))
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