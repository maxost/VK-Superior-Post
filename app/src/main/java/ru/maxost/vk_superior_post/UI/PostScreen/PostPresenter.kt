package ru.maxost.vk_superior_post.UI.PostScreen

import com.evernote.android.state.State
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Utils.BasePresenter
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
class PostPresenter @Inject constructor(private val dataManger: DataManger)
    : BasePresenter<PostPresenter.View>() {

    interface View {
        fun showUploadScreen(post: Post)
    }

    @State var post: Post = Post()

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)

    }

    fun onTextInput(text: String) {
        post.text = text
    }

    fun onSubmitClick() = view?.showUploadScreen(post)
}