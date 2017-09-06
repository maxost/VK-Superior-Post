package ru.maxost.vk_superior_post.UI

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_login.*
import ru.maxost.switchlog.SwitchLog
import ru.maxost.vk_superior_post.R
import ru.maxost.vk_superior_post.UI.PostScreen.PostActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activity_login_button.setOnClickListener { VKSdk.login(this, "wall", "photos") }

        if(VKAccessToken.currentToken()!=null) { startPostActivity() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
            override fun onResult(res: VKAccessToken?) {
                startPostActivity()
            }

            override fun onError(error: VKError?) {
                SwitchLog.log(error.toString())
            }
        })
    }

    private fun startPostActivity() {
        val intent = Intent(this@LoginActivity, PostActivity::class.java)
        startActivity(intent)
        finish()
    }
}