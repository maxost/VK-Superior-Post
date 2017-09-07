package ru.maxost.vk_superior_post.UI.PostScreen

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vk.sdk.api.photo.VKUploadImage
import kotlinx.android.synthetic.main.activity_post.*
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.UploadScreen.UploadActivity
import ru.maxost.vk_superior_post.Utils.onTextChanged


class PostActivity : AppCompatActivity(), PostPresenter.View {

    private val presenter by lazy { App.graph.getPostPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        activity_post_submit_button.setOnClickListener { presenter.onSubmitClick() }
        activity_post_text.onTextChanged { presenter.onTextInput(it) }
        presenter.attach(this, savedInstanceState==null)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun showUploadScreen(post: Post) = UploadActivity.start(this, post)
}
