package ru.maxost.vk_superior_post.Utils

import android.content.Context

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
fun Int.dp2px(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.px2dp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun Float.dp2px(context: Context): Float = this * context.resources.displayMetrics.density

fun Float.px2dp(context: Context): Float = this / context.resources.displayMetrics.density