package ru.maxost.vk_superior_post.UI.PostScreen

import android.os.Bundle
import android.widget.Switch
import com.evernote.android.state.Bundler
import com.evernote.android.state.State
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Model.Sticker
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.BasePresenter
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * (c) White Soft
 */
class PostPresenter @Inject constructor(private val dataManger: DataManger)
    : BasePresenter<PostPresenter.View>() {

    interface View {
        fun setText(text: String)
        fun setTextStyle(textStyle: TextStyle)
        fun showUploadScreen(post: Post)
        fun showStickerPickerDialog()
        fun setBackground(file: File)
    }

    @State var post: Post = Post(stickers = Stack())

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)
        this.view?.apply {
            setText(post.text)
            setTextStyle(post.textStyle)
        }

        dataManger.getImagesFromGallery()
                .subscribe({
                    SwitchLog.log(it.toString())
                    this.view?.setBackground(it.first())
                }, {
                    it.printStackTrace()
                })
    }

    fun onFilePermissionGranted() {
        dataManger.getImagesFromGallery()
                .subscribe({
                    SwitchLog.log(it.toString())
                    view?.setBackground(it.first())
                }, {
                    it.printStackTrace()
                })
    }

    fun onTextInput(text: String) {
        post.text = text
    }

    fun onTextStyleClick() {
        post.nextTextStyle()
        view?.setTextStyle(post.textStyle)
    }

    fun onStickerPickerClick() = view?.showStickerPickerDialog()

    fun onStickerClick(stickerId: Int) {
        post.stickers.add(Sticker(stickerId))
    }

    fun onSubmitClick() = view?.showUploadScreen(post)

    private fun Post.nextTextStyle(): TextStyle {
        textStyle = when (textStyle) {
            TextStyle.BLACK                 -> TextStyle.WHITE
            TextStyle.WHITE                 -> TextStyle.BLACK_WITH_BACKGROUND
            TextStyle.BLACK_WITH_BACKGROUND -> TextStyle.WHITE_WITH_BACKGROUND
            TextStyle.WHITE_WITH_BACKGROUND -> TextStyle.BLACK
        }

        return textStyle
    }
}