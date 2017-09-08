package ru.maxost.vk_superior_post.UI.PostScreen

import android.os.Bundle
import com.evernote.android.state.Bundler
import com.evernote.android.state.State
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Model.Sticker
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.BasePresenter
import java.util.*
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

    @State var post: Post = Post(stickers = Stack())

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)
        post.stickers.push(Sticker(R.drawable.sticker_fish_1))
        post.stickers.push(Sticker(R.drawable.sticker_fish_2))
        post.stickers.push(Sticker(R.drawable.sticker_fish_3))
    }

    fun onTextInput(text: String) {
        post.text = text
    }

    fun onSubmitClick() = view?.showUploadScreen(post)
}