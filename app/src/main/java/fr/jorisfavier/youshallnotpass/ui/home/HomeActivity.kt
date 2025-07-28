package fr.jorisfavier.youshallnotpass.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ActivityHomeBinding
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.utils.extensions.findNavControllerFromFragmentContainerView

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val navController by lazy {
        findNavControllerFromFragmentContainerView(R.id.nav_host_fragment)
    }

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        initObserver()
        viewModel.trackScreenView()
        onBackPressedDispatcher.addCallback(owner = this) {
            if (navController.previousBackStackEntry == null) {
                ActivityCompat.finishAffinity(this@HomeActivity)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.onConfigurationChanged()
    }

    override fun onPause() {
        viewModel.onAppPaused()
        super.onPause()
    }

    override fun onResume() {
        viewModel.onAppResumed()
        super.onResume()
    }

    private fun initObserver() {
        viewModel.requireAuthentication.observe(this) {
            val intent = Intent(this, AuthActivity::class.java).apply {
                putExtra(AuthActivity.REDIRECT_TO_HOME_EXTRA, false)
            }
            startActivity(intent)
        }
    }
}
