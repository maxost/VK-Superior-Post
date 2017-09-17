package ru.maxost.vk_superior_post.UI.UploadScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import com.evernote.android.state.StateSaver
import kotlinx.android.synthetic.main.activity_upload.*
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.PostScreen.PostActivity
import ru.maxost.vk_superior_post.Utils.dp2px
import ru.maxost.vk_superior_post.Utils.show

class UploadActivity : AppCompatActivity(), UploadPresenter.View {

    private val presenter: UploadPresenter by lazy(LazyThreadSafetyMode.NONE) { App.graph.getUploadPresenter() }

    companion object {
        fun start(caller: Activity) {
            val intent = Intent(caller, UploadActivity::class.java)
            caller.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        activity_upload_cancel_retry_button.setOnClickListener { presenter.onCancelRetryButtonClick() }
        activity_upload_new_post_button.setOnClickListener { presenter.onNewPostButtonClick() }
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
                activity_upload_cancel_retry_button.text = getString(R.string.upload_cancel)
                activity_upload_loader.show(true)

                ViewCompat.animate(activity_upload_cancel_retry_button)
                        .translationY(-16.dp2px(this).toFloat())
                        .alpha(1f)
                        .setStartDelay(300)
                        .setDuration(200)
                        .start()
            }

            UploadPresenter.ViewModel.ViewState.ERROR -> {
                activity_upload_common_text.text = getString(R.string.error)
                activity_upload_cancel_retry_button.text = getString(R.string.upload_retry)
                activity_upload_loader.show(false)

                ViewCompat.animate(activity_upload_cancel_retry_button)
                        .translationY(-16.dp2px(this).toFloat())
                        .alpha(1f)
                        .setDuration(0)
                        .start()
            }

            UploadPresenter.ViewModel.ViewState.SUCCESS -> {
                activity_upload_common_text.text = getString(R.string.upload_success)

                ViewCompat.animate(activity_upload_loader)
                        .alpha(0f)
                        .setDuration(100)
                        .start()

                ViewCompat.animate(activity_upload_circle)
                        .alpha(1f)
                        .setDuration(100)
                        .start()

                ViewCompat.animate(activity_upload_check_curtain)
                        .translationX(24.dp2px(this).toFloat())
                        .setDuration(500)
                        .start()

                ViewCompat.animate(activity_upload_cancel_retry_button)
                        .alpha(0f)
                        .setStartDelay(300)
                        .setDuration(300)
                        .start()

                activity_upload_new_post_button.show(true)
                ViewCompat.animate(activity_upload_new_post_button)
                        .alpha(1f)
                        .setStartDelay(300)
                        .setDuration(300)
                        .start()
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
