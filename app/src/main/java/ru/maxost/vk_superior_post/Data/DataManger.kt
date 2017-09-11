package ru.maxost.vk_superior_post.Data

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.maxost.vk_superior_post.Data.Services.Api.ApiService
import ru.maxost.vk_superior_post.Data.Services.Bitmap.BitmapService
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.GALLERY_LAST_IMAGES_COUNT
import ru.maxost.vk_superior_post.Model.Post
import java.io.File
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class DataManger @Inject constructor(private val fileService: FileService,
                                     private val apiService: ApiService,
                                     private val bitmapService: BitmapService) {

    fun isLoggedIn(): Single<Boolean> {
        return apiService.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getImagesFromGallery(): Single<List<File>> {
        return fileService.getImagesFromGallery(GALLERY_LAST_IMAGES_COUNT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun createAndPostImage(post: Post): Completable {
        return bitmapService.createBitmap(post)
                .flatMap { apiService.uploadImage(it) }
                .flatMapCompletable { apiService.wallPostPhoto(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}