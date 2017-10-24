package ru.maxost.vk_superior_post.UI.UploadScreen

import com.evernote.android.state.State
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Utils.BasePresenter
import java.io.Serializable
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 07.09.17.
 * dustlooped@yandex.ru
 */
class UploadPresenter @Inject constructor(private val dataManger: DataManger)
    : BasePresenter<UploadPresenter.View>() {

    interface View {
        fun updateView(viewModel: ViewModel)
        fun showNewPostScreen()
        fun close()
    }

    data class ViewModel(var state: ViewState = ViewModel.ViewState.LOADING): Serializable {
        enum class ViewState { LOADING, SUCCESS, ERROR }
    }

    @State var viewModel = ViewModel()

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)
        this.view?.updateView(viewModel)
        if(viewModel.state == ViewModel.ViewState.LOADING) postImage()
    }

    fun onCancelRetryButtonClick() {
        when(viewModel.state) {
            UploadPresenter.ViewModel.ViewState.ERROR -> postImage()
            UploadPresenter.ViewModel.ViewState.LOADING -> view?.close()
            else -> {}
        }
    }

    fun onNewPostButtonClick() = view?.showNewPostScreen()

    private fun postImage() {
        dataManger.postImage()
                .doOnSubscribe {
                    viewModel.state = ViewModel.ViewState.LOADING
                    view?.updateView(viewModel)
                }
                .subscribe({
                    viewModel.state = ViewModel.ViewState.SUCCESS
                    view?.updateView(viewModel)
                }, {
                    it.printStackTrace()
                    viewModel.state = ViewModel.ViewState.ERROR
                    view?.updateView(viewModel)
                }).addToDisposables()
    }
}