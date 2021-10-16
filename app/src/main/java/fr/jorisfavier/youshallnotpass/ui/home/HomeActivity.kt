package fr.jorisfavier.youshallnotpass.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.utils.extensions.findNavControllerFromFragmentContainerView

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    private val navController by lazy {
        findNavControllerFromFragmentContainerView(R.id.nav_host_fragment)
    }

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBarWithNavController(navController)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        initObserver()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onBackPressed() {
        if (navController.previousBackStackEntry == null) {
            ActivityCompat.finishAffinity(this)
        } else {
            super.onBackPressed()
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
            Intent(this, AuthActivity::class.java).let {
                it.putExtra(AuthActivity.REDIRECT_TO_HOME_EXTRA, false)
                startActivity(it)
            }
        }
    }
}
