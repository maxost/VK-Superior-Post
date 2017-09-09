package ru.maxost.vk_superior_post.UI.PostScreen

import android.os.Bundle
import android.widget.Switch
import com.evernote.android.state.Bundler
import com.evernote.android.state.State
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Data.Services.File.FileService
import ru.maxost.vk_superior_post.Model.*
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
        fun showStickerPickerDialog()
        fun showUploadScreen(post: Post)
        fun showGallery()
        fun takePhoto()
        fun setGalleyList(list: List<File>)
        fun showGalleryPanel(show: Boolean)
        fun showKeyboard(show: Boolean)

        fun setText(text: String)
        fun setTextStyle(textStyle: TextStyle)
        fun setBackground(background: Background)
    }

    @State var post: Post = Post(stickers = Stack())

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)
        this.view?.apply {
            setText(post.text)
            setTextStyle(post.textStyle)
        }
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

    fun onBackgroundSelected(background: Background) {

        if(post.textStyle==TextStyle.WHITE) {
            post.textStyle = TextStyle.BLACK
            view?.setTextStyle(post.textStyle)
        }

        post.background = background
        view?.setBackground(post.background)

        if(background.type == BackgroundType.IMAGE) {
            view?.showKeyboard(false)
            view?.showGalleryPanel(true)

            dataManger.getImagesFromGallery()
                    .subscribe({
                        SwitchLog.log(it.toString())
                        view?.setGalleyList(it)
                    }, {
                        it.printStackTrace()
                    })
        }
    }

    fun onFileSelected(file: File) {
        post.background = Background(type = BackgroundType.IMAGE, imageFile = file)
        onBackgroundSelected(post.background)
    }

    fun onTakePhotoClick() = view?.takePhoto()

    fun onOpenGalleryClick() = view?.showGallery()

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