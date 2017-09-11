package ru.maxost.vk_superior_post.UI.UploadScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.evernote.android.state.StateSaver
import kotlinx.android.synthetic.main.activity_upload.*
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.Model.Post
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.PostScreen.PostActivity

class UploadActivity : AppCompatActivity(), UploadPresenter.View {

    private val presenter: UploadPresenter by lazy(LazyThreadSafetyMode.NONE) { App.graph.getUploadPresenter() }

    companion object {
        const val POST_KEY = "POST_KEY"

        fun start(caller: Activity, post: Post) {
            val intent = Intent(caller, UploadActivity::class.java)
            intent.putExtra(POST_KEY, post)
            caller.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        activity_upload_common_button.setOnClickListener { presenter.onCommonButtonClick() }
        presenter.init(intent.getSerializableExtra(POST_KEY) as Post)
        StateSaver.restoreInstanceState(presenter, savedInstanceState)
        presenter.attach(this, savedInstanceState==null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(presenter, outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun updateView(viewModel: UploadPresenter.ViewModel) {
        when(viewModel.state) {
            UploadPresenter.ViewModel.ViewState.LOADING -> {
                activity_upload_common_text.text = getString(R.string.upload_loading)
                activity_upload_common_button.text = getString(R.string.upload_cancel)
            }
            UploadPresenter.ViewModel.ViewState.SUCCESS -> {
                activity_upload_common_text.text = getString(R.string.upload_success)
                activity_upload_common_button.text = getString(R.string.upload_load_more)
            }
            UploadPresenter.ViewModel.ViewState.ERROR -> {
                activity_upload_common_text.text = getString(R.string.upload_error)
                activity_upload_common_button.text = getString(R.string.upload_retry)
            }
        }
    }

    override fun showNewPostScreen() {
        val intent = Intent(this, PostActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun close() = finish()
}
