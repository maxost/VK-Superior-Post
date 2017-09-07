package ru.maxost.vk_superior_post.Utils

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * (c) White Soft
 */
open class BasePresenter<V> {

    protected var view : V? = null

    private val disposables = CompositeDisposable()

    @CallSuper
    open fun attach(view : V, isInitialAttach: Boolean) {
        this.view = view
    }

    @CallSuper
    open fun detach() {
        disposables.clear()
        view = null
    }

    protected fun Disposable.addToDisposables() {
        disposables.add(this)
    }
}