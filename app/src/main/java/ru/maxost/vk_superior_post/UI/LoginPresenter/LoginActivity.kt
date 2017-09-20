package ru.maxost.vk_superior_post.UI.LoginPresenter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_login.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.App
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.PostScreen.PostActivity

class LoginActivity : AppCompatActivity(), LoginPresenter.View {

    private val presenter: LoginPresenter by lazy(LazyThreadSafetyMode.NONE) { App.graph.getLoginPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        activity_login_button.setOnClickListener { presenter.onVkLoginClick() }
        presenter.attach(this, savedInstanceState==null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken?) {
                presenter.onVkLoginSuccess()
            }

            override fun onError(error: VKError?) {
                SwitchLog.log(error.toString())
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
    }

    override fun showPostScreen() {
        val intent = Intent(this@LoginActivity, PostActivity::class.java)
        startActivity(intent)
    }

    override fun executeSdkLogin() {
        VKSdk.login(this, "wall", "photos")
    }

    override fun close() = finish()
}