package ru.maxost.vk_superior_post.Data.Services.Api

import android.graphics.Bitmap
import com.vk.sdk.api.model.VKApiPhoto
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
interface ApiService {
    fun isLoggedIn(): Single<Boolean>
    fun uploadImage(bitmap: Bitmap): Single<VKApiPhoto>
    fun wallPostPhoto(photo: VKApiPhoto): Completable
}