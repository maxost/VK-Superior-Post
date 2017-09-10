package ru.maxost.vk_superior_post.UI.PostScreen

import android.Manifest
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.evernote.android.state.StateSaver
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.tbruyelle.rxpermissions2.RxPermissions
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.*
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterGridLayoutManager
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterLinearLayoutManager
import java.io.File
import android.R.attr.y
import android.R.attr.x
import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import ru.maxost.switchlog.SwitchLog
import android.content.Context.INPUT_METHOD_SERVICE
import android.support.constraint.ConstraintLayout
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import ru.maxost.vk_superior_post.Model.*
import ru.maxost.vk_superior_post.Utils.KeyboardHeight.KeyboardHeightActivity
import ru.maxost.vk_superior_post.Utils.KeyboardHeight.KeyboardHeightProvider

class PostActivity : PostPresenter.View, StickerListDialogFragment.Listener, KeyboardHeightActivity() {

    private val presenter by lazy { App.graph.getPostPresenter() }

    private var keyboardHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        initListeners()
        initBackgroundsList()
        initGalleryList()
        initPresenter(savedInstanceState)

        //prevent editText gain focus
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        activity_post_text.clearFocus()
        activity_post_root_layout.requestFocus()

        if (savedInstanceState == null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            activity_post_text.requestFocus()
        }

        activity_post_root_layout.viewTreeObserver.addOnGlobalLayoutListener {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            val accessibleHeight = size.y
            val diff = accessibleHeight - activity_post_root_layout.height
            presenter.onKeyboardShow(diff > 120.dp2px(this))
            SwitchLog.scream("accessibleHeight: $accessibleHeight layout.height: ${activity_post_root_layout.height} diff: ${diff.px2dp(this)}")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(presenter, outState)
    }

    override fun onBackPressed() {
        if (!presenter.onBack()) super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        SwitchLog.scream("onKeyboardHeightChanged: $height")
        if(height > 0) {
            keyboardHeight = height
            activity_post_compose_root_layout.y -= keyboardHeight / 2
        } else {
            activity_post_compose_root_layout.y += keyboardHeight / 2
        }

        activity_post_bottom_panel.layoutParams = (activity_post_bottom_panel.layoutParams as ConstraintLayout.LayoutParams).apply {
            setMargins(0, 0, 0, height)
        }
    }

    override fun showStickerPickerDialog() {
        StickerListDialogFragment.newInstance(24)
                .show(supportFragmentManager, null)
    }

    override fun setPostType(postType: PostType) {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        SwitchLog.scream(size.toString())

        when (postType) {
            PostType.POST -> {
                activity_post_compose_root_layout.layoutParams =
                        activity_post_compose_root_layout.layoutParams
                                .apply {
                                    SwitchLog.scream("1 ${height}")
                                    height = size.x
                                    SwitchLog.scream("2 ${height}")
                                }
            }
            PostType.STORY -> {
                activity_post_compose_root_layout.layoutParams =
                        activity_post_compose_root_layout.layoutParams
                                .apply {
                                    SwitchLog.scream("3 ${height}")
                                    height = 0
                                    SwitchLog.scream("4 ${height}")
                                }
            }
        }

        activity_post_compose_root_layout.requestLayout()
    }

    override fun showGallery() {
        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .filter { it }
                .flatMap { RxImagePicker.with(this).requestImage(Sources.GALLERY) }
                .subscribe({ uri ->
                    //TODO get file instead of uri and show selection in list if match
                    Glide.with(this)
                            .load(uri)
                            .fitCenter()
                            .into(activity_post_compose_background_center)
                    getGalleryAdapter().setSelectedFile(null)
                }, {
                    it.printStackTrace()
                    Toasty.error(this, "Произошла ошибка").show()
                })
    }

    override fun takePhoto() {
        RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .filter { it }
                .flatMap { RxImagePicker.with(this).requestImage(Sources.CAMERA) }
                .subscribe({ uri ->
                    Glide.with(this)
                            .load(uri)
                            .fitCenter()
                            .into(activity_post_compose_background_center)
                    getGalleryAdapter().setSelectedFile(null)
                }, {
                    it.printStackTrace()
                    Toasty.error(this, "Произошла ошибка").show()
                })
    }

    override fun showUploadScreen(post: Post) = UploadActivity.start(this, post)

    override fun onStickerClicked(stickerId: Int) = presenter.onStickerClick(stickerId)

    override fun setTextStyle(textStyle: TextStyle) {
        activity_post_text.setTextStyle(textStyle)
    }

    override fun setText(text: String) {
        activity_post_text.text = text.toEditable()
    }

    override fun setBackground(background: Background) {
        activity_post_compose_background_top.setImageResource(0)
        activity_post_compose_background_bottom.setImageResource(0)

        when (background.type) {
            BackgroundType.COLORED -> {
                activity_post_compose_background_center.setImageResource(background.colorDrawableResId!!)
            }
            BackgroundType.BEACH -> {
                Glide.with(this)
                        .load(R.drawable.bg_beach_center)
                        .into(activity_post_compose_background_center)
                Glide.with(this)
                        .load(R.drawable.bg_beach_top)
                        .into(activity_post_compose_background_top)
                Glide.with(this)
                        .load(R.drawable.bg_beach_bottom)
                        .into(activity_post_compose_background_bottom)
            }
            BackgroundType.STARS -> {
                Glide.with(this)
                        .load(R.drawable.bg_stars_center) //TODO must load as pattern image
                        .into(activity_post_compose_background_center)
            }
            BackgroundType.IMAGE -> {
                Glide.with(this)
                        .load(background.imageFile)
                        .centerCrop()
                        .into(activity_post_compose_background_center)
            }
        }
    }

    override fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun setGalleyList(list: List<File>) {
        getGalleryAdapter().setNewData(list)
    }

    override fun showGalleryPanel(show: Boolean) {
        activity_post_gallery_list.show(show)
        if(keyboardHeight >= activity_post_gallery_list.height) {
            activity_post_gallery_list.layoutParams = activity_post_gallery_list.layoutParams.apply {
                height = keyboardHeight
            }
        }
        SwitchLog.scream("activity_post_compose_root_layout.y: ${activity_post_compose_root_layout.y}")
        if(show) {
            activity_post_compose_root_layout.y -= keyboardHeight / 2
            SwitchLog.scream("activity_post_compose_root_layout.y: ${activity_post_compose_root_layout.y}")
        } else {
            activity_post_compose_root_layout.y += keyboardHeight / 2
            SwitchLog.scream("activity_post_compose_root_layout.y: ${activity_post_compose_root_layout.y}")
        }
    }

    override fun enableSubmitButton(enable: Boolean) {
        activity_post_submit_button.isEnabled = enable
    }

    override fun setSelectedGalleryImage(file: File?) {
        getGalleryAdapter().setSelectedFile(file)
    }

    override fun setSelectedBackground(background: Background?) {
        (activity_post_backgrounds_list.adapter as BackgroundsAdapter).setSelectedItem(background)
    }

    private fun initPresenter(savedInstanceState: Bundle?) {
        StateSaver.restoreInstanceState(presenter, savedInstanceState)
        presenter.attach(this, savedInstanceState == null)
    }

    private fun initListeners() {
        activity_post_submit_button.setOnClickListener { presenter.onSubmitClick() }
        activity_post_stickers_clickbox.setOnClickListener { presenter.onStickerPickerClick() }
        activity_post_text.onTextChanged {
            if (it.isBlank()) activity_post_text.gravity = Gravity.START
            else activity_post_text.gravity = Gravity.CENTER
            presenter.onTextInput(it)
        }
        activity_post_text_style_clickbox.setOnClickListener { presenter.onTextStyleClick() }
        activity_post_type_post.setOnClickListener { presenter.onPostTypeChange(PostType.POST) }
        activity_post_type_story.setOnClickListener { presenter.onPostTypeChange(PostType.STORY) }

        keyboardHeightProvider = KeyboardHeightProvider(this)
        activity_post_root_layout.post({ keyboardHeightProvider.start() })
    }

    private fun initBackgroundsList() {
        CenterLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false).apply {
            activity_post_backgrounds_list.layoutManager = this
        }
        activity_post_backgrounds_list.adapter = backgroundsAdapter(
                context = this,
                onItemClick = { background ->
                    if (background.type == BackgroundType.IMAGE) {
                        RxPermissions(this)
                                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .subscribe({ granted ->
                                    if (granted) {
                                        presenter.onBackgroundSelected(background)
                                    } else {
                                        activity_post_gallery_list.show(true)
                                    }
                                })
                    } else {
                        presenter.onBackgroundSelected(background)
                    }
                }
        )
    }

    private fun getGalleryAdapter() = activity_post_gallery_list.adapter as GalleryAdapter

    private fun initGalleryList() {
        CenterGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false).apply {
            activity_post_gallery_list.layoutManager = this
        }
        activity_post_gallery_list.adapter = galleryAdapter(
                context = this,
                onItemClick = { presenter.onFileSelected(it) },
                onOpenGalleryClick = { presenter.onOpenGalleryClick() },
                onTakePhotoClick = { presenter.onTakePhotoClick() }
        )
        activity_post_gallery_list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                val position = parent!!.getChildLayoutPosition(view)
                if (position % 2 == 1) {
                    outRect.top = 4.dp2px(this@PostActivity)
                }
                if (position > getGalleryAdapter().getItemsCount() - 3) {
                    outRect.right = 0
                } else {
                    outRect.right = 8.dp2px(this@PostActivity)
                }
            }
        })
    }
}
