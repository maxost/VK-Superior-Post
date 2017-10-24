package ru.maxost.vk_superior_post.UI.PostScreen

import android.graphics.Bitmap
import com.evernote.android.state.State
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.Data.DataManger
import ru.maxost.vk_superior_post.Model.*
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.Utils.BasePresenter
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Inject

/**
 * Created by Maxim Ostrovidov on 06.09.17.
 * dustlooped@yandex.ru
 */
class PostPresenter @Inject constructor(private val dataManger: DataManger)
    : BasePresenter<PostPresenter.View>() {

    interface View {
        //ui
        fun setGalleyList(list: List<File>)
        fun showGalleryPanel(show: Boolean, animate: Boolean, shiftViews: Boolean)
        fun setSelectedBackground(background: Background?)
        fun setSelectedGalleryImage(file: File?)
        fun enableSubmitButton(enable: Boolean)
        fun closeKeyboard()
        fun setPostType(postType: PostType, animate: Boolean)
        fun updatePostSelector(postType: PostType)
        fun shiftViewsForKeyboard(shift: Boolean)
        fun requestPostBitmap()
        fun showError()

        //post
        fun setText(text: String)
        fun setTextStyle(textStyle: TextStyle)
        fun setBackground(background: Background)
        fun setBackground(uri: URI)
        fun addSticker(sticker: Sticker)
        fun setStickers(stickers: Stack<Sticker>)

        //other
        fun showUploadScreen()
        fun showGallery()
        fun takePhoto()
        fun showStickerPickerDialog()
    }

    @State var post: Post = Post()
    @State var lastSelectedGalleryImage: File? = null
    @State var keyboardHeight = 0
    @State var tempPhotoUri: URI? = null

    private var isKeyboardVisible: Boolean = false
    private var isBottomPanelVisible: Boolean = false

    override fun attach(view: View, isInitialAttach: Boolean) {
        super.attach(view, isInitialAttach)

        this.view?.apply {
            if(tempPhotoUri!=null && post.background.type == BackgroundType.IMAGE) {
                setBackground(tempPhotoUri!!)
            } else {
                setBackground(post.background)
            }

            setText(post.text)
            setTextStyle(post.textStyle)
            setSelectedBackground(post.background)
            if(isBottomPanelVisible) loadGalleryImages()
            setPostType(post.postType, false)
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
        view?.setPostType(post.postType, true)
    }

    fun onKeyboardShow(show: Boolean) {
        SwitchLog.scream("show: $show isKeyboardVisible: $isKeyboardVisible isBottomPanelVisible: $isBottomPanelVisible")
        if(isKeyboardVisible == show) return

        if(show) {
            if(!isBottomPanelVisible) view?.shiftViewsForKeyboard(true)
        } else {
            if(!isBottomPanelVisible) view?.shiftViewsForKeyboard(false)
        }

        isKeyboardVisible = show
    }

    fun onBack(): Boolean {
        return when {
            isKeyboardVisible -> false
            isBottomPanelVisible -> {
                view?.showGalleryPanel(false, true, true)
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
        view?.addSticker(sticker)
    }

    fun onStickerDelete(sticker: Sticker) {
        post.stickers.remove(sticker)
    }

    fun getCurrentBackground() = post.background

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
        tempPhotoUri = null
        post.background = Background(type = BackgroundType.IMAGE, imageFile = file)

        view?.setSelectedGalleryImage(file)
        view?.setSelectedBackground(post.background)
        view?.setBackground(post.background)
        lastSelectedGalleryImage = file
    }

    fun onTakePhotoClick() = view?.takePhoto()

    fun onOpenGalleryClick() = view?.showGallery()

    fun onSubmitClick() = view?.requestPostBitmap()

    fun onBitmapReady(bitmap: Bitmap) {
        dataManger.savePost(bitmap)
                .subscribe({
                    view?.showUploadScreen()
                }, {
                    it.printStackTrace()
                    view?.showError()
                })
    }

    fun isKeyboardVisible() = isKeyboardVisible
    fun isGalleryPanelVisible() = isBottomPanelVisible
    fun isPostTypePost() = post.postType == PostType.POST
    fun onPostSelectorReady() = view?.updatePostSelector(post.postType)

    private fun onImageBackgroundSelected() {

        val keyBoardWasVisible = isKeyboardVisible

        if(isKeyboardVisible) {
            view?.closeKeyboard()
            isKeyboardVisible = false
        }

        if(isBottomPanelVisible) {
            loadGalleryImages()
            return
        }

        view?.showGalleryPanel(true, !keyBoardWasVisible, !keyBoardWasVisible)
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
                    if(tempPhotoUri!=null) {
                        view?.setBackground(tempPhotoUri!!)
                        view?.setSelectedGalleryImage(null)
                        post.background = Background(type = BackgroundType.IMAGE, imageFile = null)
                    } else {
                        val previousWasNotImage = post.background.type != BackgroundType.IMAGE
                        if (it.isNotEmpty() && previousWasNotImage) {
                            val file = lastSelectedGalleryImage ?: it.first()
                            post.background = Background(type = BackgroundType.IMAGE, imageFile = file)
                            view?.setBackground(post.background)
                            view?.setSelectedGalleryImage(file)
                        }
                    }
                    view?.setGalleyList(it)
                }, {
                    it.printStackTrace()
                    view?.showError()
                })
    }
}