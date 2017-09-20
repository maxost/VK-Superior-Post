package ru.maxost.vk_superior_post.DI

import dagger.Component
import ru.maxost.vk_superior_post.UI.LoginPresenter.LoginPresenter
import ru.maxost.vk_superior_post.UI.PostScreen.PostPresenter
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadPresenter
import javax.inject.Singleton

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
@Singleton
@Component(modules = arrayOf(
        ApiModule::class,
        FileModule::class
))
interface AppComponent {
    fun getLoginPresenter(): LoginPresenter
    fun getPostPresenter(): PostPresenter
    fun getUploadPresenter(): UploadPresenter
}