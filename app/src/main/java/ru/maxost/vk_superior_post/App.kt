package ru.maxost.vk_superior_post

import android.app.Application
import android.content.Context
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker
import com.vk.sdk.VKSdk

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        val tokenTracker = object: VKAccessTokenTracker() {
            override fun onVKAccessTokenChanged(oldToken: VKAccessToken?,
                                                newToken: VKAccessToken?) {
                //TODO ?
            }
        }

        VKSdk.initialize(this)
    }
}