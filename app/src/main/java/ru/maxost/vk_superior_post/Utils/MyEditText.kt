package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.R

/**
 * Created by Maksim Ostrovidov on 10.09.17.
 * dustlooped@yandex.ru
 */
class MyEditText @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : AppCompatEditText(context, attributeSet) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val color1 = ContextCompat.getColor(context, R.color.cornFlowerBlueTwoTransparent)
    private val color2 = ContextCompat.getColor(context, R.color.shadow)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

//        var line = 0
//        while (line < lineCount) {
//            val rect = Rect()
//            getLineBounds(line, rect)
//            paint.color = if(line % 2 ==0) color1 else color2
//            canvas?.drawRect(rect, paint)
//            SwitchLog.scream("$rect")
//            line++
//        }
    }
}