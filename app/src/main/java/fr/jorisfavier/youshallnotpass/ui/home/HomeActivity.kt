package fr.jorisfavier.youshallnotpass.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: HomeViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        AndroidInjection.inject(this)
        setupActionBarWithNavController(navController)
        initObserver()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            viewModel.onConfigurationChanged()
        }
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
        viewModel.requireAuthentication.observe(this, Observer {
            Intent(this, AuthActivity::class.java).let {
                it.putExtra(AuthActivity.redirectToHomeExtraKey, false)
                startActivity(it)
            }
        })
    }
}
