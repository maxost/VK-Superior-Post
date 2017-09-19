package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.graphics.*
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Model.Sticker
import java.util.concurrent.TimeUnit

/**
 * Created by Maxim Ostrovidov on 11.09.17.
 * (c) White Soft
 */
enum class StickerState {
    NORMAL,
    CREATING,
    OVER_BIN,
    OUT_BIN
}

object StickerTasks {

    private val DEFAULT_DURATION: Long = 150

    private val subjects = mutableMapOf<String, BehaviorSubject<Float>>()

    fun startTask(stickerId: String, duration: Long = DEFAULT_DURATION) {
        SwitchLog.scream("stickerId: $stickerId")

        //clean up
        subjects[stickerId]?.onComplete()
        subjects.remove(stickerId)

        val subject = BehaviorSubject.createDefault<Float>(0f)
        subjects.put(stickerId, subject)

        Observable.range(1, 100)
                .concatMap { Observable.just(it).delay(duration / 100, TimeUnit.MILLISECONDS) }
                .map { it / 100f }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    subject.onNext(it)
                }, {
                    it.printStackTrace()
                })
    }

    fun getTaskCompletionFactor(stickerId: String): Float {
        val factor = subjects[stickerId]?.value
        if(factor == 1f) {
            subjects[stickerId]?.onComplete()
            subjects.remove(stickerId)
        }

        SwitchLog.scream("stickerId: $stickerId CompletionFactor: $factor")
        return factor ?: 1f
    }
}

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
    private val stickerState = mutableMapOf<String, StickerState>() // String = stickerId
    private var bitmaps: MutableSet<Pair<Int, Bitmap>> = mutableSetOf() // Int = resId
    private val paint = Paint()

    fun addSticker(sticker: Sticker) {
        stickers.add(sticker)
        stickerState.put(sticker.id, StickerState.CREATING)
        StickerTasks.startTask(sticker.id, 200)
        invalidate()
    }

    fun setStickers(stickers: MutableList<Sticker>) {
        this.stickers.clear()
        this.stickers.addAll(stickers)
        stickers.forEach { stickerState.put(it.id, StickerState.NORMAL) }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        SwitchLog.scream("stickers: ${stickers.size} bitmaps: ${bitmaps.size}")

        stickers.forEach { sticker ->
//            SwitchLog.scream(sticker.toString())
//            SwitchLog.scream("${stickerState[sticker.id]}")

            if(bitmaps.firstOrNull { it.first == sticker.resId } == null) {
                val drawable = BitmapFactory.decodeResource(context.resources, sticker.resId)
                val ratio: Float = drawable.width.toFloat() / drawable.height.toFloat()
                val scaledWidth = width * ratio
                val scaledHeight = width
                bitmaps.add(Pair(sticker.resId, Bitmap.createScaledBitmap(drawable, scaledWidth.toInt(), scaledHeight.toInt(), true)))
                drawable.recycle()
            }

            val bitmap = bitmaps.firstOrNull { it.first == sticker.resId }?.second ?: throw IllegalArgumentException("no bitmap!?")

            when(stickerState[sticker.id]) {
                StickerState.CREATING -> {
                    val factor = StickerTasks.getTaskCompletionFactor(sticker.id)
                    if(factor == 1f) {
                        stickerState.put(sticker.id, StickerState.NORMAL)
                        drawNormal(sticker, canvas, bitmap)
                    } else {
                        val matrix = Matrix().apply {
                            preScale(sticker.scaleFactor * factor, sticker.scaleFactor * factor)
                            preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
                            val translateX = sticker.xFactor * width - (sticker.scaleFactor * factor * width / 2)
                            val translateY = sticker.yFactor * height - (sticker.scaleFactor * factor * width / 2)
                            postTranslate(translateX, translateY)
                        }
                        paint.alpha = (255 * factor).toInt()
                        canvas?.drawBitmap(bitmap, matrix, paint)
                        invalidate()
                    }
                }
                StickerState.OVER_BIN -> {
                    val factor = StickerTasks.getTaskCompletionFactor(sticker.id)
                    if(factor != 1f) {
                        val scaleFactor = sticker.scaleFactor - sticker.scaleFactor * factor
                        val matrix = Matrix().apply {
                            preScale(scaleFactor, scaleFactor)
                            preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
                            var translateX = sticker.xFactor * width
                            var translateY = sticker.yFactor * height

//                            SwitchLog.scream("translateX: $translateX translateY: $translateY binRect.centerX: ${binRect.centerX()} binRect.centerY: ${binRect.centerY()}")
                            val binX = binRect.centerX() - IntArray(2).apply { getLocationOnScreen(this) }[0]
                            val binY = binRect.centerY() - IntArray(2).apply { getLocationOnScreen(this) }[1]
                            translateX += (binX - translateX) * factor - (scaleFactor * width / 2)
                            translateY += (binY - translateY) * factor - (scaleFactor * width / 2)
//                            SwitchLog.scream("new translateX: $translateX new translateY: $translateY")

                            postTranslate(translateX, translateY)
                        }
                        paint.alpha = 160 - (160 * factor).toInt()
                        canvas?.drawBitmap(bitmap, matrix, paint)
                        invalidate()
                    }
                }
                StickerState.OUT_BIN -> {
                    val factor = StickerTasks.getTaskCompletionFactor(sticker.id)
                    val scaleFactor = sticker.scaleFactor * factor
                    val matrix = Matrix().apply {
                        preScale(scaleFactor, scaleFactor)
                        preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
                        var translateX = sticker.xFactor * width
                        var translateY = sticker.yFactor * height

                        SwitchLog.scream("translateX: $translateX translateY: $translateY binRect.centerX: ${binRect.centerX()} binRect.centerY: ${binRect.centerY()}")
                        val binX = binRect.centerX() - IntArray(2).apply { getLocationOnScreen(this) }[0]
                        val binY = binRect.centerY() - IntArray(2).apply { getLocationOnScreen(this) }[1]

                        translateX = binX + (translateX - binX) * factor - (scaleFactor * width / 2)
                        translateY = binY + (translateY - binY) * factor - (scaleFactor * width / 2)

                        SwitchLog.scream("new translateX: $translateX new translateY: $translateY")

                        postTranslate(translateX, translateY)
                    }
                    paint.alpha = (160 * factor).toInt()
                    SwitchLog.scream("alpha: ${paint.alpha}")
                    canvas?.drawBitmap(bitmap, matrix, paint)
                    if(factor == 1f) stickerState.put(sticker.id, StickerState.NORMAL)
                    invalidate()
                }
                StickerState.NORMAL -> {
                    drawNormal(sticker, canvas, bitmap)
                }
            }
        }
    }

    private fun drawNormal(sticker: Sticker, canvas: Canvas?, bitmap: Bitmap) {
        val matrix = Matrix().apply {
            preScale(sticker.scaleFactor, sticker.scaleFactor)
            preRotate(sticker.angle, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())
            val translateX = sticker.xFactor * width - (sticker.scaleFactor * width / 2)
            val translateY = sticker.yFactor * height - (sticker.scaleFactor * width / 2)
            postTranslate(translateX, translateY)
        }

        canvas?.drawBitmap(bitmap, matrix, null)
    }

    private var currentSticker: Sticker? = null
    private var currentTouchDistanceFromCenterX = 0
    private var currentTouchDistanceFromCenterY = 0

    private var primaryStart: Point = Point()
    private var secondaryStart: Point = Point()
    private var startAngle: Float = 0f
    private var pointerStartAngle = 0f
    private var startScaleFactor = 0f
    private var startDistance: Int = 0

    private var binRect = Rect()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
//                SwitchLog.scream("DOWN x: ${event.x} y: ${event.y}")
                stickers.lastOrNull {
                    calculateRect(it, width, height).contains(event.x.toInt(), event.y.toInt())
                }?.let {
//                    SwitchLog.scream("Sticker match!")
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
//                SwitchLog.scream("POINTER_DOWN x: ${event.x} y: ${event.y}")
                currentSticker?.let {
                    val primaryPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
                    val secondaryPoint = Point(event.getX(1).toInt(), event.getY(1).toInt())
                    startDistance = calculateDistance(primaryPoint, secondaryPoint)

                    secondaryStart = Point(event.getX(1).toInt(), event.getY(1).toInt())
                    startAngle = it.angle
                    pointerStartAngle = calcRotationAngleInDegrees(primaryPoint, secondaryPoint)
                    startScaleFactor = it.scaleFactor
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
//                SwitchLog.scream("MOVE x: ${event.x} y: ${event.y}")

                if(currentSticker==null) return false

                if(event.pointerCount == 2) {
                    val primaryPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
                    val secondaryPoint = Point(event.getX(1).toInt(), event.getY(1).toInt())

                    val currentAngle = calcRotationAngleInDegrees(primaryPoint, secondaryPoint)
                    val angleChange = pointerStartAngle - currentAngle
                    val newAngle = correctAngle(startAngle - angleChange)

                    val currentDistance = calculateDistance(primaryPoint, secondaryPoint)
                    val distanceChange =  currentDistance - startDistance
                    val startStickerSize = (startScaleFactor * width).toInt()

                    val newStickerSize: Int = startStickerSize + distanceChange
                    val newStickerScaleFactor: Float = newStickerSize.toFloat() / width.toFloat()
                    val newScaleFactorBounded = Math.min(Math.max(newStickerScaleFactor, MIN_SCALE_FACTOR), MAX_SCALE_FACTOR)

                    currentSticker?.scaleFactor = newScaleFactorBounded
                    currentSticker?.angle = newAngle

                    SwitchLog.scream(
//                                                            "startDistance: $startDistance " +
//                                "distanceChange: ${distanceChange.toFloat()} " +
//                                "width: ${width.toFloat()} " +
//                                "origScaleSize: $scaleFactor " +
//                                "startStickerSize: $startStickerSize " +
//                                "newStickerSize: $newStickerSize " +
//                                "newStickerScaleFactor: $newStickerScaleFactor " +
//                                "newScaleFactorBounded: $newScaleFactorBounded " +
//                            "startAngle: $startAngle " +
//                                    "currentAngle: $currentAngle " +
//                                    "angleChange: $angleChange " +
//                                    "newAngle: $newAngle"
                    )
                }

                currentSticker?.xFactor = (event.x + currentTouchDistanceFromCenterX) / width
                currentSticker?.yFactor = (event.y + currentTouchDistanceFromCenterY) / height

                val listener = context as Listener
                if(event.pointerCount == 1) {
                    binRect = listener.onDragging(true)
                    val isOverBin = binRect.contains(event.rawX.toInt(), event.rawY.toInt())
                    listener.onOverBin(isOverBin)

                    val stickerId = currentSticker!!.id
                    if(isOverBin) {
                        if(stickerState[stickerId] == StickerState.NORMAL || stickerState[stickerId] == StickerState.OUT_BIN) {
                            stickerState.put(stickerId, StickerState.OVER_BIN)
                            StickerTasks.startTask(stickerId)
                        }
                    } else {
                        if(stickerState[stickerId] == StickerState.OVER_BIN) {
                            stickerState.put(stickerId, StickerState.OUT_BIN)
                            StickerTasks.startTask(stickerId)
                        }
                    }
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
                    currentSticker?.let { sticker ->
                        listener.onDeleteSticker(sticker)
                        bitmaps.firstOrNull { it.first == sticker.resId }?.second?.recycle()
                        bitmaps.removeAll { it.first == sticker.resId }
                        stickers.remove(sticker)
                        invalidate()
                    }
                }

                currentSticker = null
                currentTouchDistanceFromCenterX = 0
                currentTouchDistanceFromCenterY = 0
                primaryStart = Point()
                secondaryStart = Point()
                startAngle = 0f
                pointerStartAngle = 0f
                startScaleFactor = 0f
                startDistance = 0
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                SwitchLog.scream("ACTION_POINTER_UP x: ${event.x} y: ${event.y} index: ${event.actionIndex}")

                if(event.actionIndex == 0) {

                    secondaryStart = Point()
                    startAngle = 0f
                    pointerStartAngle = 0f
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
        while(newAngle < 0) {
            newAngle += 360
        }
        while(newAngle > 360) {
            newAngle -= 360
        }
        return newAngle
    }

    private fun calcRotationAngleInDegrees(centerPt: Point, targetPt: Point): Float {
        var theta = Math.atan2((targetPt.y - centerPt.y).toDouble(), (targetPt.x - centerPt.x).toDouble())
        theta += Math.PI / 2.0
        var angle = Math.toDegrees(theta)

        correctAngle(angle.toFloat())

        return angle.toFloat()
    }
}