package ru.maxost.vk_superior_post.Data.Services.Bitmap

import android.content.Context
import android.graphics.*
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
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Utils.dp2px


/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdBitmapService(private val context: Context): BitmapService {

    override fun createBitmap(post: Post): Single<Bitmap> = Single.fromCallable {

        //init
        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(ContextCompat.getColor(context, R.color.blue))

        //stickers
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, R.drawable.sticker_fish_5, options)

        val matrix = Matrix().apply {
            setRotate(90f)
            setTranslate(200f, 200f)
        }
        val sticker = BitmapFactory.decodeResource(context.resources, R.drawable.sticker_fish_5)
        val stickerScaled = Bitmap.createScaledBitmap(sticker, 200, 200, true)

        SwitchLog.scream("${stickerScaled.height} ${stickerScaled.width}")
        canvas.drawBitmap(stickerScaled, matrix, null)

        val frameLayout = FrameLayout(context)
        frameLayout.isDrawingCacheEnabled = true

        //text
        val textView = TextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        textView.text = post.text
        textView.typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        textView.gravity = Gravity.CENTER
        textView.setPadding(24.dp2px(context), 24.dp2px(context), 24.dp2px(context), 24.dp2px(context))
        frameLayout.addView(textView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        frameLayout.measure(
                View.MeasureSpec.makeMeasureSpec(canvas.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(canvas.height, View.MeasureSpec.EXACTLY))
        frameLayout.layout(0, 0, canvas.width, canvas.height)
        canvas.drawBitmap(frameLayout.drawingCache, 0f, 0f, null)

        return@fromCallable bitmap
    }
}