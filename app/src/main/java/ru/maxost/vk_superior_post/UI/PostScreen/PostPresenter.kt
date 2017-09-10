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
        //ui
        fun setGalleyList(list: List<File>)
        fun showGalleryPanel(show: Boolean)
        fun setSelectedBackground(background: Background?)
        fun setSelectedGalleryImage(file: File?)
        fun enableSubmitButton(enable: Boolean)

        //post
        fun setText(text: String)
        fun setTextStyle(textStyle: TextStyle)
        fun setBackground(background: Background)

        //other
        fun showUploadScreen(post: Post)
        fun showGallery()
        fun takePhoto()
        fun showStickerPickerDialog()
    }

    @State var post: Post = Post()
    @State var lastSelectedGalleryImage: File? = null

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)

        this.view?.apply {
            setText(post.text)
            setTextStyle(post.textStyle)
            setBackground(post.background)
        }
    }

    fun onTextInput(text: String) {
        post.text = text
        view?.enableSubmitButton(text.isNotBlank())
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

        val previousWasNotImage = post.background.type != BackgroundType.IMAGE
        post.background = background

        view?.setSelectedBackground(post.background)
        view?.setBackground(post.background)

        if(background.type == BackgroundType.IMAGE) {
            view?.showGalleryPanel(true)

            dataManger.getImagesFromGallery()
                    .subscribe({
                        if(it.isNotEmpty() && previousWasNotImage) {
                            val file = lastSelectedGalleryImage ?: it.first()
                            post.background = Background(type = BackgroundType.IMAGE, imageFile = file)
                            view?.setBackground(post.background)
                            view?.setSelectedGalleryImage(file)
                        }
                        view?.setGalleyList(it)
                    }, {
                        it.printStackTrace()
                    })
        } else {
            view?.setSelectedGalleryImage(null)
        }

        //text stuff
        if(post.textStyle==TextStyle.BLACK
                && post.background.colorDrawableResId == R.drawable.background_white_full
                && post.background != background) {
            post.textStyle = TextStyle.WHITE
            view?.setTextStyle(post.textStyle)
        }
        if(post.textStyle==TextStyle.WHITE
                && background.type == BackgroundType.COLORED
                && background.colorDrawableResId == R.drawable.background_white_full) {
            post.textStyle = TextStyle.BLACK
            view?.setTextStyle(post.textStyle)
        }
    }

    fun onFileSelected(file: File) {
        post.background = Background(type = BackgroundType.IMAGE, imageFile = file)

        view?.setSelectedGalleryImage(file)
        view?.setSelectedBackground(post.background)
        view?.setBackground(post.background)
        lastSelectedGalleryImage = file
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