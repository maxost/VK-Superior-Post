package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.Sticker
import android.graphics.*

/**
 * Created by Maxim Ostrovidov on 11.09.17.
 * (c) White Soft
 */
class StickerView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {

    companion object {
        const val MIN_SCALE_FACTOR = 0.1f
        const val MAX_SCALE_FACTOR = 1f
    }

    interface Listener {
        fun onMultiTouch(enable: Boolean)
        fun onDragging(isDragging: Boolean): Rect
        fun onOverBin(isOverBin: Boolean)
        fun onDeleteSticker(sticker: Sticker)
    }

    private val stickers: MutableList<Sticker> = mutableListOf()
    private var bitmaps: MutableSet<Pair<Int, Bitmap>> = mutableSetOf() // resId - Bitmap

    fun addSticker(sticker: Sticker) {
        stickers.add(sticker)
        invalidate()
    }

    fun setStickers(stickers: MutableList<Sticker>) {
        this.stickers.clear()
        this.stickers.addAll(stickers)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        SwitchLog.scream("stickers: ${stickers.size} bitmaps: ${bitmaps.size}")

        stickers.forEach { sticker ->
//            SwitchLog.scream(sticker.toString())

            if(bitmaps.firstOrNull { it.first == sticker.resId } == null) {
                val drawable = BitmapFactory.decodeResource(context.resources, sticker.resId)
                val ratio: Float = drawable.width.toFloat() / drawable.height.toFloat()
                val scaledWidth = width * ratio
                val scaledHeight = width
                bitmaps.add(Pair(sticker.resId, Bitmap.createScaledBitmap(drawable, scaledWidth.toInt(), scaledHeight.toInt(), true)))
                drawable.recycle()
            }

            val bitmap = bitmaps.firstOrNull { it.first == sticker.resId }?.second ?: throw IllegalArgumentException("no bitmap!?")
            val matrix = Matrix().apply {
                preScale(sticker.scaleFactor, sticker.scaleFactor)
                preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
                val translateX = sticker.xFactor * width - (sticker.scaleFactor * width / 2)
                val translateY = sticker.yFactor * height - (sticker.scaleFactor * width / 2)
                postTranslate(translateX, translateY)
            }
            canvas?.drawBitmap(bitmap, matrix, null)
        }
    }

    private var currentSticker: Sticker? = null
    private var currentTouchDistanceFromCenterX = 0
    private var currentTouchDistanceFromCenterY = 0

    private var primaryStart: Point = Point()
    private var secondaryStart: Point = Point()
    private var startAngle: Float = 0f
    private var startScaleFactor = 0f
    private var startDistance: Int = 0

    private var binRect = Rect()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                SwitchLog.scream("DOWN x: ${event.x} y: ${event.y}")
                stickers.lastOrNull {
                    calculateRect(it, width, height).contains(event.x.toInt(), event.y.toInt())
                }?.let {
                    SwitchLog.scream("Sticker match!")
                    (context as Listener).onMultiTouch(true)

                    //move sticker to the end of list
                    stickers.remove(it)
                    stickers.add(it)

                    val rect = calculateRect(it, width, height)
                    currentTouchDistanceFromCenterX = (rect.centerX() - event.x).toInt()
                    currentTouchDistanceFromCenterY = (rect.centerY() - event.y).toInt()
                    primaryStart = Point(event.x.toInt(), event.y.toInt())

                    currentSticker = it
                    return true
                }
                return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                SwitchLog.scream("POINTER_DOWN x: ${event.x} y: ${event.y}")
                currentSticker?.let {
                    secondaryStart = Point(event.getX(1).toInt(), event.getY(1).toInt())
                    startAngle = it.angle
                    startScaleFactor = it.scaleFactor

                    val primaryPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
                    val secondaryPoint = Point(event.getX(1).toInt(), event.getY(1).toInt())
                    startDistance = calculateDistance(primaryPoint, secondaryPoint)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
//                SwitchLog.scream("MOVE x: ${event.x} y: ${event.y}")

                if(currentSticker==null) return false

                if(event.pointerCount == 2) {
                    val primaryPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
                    val secondaryPoint = Point(event.getX(1).toInt(), event.getY(1).toInt())

                    //TODO glitches when trying to adjust angle from an opposite position
                    val currentAngle = calcRotationAngleInDegrees(primaryPoint, secondaryPoint)
                    val angleChange = calcRotationAngleInDegrees(primaryStart, secondaryStart) - currentAngle
                    val newAngle = startAngle - angleChange

                    val currentDistance = calculateDistance(primaryPoint, secondaryPoint)
                    val distanceChange =  currentDistance - startDistance

                    //TODO scale with center between fingers
                    val startStickerSize = (startScaleFactor * width).toInt()
                    val newStickerSize: Int = startStickerSize + distanceChange
                    val newStickerScaleFactor: Float = newStickerSize.toFloat() / width.toFloat()
                    val newScaleFactorBounded = Math.min(Math.max(newStickerScaleFactor, MIN_SCALE_FACTOR), MAX_SCALE_FACTOR)

                    SwitchLog.scream(
                            //                                "startDistance: $startDistance " +
//                                "distanceChange: ${distanceChange.toFloat()} " +
//                                "width: ${width.toFloat()} " +
//                                "origScaleSize: $scaleFactor " +
//                                "startStickerSize: $startStickerSize " +
//                                "newStickerSize: $newStickerSize " +
//                                "newStickerScaleFactor: $newStickerScaleFactor " +
//                                "newScaleFactorBounded: $newScaleFactorBounded " +
                            "startAngle: $startAngle " +
                                    "currentAngle: $currentAngle " +
                                    "angleChange: $angleChange " +
                                    "newAngle: $newAngle")

                    currentSticker?.scaleFactor = newScaleFactorBounded
                    currentSticker?.angle = newAngle
                }

                currentSticker?.xFactor = (event.x + currentTouchDistanceFromCenterX) / width
                currentSticker?.yFactor = (event.y + currentTouchDistanceFromCenterY) / height

                val listener = context as Listener
                if(event.pointerCount == 1) {
                    binRect = listener.onDragging(true)
                    val isOverBin = binRect.contains(event.rawX.toInt(), event.rawY.toInt())
                    listener.onOverBin(isOverBin)
                } else listener.onDragging(false)

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                SwitchLog.scream("ACTION_UP or ACTION_CANCEL x: ${event.x} y: ${event.y}")
                val listener = context as Listener
                listener.onMultiTouch(false)
                listener.onDragging(false)
                val isOverBin = binRect.contains(event.rawX.toInt(), event.rawY.toInt())
                if(isOverBin) {
                    currentSticker?.let { listener.onDeleteSticker(it) }
                    bitmaps.firstOrNull { it.first == currentSticker?.resId }?.second?.recycle()
                    bitmaps.removeAll { it.first == currentSticker?.resId }
                    stickers.remove(currentSticker)
                    invalidate()
                }

                currentSticker = null
                currentTouchDistanceFromCenterX = 0
                currentTouchDistanceFromCenterY = 0
                primaryStart = Point()
                secondaryStart = Point()
                startAngle = 0f
                startScaleFactor = 0f
                startDistance = 0
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                SwitchLog.scream("ACTION_POINTER_UP x: ${event.x} y: ${event.y} index: ${event.actionIndex}")

                if(event.actionIndex == 0) {

                    secondaryStart = Point()
                    startAngle = 0f
                    startScaleFactor = 0f
                    startDistance = 0

                    val rect = calculateRect(currentSticker!!, width, height)
                    currentTouchDistanceFromCenterX = (rect.centerX() - event.getX(1)).toInt()
                    currentTouchDistanceFromCenterY = (rect.centerY() - event.getY(1)).toInt()
                    primaryStart = Point(event.x.toInt(), event.y.toInt())
                } else {
                    secondaryStart = Point()
                    startAngle = 0f
                    startScaleFactor = 0f
                    startDistance = 0
                }
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    private fun calculateRect(sticker: Sticker, width: Int, height: Int): Rect {
        val rect = Rect()

        val stickerHeight = width * sticker.scaleFactor
        val stickerWidth = width * sticker.scaleFactor

        rect.top    = (height * sticker.yFactor - stickerHeight / 2).toInt()
        rect.bottom = (height * sticker.yFactor + stickerHeight / 2).toInt()
        rect.left   = (width * sticker.xFactor - stickerWidth  / 2).toInt()
        rect.right  = (width * sticker.xFactor + stickerWidth  / 2).toInt()

//        SwitchLog.scream("left: ${rect.left} top: ${rect.top} right: ${rect.right} bottom: ${rect.bottom} " +
//                "stickerHeight: $stickerHeight stickerWidth: $stickerWidth")
        return rect
    }

    private fun calculateDistance(primary: Point, secondary: Point): Int {
        return Math.sqrt(
                Math.pow(
                        secondary.x.toDouble() - primary.x.toDouble(),
                        2.toDouble()) + Math.pow(secondary.y.toDouble() - primary.y.toDouble(),
                        2.toDouble()
                )).toInt()
    }

    private fun correctAngle(angle: Float): Float {
        var newAngle = angle
        if (angle < 0) newAngle += 360f
        return newAngle
    }

    private fun calcRotationAngleInDegrees(centerPt: Point, targetPt: Point): Float {
        var theta = Math.atan2((targetPt.y - centerPt.y).toDouble(), (targetPt.x - centerPt.x).toDouble())
        theta += Math.PI / 2.0
        var angle = Math.toDegrees(theta)
        if (angle < 0) angle += 360.0
        return angle.toFloat()
    }
}