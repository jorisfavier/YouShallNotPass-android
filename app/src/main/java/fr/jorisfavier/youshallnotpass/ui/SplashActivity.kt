package fr.jorisfavier.youshallnotpass.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        supportActionBar?.hide()
        lifecycleScope.launchWhenStarted {
            delay(300)
            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
        }
    }

}