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
                val scaledHeight = sticker.scaleFactor * height
                SwitchLog.scream("drawable.width: ${drawable.width} drawable.height: ${drawable.height} scaledWidth: $scaledWidth scaledHeight: $scaledHeight")
                bitmaps.add(Pair(sticker, Bitmap.createScaledBitmap(drawable, scaledWidth.toInt(), scaledHeight.toInt(), true)))
                drawable.recycle()
            }

            val bitmap = bitmaps.first { it.first == sticker } .second
            val matrix = Matrix().apply {
                setTranslate(sticker.xFactor * width - (sticker.scaleFactor * width / 2), sticker.yFactor * height - (sticker.scaleFactor * height / 2))
                postRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
            }

            canvas?.drawBitmap(bitmap, matrix, null)
        }
    }

    private var currentSticker: Sticker? = null
    private var currentTouchDistanceFromCenterX = 0
    private var currentTouchDistanceFromCenterY = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val action = MotionEventCompat.getActionMasked(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                SwitchLog.scream("Action was DOWN x: ${event.x} y: ${event.y}")
                stickers.firstOrNull {
                    calculateRect(it, width, height).contains(event.x.toInt(), event.y.toInt())
                }?.let {
                    SwitchLog.scream("ACTION_DOWN sticker match!")
                    //move sticker to the end of list
                    stickers.remove(it)
                    stickers.add(it)

                    val rect = calculateRect(it, width, height)
                    currentTouchDistanceFromCenterX = (rect.centerX() - event.x).toInt()
                    currentTouchDistanceFromCenterY = (rect.centerY() - event.y).toInt()

                    currentSticker = it
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                SwitchLog.scream("Action was MOVE x: ${event.x} y: ${event.y}")

                currentSticker?.apply {
                    SwitchLog.scream("currentTouchDistanceFromCenterX: $currentTouchDistanceFromCenterX currentTouchDistanceFromCenterY: $currentTouchDistanceFromCenterY")
                    xFactor = (event.x + currentTouchDistanceFromCenterX) / width
                    yFactor = (event.y + currentTouchDistanceFromCenterY) / height
                    invalidate()
                    return true
                }
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                SwitchLog.scream( "Action was UP or CANCEL x: ${event.x} y: ${event.y}")
                if(currentSticker!=null) {
                    currentSticker = null
                    currentTouchDistanceFromCenterX = 0
                    currentTouchDistanceFromCenterY = 0
                    return true
                }
                return false
            }
            else -> return super.onTouchEvent(event)
        }
    }

    private fun calculateRect(sticker: Sticker, width: Int, height: Int): Rect {
        val rect = Rect()

        val stickerHeight = height * sticker.scaleFactor
        val stickerWidth = width * sticker.scaleFactor

        rect.top    = (width * sticker.yFactor - stickerHeight / 2).toInt()
        rect.bottom = (width * sticker.yFactor + stickerHeight / 2).toInt()
        rect.left   = (width * sticker.xFactor - stickerWidth  / 2).toInt()
        rect.right  = (width * sticker.xFactor + stickerWidth  / 2).toInt()

        SwitchLog.scream("left: ${rect.left} top: ${rect.top} right: ${rect.right} bottom: ${rect.bottom} ")
        return rect
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        SwitchLog.scream("onSizeChanged w: $w h: $h oldw: $oldw oldh: $oldh")
    }
}