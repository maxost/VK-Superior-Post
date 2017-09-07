package ru.maxost.vk_superior_post.Data.Services.Bitmap

import android.graphics.Bitmap
import io.reactivex.Single
import ru.maxost.vk_superior_post.Model.Post

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
interface BitmapService {
    fun createBitmap(post: Post): Single<Bitmap>
}