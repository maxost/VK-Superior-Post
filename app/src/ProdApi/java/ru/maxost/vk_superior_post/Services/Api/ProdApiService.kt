package ru.maxost.vk_superior_post.Services.Api

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.vk.sdk.VKAccessToken
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VKApiPhoto
import com.vk.sdk.api.model.VKAttachments
import com.vk.sdk.api.model.VKPhotoArray
import com.vk.sdk.api.model.VKWallPostResult
import com.vk.sdk.api.photo.VKImageParameters
import com.vk.sdk.api.photo.VKUploadImage
import es.dmoral.toasty.Toasty
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Data.Services.Api.ApiService
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
class ProdApiService : ApiService {

    override fun isLoggedIn(): Single<Boolean> = Single.fromCallable {
        return@fromCallable VKAccessToken.currentToken() != null
    }

    override fun uploadImage(bitmap: Bitmap): Single<VKApiPhoto> = Single.create { source ->
        val image = VKUploadImage(bitmap, VKImageParameters.jpgImage(1f))
        val userId = VKAccessToken.currentToken().userId.toInt()
        VKApi.uploadWallPhotoRequest(image, userId.toLong(), 0)
                .executeWithListener(object : VKRequest.VKRequestListener() {
                    override fun onComplete(response: VKResponse?) {
                        if (!source.isDisposed) {
                            val photo: VKApiPhoto = (response!!.parsedModel as VKPhotoArray)[0]
                            source.onSuccess(photo)
                        }
                    }

                    override fun attemptFailed(request: VKRequest?, attemptNumber: Int, totalAttempts: Int) {
                        if (!source.isDisposed) source.onError(IOException())
                    }

                    override fun onError(error: VKError?) {
                        if (!source.isDisposed) source.onError(IOException())
                    }
                })
        source.setCancellable { bitmap.recycle() } //TODO test this
    }

    override fun wallPostPhoto(photo: VKApiPhoto): Completable = Completable.create { source ->
        val parameters = VKParameters()
        val userId = VKAccessToken.currentToken().userId
        parameters.put(VKApiConst.OWNER_ID, userId)
        parameters.put(VKApiConst.ATTACHMENTS, VKAttachments(photo))
        parameters.put(VKApiConst.MESSAGE, "VK Superior Post")
        val post = VKApi.wall().post(parameters)
        post.setModelClass(VKWallPostResult::class.java)
        post.executeWithListener(object : VKRequest.VKRequestListener() {
            override fun onComplete(response: VKResponse?) {
                if (!source.isDisposed) source.onComplete()
            }

            override fun onError(error: VKError?) {
                if (!source.isDisposed) source.onError(IOException())
            }
        })
    }
}