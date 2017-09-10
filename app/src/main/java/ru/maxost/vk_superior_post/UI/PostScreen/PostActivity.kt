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
import ru.maxost.vk_superior_post.Model.Background
import ru.maxost.vk_superior_post.Model.BackgroundType
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.*
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterGridLayoutManager
import ru.maxost.vk_superior_post.Utils.LayoutManagers.CenterLinearLayoutManager
import java.io.File


class PostActivity : AppCompatActivity(), PostPresenter.View, StickerListDialogFragment.Listener {

    private val presenter by lazy { App.graph.getPostPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        initListeners()
        initBackgroundsList()
        initGalleryList()
        initPresenter(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(presenter, outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun showStickerPickerDialog() {
        StickerListDialogFragment.newInstance(24)
                .show(supportFragmentManager, null)
    }

    override fun showGallery() {
        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .filter { it }
                .flatMap { RxImagePicker.with(this).requestImage(Sources.GALLERY) }
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

        when(background.type) {
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

    override fun setGalleyList(list: List<File>) {
        getGalleryAdapter().setNewData(list)
    }

    override fun showGalleryPanel(show: Boolean) {
        activity_post_gallery_list.show(show)
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
        presenter.attach(this, savedInstanceState==null)
    }

    private fun initListeners() {
        activity_post_submit_button.setOnClickListener { presenter.onSubmitClick() }
        activity_post_stickers_clickbox.setOnClickListener { presenter.onStickerPickerClick() }
        activity_post_text.onTextChanged { presenter.onTextInput(it) }
        activity_post_text_style_clickbox.setOnClickListener { presenter.onTextStyleClick() }
    }

    private fun initBackgroundsList() {
        CenterLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false). apply {
            activity_post_backgrounds_list.layoutManager = this
        }
        activity_post_backgrounds_list.adapter = backgroundsAdapter(
                context = this,
                onItemClick = { background ->
                    if(background.type == BackgroundType.IMAGE) {
                        RxPermissions(this)
                                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .subscribe({ granted ->
                                    if(granted) {
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
                if(position % 2 == 1) {
                    outRect.top = 4.dp2px(this@PostActivity)
                }
                if(position > getGalleryAdapter().getItemsCount() - 3) {
                    outRect.right = 0
                } else {
                    outRect.right = 8.dp2px(this@PostActivity)
                }
            }
        })
    }
}
