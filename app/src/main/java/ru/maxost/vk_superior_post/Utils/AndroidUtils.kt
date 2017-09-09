package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.view.View
import java.net.URI
import android.app.Activity
import android.view.inputmethod.InputMethodManager


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

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}