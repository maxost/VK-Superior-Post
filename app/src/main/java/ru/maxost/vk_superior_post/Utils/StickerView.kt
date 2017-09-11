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

    private val stickers: MutableList<Sticker> = mutableListOf()
    private var bitmaps: MutableSet<Pair<Sticker, Bitmap>> = mutableSetOf()

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

        stickers.forEach { sticker ->
            SwitchLog.scream(sticker.toString())

            if(!bitmaps.any { it.first == sticker }) {
                val drawable = BitmapFactory.decodeResource(context.resources, sticker.id)
                val ratio: Float = drawable.width.toFloat() / drawable.height.toFloat()
                val scaledWidth = sticker.scaleFactor * width * ratio
                val scaledHeight = sticker.scaleFactor * width
                bitmaps.add(Pair(sticker, Bitmap.createScaledBitmap(drawable, scaledWidth.toInt(), scaledHeight.toInt(), true)))
                drawable.recycle()
            }

            val bitmap = bitmaps.first { it.first == sticker } .second
            val matrix = Matrix().apply {
                SwitchLog.scream("sticker.scaleFactor: ${sticker.scaleFactor}")
//                preScale(sticker.scaleFactor, sticker.scaleFactor)
                preTranslate(sticker.xFactor * width - (sticker.scaleFactor * width / 2), sticker.yFactor * height - (sticker.scaleFactor * width / 2))
                preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
            }

            canvas?.drawBitmap(bitmap, matrix, null)
        }
    }

    private var currentSticker: Sticker? = null
    private var currentTouchDistanceFromCenterX = 0
    private var currentTouchDistanceFromCenterY = 0

    private var primaryStart: Point = Point()
    private var secondaryStart: Point = Point()

    private var multiTouchMode = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                SwitchLog.scream("DOWN x: ${event.x} y: ${event.y}")
                stickers.lastOrNull {
                    calculateRect(it, width, height).contains(event.x.toInt(), event.y.toInt())
                }?.let {
                    SwitchLog.scream("Sticker match!")
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
                multiTouchMode = true
                secondaryStart = Point(event.getX(1).toInt(), event.getY(1).toInt())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                SwitchLog.scream("MOVE x: ${event.x} y: ${event.y}")

                if(multiTouchMode) {
                    val primaryPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
                    val secondaryPoint = Point(event.getX(1).toInt(), event.getY(1).toInt())
                    val distance = calculateDistance(primaryStart, secondaryStart) - calculateDistance(primaryPoint, secondaryPoint)
                    val angleChange = calculateAngle(primaryStart, secondaryStart) - calculateAngle(primaryPoint, secondaryPoint)

                    SwitchLog.scream("distance: $distance angleChange: $angleChange")
                }

                currentSticker?.apply {
                    xFactor = (event.x + currentTouchDistanceFromCenterX) / width
                    yFactor = (event.y + currentTouchDistanceFromCenterY) / height
                    invalidate()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if(currentSticker!=null) {
                    currentSticker = null
                    currentTouchDistanceFromCenterX = 0
                    currentTouchDistanceFromCenterY = 0
                    multiTouchMode = false
                    primaryStart = Point()
                    secondaryStart = Point()
                    bitmaps.clear() //TODO ?
                    return true
                }
                return false
            }
            MotionEvent.ACTION_POINTER_UP -> {
                multiTouchMode = false
                secondaryStart = Point()
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

        SwitchLog.scream("left: ${rect.left} top: ${rect.top} right: ${rect.right} bottom: ${rect.bottom} stickerHeight: $stickerHeight stickerWidth: $stickerWidth")
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

    fun calculateAngle(primary: Point, secondary: Point): Float {
        var angle = Math.toDegrees(Math.atan2(primary.y.toDouble() - secondary.y.toDouble(), primary.x.toDouble() - secondary.x.toDouble()))
        if (angle < 0) angle += 360f
        return angle.toFloat()
    }
}