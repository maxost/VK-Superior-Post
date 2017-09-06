package ru.maxost.vk_superior_post.UI.PostScreen

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vk.sdk.api.photo.VKUploadImage
import kotlinx.android.synthetic.main.activity_post.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKSdk
import com.vk.sdk.api.model.VKPhotoArray
import com.vk.sdk.api.photo.VKImageParameters
import es.dmoral.toasty.Toasty
import ru.maxost.switchlog.SwitchLog
import com.vk.sdk.api.VKError
import com.vk.sdk.api.VKResponse
import com.vk.sdk.api.VKRequest.VKRequestListener
import com.vk.sdk.api.model.VKWallPostResult
import com.vk.sdk.api.VKApi
import com.vk.sdk.api.VKRequest
import com.vk.sdk.api.VKApiConst
import com.vk.sdk.api.VKParameters
import com.vk.sdk.api.model.VKAttachments
import ru.maxost.vk_superior_post.R


class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        activity_post_logout_button.setOnClickListener {
            VKSdk.logout()
        }

        activity_post_send_button.setOnClickListener {
            val bitmap = loadBitmapFromView(activity_post_root_layout)
            val image = VKUploadImage(bitmap, VKImageParameters.jpgImage(1f))
            val userId = VKAccessToken.currentToken().userId.toInt()
            VKApi.uploadWallPhotoRequest(image, userId.toLong(), 0).executeWithListener(object : VKRequest.VKRequestListener() {
                override fun attemptFailed(request: VKRequest?, attemptNumber: Int, totalAttempts: Int) {
                    bitmap.recycle()
                    Toasty.error(this@PostActivity, "network error").show()
                }

                override fun onComplete(response: VKResponse?) {
                    bitmap.recycle()
                    val photo = (response!!.parsedModel as VKPhotoArray)[0]
                    makePost(VKAttachments(photo), "test", userId)
                }

                override fun onProgress(progressType: VKRequest.VKProgressType?, bytesLoaded: Long, bytesTotal: Long) {

                }

                override fun onError(error: VKError?) {
                    bitmap.recycle()
                    SwitchLog.log(error.toString())
                    Toasty.error(this@PostActivity, error.toString()).show()
                }
            })
        }
    }

    fun makePost(att: VKAttachments, msg: String, ownerId: Int) {
        val parameters = VKParameters()
        parameters.put(VKApiConst.OWNER_ID, ownerId.toString())
        parameters.put(VKApiConst.ATTACHMENTS, att)
        parameters.put(VKApiConst.MESSAGE, msg)
        val post = VKApi.wall().post(parameters)
        post.setModelClass(VKWallPostResult::class.java)
        post.executeWithListener(object : VKRequestListener() {
            override fun onComplete(response: VKResponse?) {
                Toasty.success(this@PostActivity, "done").show()
            }

            override fun onError(error: VKError?) {
                Toasty.error(this@PostActivity, error.toString()).show()
            }
        })
    }

    private fun loadBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)
        return bitmap
    }
}
