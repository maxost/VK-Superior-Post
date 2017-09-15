package ru.maxost.vk_superior_post.UI.PostScreen

import com.evernote.android.state.State
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Model.*
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.BasePresenter
import java.io.File
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
        fun closeKeyboard()
        fun setPostType(postType: PostType)
        fun shiftPostKeyboard(shift: Boolean)
        fun shiftPostGalleryList(shift: Boolean)
        fun shiftBottomPanelKeyboard(shift: Boolean)
        fun shiftBottomPanelGalleryList(shift: Boolean)

        //post
        fun setText(text: String)
        fun setTextStyle(textStyle: TextStyle)
        fun setBackground(background: Background)
        fun addSticker(sticker: Sticker)
        fun setStickers(stickers: Stack<Sticker>)

        //other
        fun showUploadScreen(post: Post)
        fun showGallery()
        fun takePhoto()
        fun showStickerPickerDialog()
    }

    @State var post: Post = Post()
    @State var lastSelectedGalleryImage: File? = null

    private var isKeyboardVisible: Boolean = false
    private var isBottomPanelVisible: Boolean = false

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)

        this.view?.apply {
            setText(post.text)
            setTextStyle(post.textStyle)
            setBackground(post.background)
            setSelectedBackground(post.background) //TODO scroll doesn't work on activity recreation
            if(isBottomPanelVisible) loadGalleryImages()
            setPostType(post.postType)
            setStickers(post.stickers)
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

    fun onPostTypeChange(postType: PostType) {
        if(post.postType == postType) return

        post.postType = postType
        view?.setPostType(post.postType)
    }

    fun onKeyboardShow(show: Boolean) {
        if(isKeyboardVisible == show) return

        if(show) {
            if(isBottomPanelVisible) {
                view?.showGalleryPanel(false)
                view?.shiftPostGalleryList(false)
                view?.shiftBottomPanelGalleryList(false)
                isBottomPanelVisible = false
            }
            view?.shiftPostKeyboard(true)
            view?.shiftBottomPanelKeyboard(true)
        } else {
            view?.shiftPostKeyboard(false)
            view?.shiftBottomPanelKeyboard(false)
        }

        isKeyboardVisible = show
    }

    fun onBack(): Boolean {
        return when {
            isKeyboardVisible -> false
            isBottomPanelVisible -> {
                view?.showGalleryPanel(false)
                view?.shiftPostGalleryList(false)
                view?.shiftBottomPanelGalleryList(false)
                isBottomPanelVisible = false
                true
            }
            else -> false
        }
    }

    fun onStickerPickerClick() = view?.showStickerPickerDialog()

    fun onStickerClick(stickerResId: Int) {
        val sticker = Sticker(id = UUID.randomUUID().toString(), resId = stickerResId)
        post.stickers.add(sticker)
        view?.setStickers(post.stickers)
    }

    fun onStickerDelete(sticker: Sticker) {
        post.stickers.remove(sticker)
    }

    fun onBackgroundSelected(background: Background) {
        view?.setSelectedBackground(background)

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

        if(background.type == BackgroundType.IMAGE) {
            onImageBackgroundSelected()
        } else {
            post.background = background
            view?.setBackground(post.background)
            view?.setSelectedGalleryImage(null)
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

    private fun onImageBackgroundSelected() {

        if(isBottomPanelVisible) {
            loadGalleryImages()
            return
        }

        if(isKeyboardVisible) {
            view?.closeKeyboard()
            view?.shiftPostKeyboard(false)
            view?.shiftBottomPanelKeyboard(false)
            isKeyboardVisible = false
        }

        view?.shiftPostGalleryList(true)
        view?.shiftBottomPanelGalleryList(true)
        view?.showGalleryPanel(true)
        isBottomPanelVisible = true

        loadGalleryImages()
    }

    private fun Post.nextTextStyle(): TextStyle {
        textStyle = when (textStyle) {
            TextStyle.BLACK                 -> TextStyle.BLACK_WITH_BACKGROUND
            TextStyle.BLACK_WITH_BACKGROUND -> TextStyle.WHITE
            TextStyle.WHITE                 -> TextStyle.WHITE_WITH_BACKGROUND
            TextStyle.WHITE_WITH_BACKGROUND -> TextStyle.BLACK
        }

        return textStyle
    }

    private fun loadGalleryImages() {
        dataManger.getImagesFromGallery()
                .subscribe({
                    val previousWasNotImage = post.background.type != BackgroundType.IMAGE
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
    }
}