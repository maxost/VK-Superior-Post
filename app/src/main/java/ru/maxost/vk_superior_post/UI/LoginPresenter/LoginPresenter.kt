package ru.maxost.vk_superior_post.UI.LoginPresenter

import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Utils.BasePresenter
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * dustlooped@yandex.ru
 */
class LoginPresenter @Inject constructor(private val dataManger: DataManger)
    : BasePresenter<LoginPresenter.View>() {

    interface View {
        fun executeSdkLogin()
        fun showPostScreen()
        fun close()
    }

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)
        dataManger.isLoggedIn()
                .subscribe({ isLoggedIn ->
                    if(isLoggedIn) {
                        this@LoginPresenter.view?.showPostScreen()
                        this@LoginPresenter.view?.close()
                    } else {
                        // do nothing
                    }
                }, {
                    it.printStackTrace()
                }).addToDisposables()
    }

    fun onVkLoginClick() = view?.executeSdkLogin()

    fun onVkLoginSuccess() {
        view?.showPostScreen()
        view?.close()
    }
}