package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.view.View
import java.net.URI

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
fun URI.toAndroidUri(): Uri = Uri.parse(this.toString())

fun Uri.toJavaURI(): URI = URI(this.toString())

fun Int.dp2px(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.px2dp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun Float.dp2px(context: Context): Float = this * context.resources.displayMetrics.density

fun Float.px2dp(context: Context): Float = this / context.resources.displayMetrics.density

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun View.show(show: Boolean) {
    if (show) this.visibility = View.VISIBLE
    else this.visibility = View.GONE
}

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}