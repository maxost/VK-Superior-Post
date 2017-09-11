package ru.maxost.vk_superior_post.Utils

import android.support.v4.content.ContextCompat
import android.widget.TextView
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R

/**
 * Created by Maxim Ostrovidov on 08.09.17.
 * (c) White Soft
 */

fun TextView.setTextStyle(textStyle: TextStyle) {
    setShadowLayer(0f, 0f, 0f, 0)
    if(text.isBlank()) return

    when(textStyle) {
        TextStyle.BLACK -> {
            setTextColor(ContextCompat.getColor(this.context, R.color.black))
        }
        TextStyle.WHITE -> {
            setTextColor(ContextCompat.getColor(this.context, R.color.white))
            setShadowLayer(1f, 0f, 0.5f, ContextCompat.getColor(context, R.color.shadow))
        }
        TextStyle.BLACK_WITH_BACKGROUND -> {
            setTextColor(ContextCompat.getColor(this.context, R.color.blue))
        }
        TextStyle.WHITE_WITH_BACKGROUND -> {
            setTextColor(ContextCompat.getColor(this.context, R.color.blue))
            setShadowLayer(1f, 0f, 0.5f, ContextCompat.getColor(context, R.color.shadow))
        }
    }
}