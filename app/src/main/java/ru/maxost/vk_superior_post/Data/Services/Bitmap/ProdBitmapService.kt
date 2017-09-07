package ru.maxost.vk_superior_post.Data.Services.Bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.provider.FontRequest
import android.provider.FontsContract
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import io.reactivex.Single
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.R
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import ru.maxost.vk_superior_post.Utils.dp2px


/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdBitmapService(private val context: Context): BitmapService {

    override fun createBitmap(post: Post): Single<Bitmap> = Single.fromCallable {

        //init
        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(ContextCompat.getColor(context, R.color.white))

        //text
        val textView = TextView(context)
        val textWidth = canvas.width - 48.dp2px(context)
        textView.layout(0, 0, textWidth, canvas.height)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        textView.setTextColor(ContextCompat.getColor(context, R.color.black))
        textView.text = post.text
        textView.typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        textView.gravity = Gravity.CENTER
        textView.isDrawingCacheEnabled = true
        canvas.drawBitmap(textView.drawingCache, 24f.dp2px(context), (canvas.height / 2 - textView.height / 2).toFloat(), null)

        return@fromCallable bitmap
    }
}