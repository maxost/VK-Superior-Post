package ru.maxost.vk_superior_post.Services.Api

import android.graphics.Bitmap
import com.vk.sdk.api.model.VKApiPhoto
import io.reactivex.Completable
import io.reactivex.Single
import ru.maxost.vk_superior_post.Data.Services.Api.ApiService
import java.util.concurrent.TimeUnit

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class FakeApiService: ApiService {

    override fun isLoggedIn(): Single<Boolean> = Single.just(true)

    override fun uploadImage(bitmap: Bitmap): Single<VKApiPhoto> {
        return Single.just(VKApiPhoto())
    }

    override fun wallPostPhoto(photo: VKApiPhoto): Completable {
        return Completable.complete()
                .delay(3, TimeUnit.SECONDS)
                .andThen(Completable.error(Exception()))
    }
}