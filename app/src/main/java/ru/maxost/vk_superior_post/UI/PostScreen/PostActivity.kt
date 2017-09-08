package ru.maxost.vk_superior_post.UI.PostScreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import com.bumptech.glide.Glide
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.Model.TextStyle
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.onTextChanged
import ru.maxost.vk_superior_post.Utils.setTextStyle
import ru.maxost.vk_superior_post.Utils.toAndroidUri
import ru.maxost.vk_superior_post.Utils.toEditable
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
        presenter.attach(this, savedInstanceState==null)

        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) presenter.onFilePermissionGranted()
                }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_FILE_CHOOSE) and (resultCode == Activity.RESULT_OK)) {
            SwitchLog.scream(data!!.data.toString())
            Glide.with(this)
                    .load(data!!.data)
                    .fitCenter()
                    .into(activity_post_background)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun showStickerPickerDialog() {
        StickerListDialogFragment.newInstance(24)
                .show(supportFragmentManager, null)
    }

    override fun showUploadScreen(post: Post) = UploadActivity.start(this, post)

    override fun onStickerClicked(stickerId: Int) = presenter.onStickerClick(stickerId)

    override fun setTextStyle(textStyle: TextStyle) {
        activity_post_text.setTextStyle(textStyle)
    }

    override fun setText(text: String) {
        activity_post_text.text = text.toEditable()
    }

    override fun setBackground(file: File) {
        Glide.with(this)
                .load(file)
                .into(activity_post_background)
    }

    private fun initListeners() {
        activity_post_submit_button.setOnClickListener { presenter.onSubmitClick() }
        activity_post_stickers_clickbox.setOnClickListener { presenter.onStickerPickerClick() }
        activity_post_text.onTextChanged { presenter.onTextInput(it) }
        activity_post_text_style_clickbox.setOnClickListener { presenter.onTextStyleClick() }
    }
}
