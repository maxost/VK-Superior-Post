package ru.maxost.vk_superior_post.UI.PostScreen

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.evernote.android.state.StateSaver
import com.mlsdev.rximagepicker.RxImagePicker
import com.mlsdev.rximagepicker.Sources
import com.tbruyelle.rxpermissions2.RxPermissions
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.Model.*
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.*
import ru.maxost.vk_superior_post.Utils.KeyboardHeight.KeyboardHeightActivity
import ru.maxost.vk_superior_post.Utils.KeyboardHeight.KeyboardHeightProvider
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterGridLayoutManager
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterLinearLayoutManager
import java.io.File
import java.util.*

class PostActivity : PostPresenter.View, StickerListDialogFragment.Listener, KeyboardHeightActivity(), StickerView.Listener {

    private val DEFAULT_ANIMATION_DURATION = 300.toLong()
    private val DEFAULT_INTERPOLATOR = AccelerateDecelerateInterpolator()

    private val presenter by lazy(LazyThreadSafetyMode.NONE) { App.graph.getPostPresenter() }
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(presenter, outState)
    }

    override fun onBackPressed() {
        if (!presenter.onBack()) super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        updateBorder()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        if (height > 0) {
            keyboardHeight = height
            presenter.onKeyboardShow(true)
        } else {
            presenter.onKeyboardShow(false)
        }
    }

    override fun showStickerPickerDialog() {
        closeKeyboard()
        StickerListDialogFragment.newInstance(24)
                .show(supportFragmentManager, null)
    }

    override fun setPostType(postType: PostType) {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)

        val opaqueWhite = ContextCompat.getColor(this, R.color.white)
        val transparentWhite = ContextCompat.getColor(this, R.color.whiteTransparentMore)

        when (postType) {
            PostType.POST -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    activity_post_bottom_panel.setPadding(0, 0, 0, 0)
                    activity_post_top_panel.setPadding(0, 0, 0 ,0)
                }

                //selector
                ViewCompat.animate(activity_post_type_selector)
                        .translationXBy(-activity_post_type_selector.width.toFloat())
                        .setDuration(300)
                        .start()

                //post
                ResizeAnimation(activity_post_compose_root_layout, activity_post_root_layout.height, size.x).apply {
                    duration = 300
                    activity_post_compose_root_layout.startAnimation(this)
                }

                //panels color
                ValueAnimator.ofObject(ArgbEvaluator(), transparentWhite, opaqueWhite).apply {
                    duration = 300
                    addUpdateListener {
                        activity_post_top_panel.setBackgroundColor(it.animatedValue as Int)
                        activity_post_bottom_panel.setBackgroundColor(it.animatedValue as Int)
                    }
                }.start()
            }
            PostType.STORY -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    activity_post_bottom_panel.setPadding(0, 0, 0, 50)
                    activity_post_top_panel.setPadding(0, 50, 0 ,0)
                }

                //selector
                ViewCompat.animate(activity_post_type_selector)
                        .translationXBy(activity_post_type_selector.width.toFloat())
                        .setDuration(300)
                        .start()

                //post
                ResizeAnimation(activity_post_compose_root_layout, size.x, activity_post_root_layout.height).apply {
                    duration = 300
                    activity_post_compose_root_layout.startAnimation(this)
                }

                //panels color
                ValueAnimator.ofObject(ArgbEvaluator(), opaqueWhite, transparentWhite).apply {
                    duration = 300
                    addUpdateListener {
                        activity_post_top_panel.setBackgroundColor(it.animatedValue as Int)
                        activity_post_bottom_panel.setBackgroundColor(it.animatedValue as Int)
                    }
                }.start()
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
                    val screenSize = Point()
                    windowManager.defaultDisplay.getSize(screenSize)

                    Glide.with(this)
                            .load(uri)
                            .override(screenSize.x, screenSize.y)
                            .centerCrop()
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
                    val screenSize = Point()
                    windowManager.defaultDisplay.getSize(screenSize)

                    Glide.with(this)
                            .load(uri)
                            .override(screenSize.x, screenSize.y)
                            .centerCrop()
                            .into(activity_post_compose_background_center)
                    getGalleryAdapter().setSelectedFile(null)
                }, {
                    it.printStackTrace()
                    Toasty.error(this, "Произошла ошибка").show()
                })
    }

    override fun showUploadScreen() = UploadActivity.start(this)

    override fun requestPostBitmap() {
        closeKeyboard()
        activity_post_text.isCursorVisible = false
        updateBorder()

        val bitmap = Bitmap.createBitmap(
                activity_post_compose_root_layout.width,
                activity_post_compose_root_layout.height,
                Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        activity_post_compose_root_layout.draw(canvas)
        presenter.onBitmapReady(bitmap)

        activity_post_text.isCursorVisible = true
    }

    override fun showError() {
        Toasty.error(this, getString(R.string.error)).show()
    }

    override fun onStickerClicked(stickerId: Int) {
        presenter.onStickerClick(stickerId)
        closeKeyboard()
    }

    override fun addSticker(sticker: Sticker) {
        activity_post_sticker_view.addSticker(sticker)
    }

    override fun setStickers(stickers: Stack<Sticker>) {
        activity_post_sticker_view.setStickers(stickers)
    }

    override fun setTextStyle(textStyle: TextStyle) {
        activity_post_text.textStyle = textStyle
        updateBorder()
    }

    override fun setText(text: String) {
        activity_post_text.text = text.toEditable()
        updateBorder()
    }

    override fun setBackground(background: Background) {
        activity_post_compose_background_top.setImageResource(0)
        activity_post_compose_background_bottom.setImageResource(0)

        val screenSize = Point()
        windowManager.defaultDisplay.getSize(screenSize)

        when (background.type) {
            BackgroundType.COLORED -> {
                activity_post_compose_background_center.setImageResource(background.colorDrawableResId!!)
            }
            BackgroundType.BEACH -> {
                Glide.with(this)
                        .load(R.drawable.bg_beach_center)
                        .asBitmap()
                        .format(DecodeFormat.PREFER_ARGB_8888)
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
                        .load(R.drawable.bg_stars_center)
                        .into(activity_post_compose_background_center)
//                activity_post_compose_background_center.setImageResource(R.drawable.drawable_stars)
            }
            BackgroundType.IMAGE -> {
                Glide.with(this)
                        .load(background.imageFile)
                        .override(screenSize.x, screenSize.y)
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

    override fun showGalleryPanel(show: Boolean, animate: Boolean) {
        val panelHeight = 208.dp2px(this).toFloat()

        ViewCompat.animate(activity_post_gallery_list)
                .translationYBy(if (show) -panelHeight else panelHeight)
                .setDuration(if (animate) DEFAULT_ANIMATION_DURATION else 0)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .start()

        ViewCompat.animate(activity_post_compose_root_layout)
                .translationYBy(if (show) -panelHeight / 2 else panelHeight / 2)
                .setDuration(if (animate) DEFAULT_ANIMATION_DURATION else 0)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .start()

        ViewCompat.animate(activity_post_bottom_panel)
                .translationYBy(if (show) -panelHeight else panelHeight)
                .setDuration(if (animate) DEFAULT_ANIMATION_DURATION else 0)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .start()
    }

    override fun shiftViewsForKeyboard(shift: Boolean) {
        if (shift) {
            activity_post_compose_root_layout.y -= keyboardHeight / 2
            activity_post_bottom_panel.y -= keyboardHeight
        } else {
            activity_post_compose_root_layout.y += keyboardHeight / 2
            activity_post_bottom_panel.y += keyboardHeight
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

    override fun onMultiTouch(enable: Boolean) {
        SwitchLog.scream("onMultiTouch: $enable")

        activity_post_text.isInterceptingTouches = !enable
//        activity_post_bin.show(false)
    }

    private var binVisible = false
    override fun onDragging(isDragging: Boolean): Rect {

        if (presenter.getCurrentBackground().type == BackgroundType.COLORED
                && presenter.getCurrentBackground().colorDrawableResId == R.drawable.background_white_full) {
            activity_post_bin.setBackgroundResource(R.drawable.drawable_bin_circle)
        } else {
            activity_post_bin.setBackgroundResource(R.drawable.drawable_bin)
        }

        activity_post_bin.layoutParams =
                (activity_post_bin.layoutParams as ConstraintLayout.LayoutParams)
                        .apply {
                            val bottomPanelHeight = 56.dp2px(this@PostActivity)
                            val galleryListHeight = 208.dp2px(this@PostActivity)
                            val margin = when {
                                presenter.isKeyboardVisible() -> keyboardHeight + bottomPanelHeight
                                presenter.isGalleryPanelVisible() -> galleryListHeight + bottomPanelHeight
                                presenter.isPostTypePost() -> {
                                    val postYLocation = IntArray(2).apply {
                                        activity_post_compose_root_layout.getLocationOnScreen(this)
                                    }[1]
                                    val postHeight = activity_post_compose_root_layout.height
                                    val postBottomY = postYLocation + postHeight

                                    val screenYLocation = IntArray(2).apply {
                                        activity_post_root_layout.getLocationOnScreen(this)
                                    }[1]
                                    val screenHeight = activity_post_root_layout.height
                                    val screenBottomY = screenYLocation + screenHeight

                                    val distanceFromBottom = screenBottomY - postBottomY
                                    val result = distanceFromBottom
                                    result
                                }
                                else -> bottomPanelHeight
                            }

                            bottomMargin = margin
                        }

        if (binVisible != isDragging) {
            SwitchLog.scream("isDragging: $isDragging")
            binVisible = isDragging
            val distance = 16.dp2px(this).toFloat()
            activity_post_root_layout.requestLayout()
            ViewCompat.animate(activity_post_bin)
                    .translationY(if (isDragging) -distance else distance / 2)
                    .alpha(if (isDragging) 1f else 0f)
                    .setDuration(200)
                    .setInterpolator(DEFAULT_INTERPOLATOR)
                    .setStartDelay(if (isDragging) 300 else 0)
                    .start()
        }

        val array = IntArray(2)
        activity_post_bin.getLocationOnScreen(array)
        return Rect(
                array[0],
                array[1],
                array[0] + activity_post_bin.width,
                array[1] + activity_post_bin.height
        )
    }

    private var binExpanded = false
    override fun onOverBin(isOverBin: Boolean) {
        if (binExpanded == isOverBin) return

        SwitchLog.scream("isOverBin: $isOverBin")
        binExpanded = isOverBin

        if (isOverBin) {
            activity_post_bin.setImageResource(R.drawable.ic_fab_trash_released)
        } else {
            activity_post_bin.setImageResource(R.drawable.ic_fab_trash)
        }

        ViewCompat.animate(activity_post_bin)
                .scaleX(if (isOverBin) 1.2f else 1f)
                .scaleY(if (isOverBin) 1.2f else 1f)
                .setDuration(200)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .setStartDelay(0)
                .start()
    }

    override fun onDeleteSticker(sticker: Sticker) {
        presenter.onStickerDelete(sticker)

        ViewCompat.animate(activity_post_bin)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .start()
    }

    private fun initPresenter(savedInstanceState: Bundle?) {
        StateSaver.restoreInstanceState(presenter, savedInstanceState)
        presenter.attach(this, savedInstanceState == null)
    }

    private fun initListeners() {
        activity_post_submit_button.setOnClickListener { presenter.onSubmitClick() }
        activity_post_stickers_clickbox.setOnClickListener { presenter.onStickerPickerClick() }
        activity_post_text.onTextChanged {
            if (it.isBlank()) {
                activity_post_text.gravity = Gravity.START
                activity_post_text.hint = getString(R.string.post_text_hint)
            } else {
                activity_post_text.gravity = Gravity.CENTER
                activity_post_text.hint = ""
            }
            presenter.onTextInput(it)
            Handler().postDelayed({ updateBorder() }, 1)
        }
        activity_post_text_style_clickbox.setOnClickListener { presenter.onTextStyleClick() }
        activity_post_type_post.setOnClickListener { presenter.onPostTypeChange(PostType.POST) }
        activity_post_type_story.setOnClickListener { presenter.onPostTypeChange(PostType.STORY) }

        keyboardHeightProvider = KeyboardHeightProvider(this)
        activity_post_root_layout.post({ keyboardHeightProvider.start() })

        activity_post_compose_root_layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateBorder()
        }
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

    private fun updateBorder() {
        activity_post_text_border.setState(activity_post_text.getState())
    }
}
