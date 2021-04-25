package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ActivityAutofillBinding
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.utils.extensions.findNavControllerFromFragmentContainerView
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutofillBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AutofillSearchViewModel by viewModels { viewModelFactory }

    private val navController by lazy {
        findNavControllerFromFragmentContainerView(R.id.nav_host_fragment)
    }

    private val assistStructure by lazy {
        intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        binding = ActivityAutofillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBarWithNavController(navController)
        if (savedInstanceState == null) {
            viewModel.setAssistStructure(assistStructure)
            requireAuthentication()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun requireAuthentication() {
        Intent(this, AuthActivity::class.java).let {
            it.putExtra(AuthActivity.REDIRECT_TO_HOME_EXTRA, false)
            startActivity(it)
        }
    }
}