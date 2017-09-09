package ru.maxost.vk_superior_post.UI.PostScreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import com.bumptech.glide.Glide
import com.evernote.android.state.StateSaver
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.Model.Background
import ru.maxost.vk_superior_post.Model.BackgroundType
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.*
import java.io.File
import java.net.URI


class PostActivity : AppCompatActivity(), PostPresenter.View, StickerListDialogFragment.Listener {

    companion object {
        const val REQUEST_CODE_FILE_CHOOSE = 1231
    }

    private val presenter by lazy { App.graph.getPostPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        initListeners()
        initBackgroundsList()
        initGalleryList()
        initPresenter(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_FILE_CHOOSE) and (resultCode == Activity.RESULT_OK)) {
            SwitchLog.scream(data!!.data.toString())
            Glide.with(this)
                    .load(data.data)
                    .fitCenter()
                    .into(activity_post_compose_background_center)
        }
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
        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        } else {
            intent = Intent(Intent.ACTION_GET_CONTENT)
        }
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSE)
    }

    override fun takePhoto() {

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
        (activity_post_gallery_list.adapter as GalleryAdapter).setNewData(list)
    }

    override fun showGalleryPanel(show: Boolean) {
        activity_post_gallery_list.show(show)
    }

    override fun showKeyboard(show: Boolean) {
        if(show) {
            //TODO ?
        } else {
            hideKeyboard()
        }
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
        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false). apply {
            activity_post_backgrounds_list.layoutManager = this
        }
        activity_post_backgrounds_list.adapter = backgroundsAdapter(
                context = this,
                onItemClick = { background ->
                    if(background.type == BackgroundType.IMAGE && !RxPermissions(this).isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        RxPermissions(this)
                                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                                .subscribe({ granted ->
                                    if(granted) {
                                        presenter.onBackgroundSelected(background)
                                    } else {
                                        //TODO
                                    }
                                })
                    } else {
                        presenter.onBackgroundSelected(background)
                    }
                }
        )
    }

    private fun initGalleryList() {
        GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false).apply {
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
                outRect.right = 8.dp2px(this@PostActivity)
                if(position % 2 == 1) {
                    outRect.top = 4.dp2px(this@PostActivity)
                }
            }
        })
    }
}
