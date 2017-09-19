package ru.maxost.vk_superior_post.Utils

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Created by Maxim Ostrovidov on 19.09.17.
 * (c) White Soft
 */
class GalleryItemFrameLayout @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : FrameLayout(context, attributeSet) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }
}

class GalleryItemImageView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : ImageView(context, attributeSet) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }
}