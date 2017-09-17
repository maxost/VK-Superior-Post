package ru.maxost.vk_superior_post.Data.Services.File

import android.graphics.Bitmap
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
interface FileService {
    fun getImagesFromGallery(count: Int): Single<List<File>>
    fun storePost(bitmap: Bitmap): Completable
    fun getPost(): Single<Bitmap>
}