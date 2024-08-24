package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.autofill.FillRequest
import android.view.autofill.AutofillManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ActivityAutofillBinding
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import fr.jorisfavier.youshallnotpass.ui.item.ItemFragmentArgs
import fr.jorisfavier.youshallnotpass.utils.extensions.findNavControllerFromFragmentContainerView
import fr.jorisfavier.youshallnotpass.utils.observeEvent

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class AutofillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutofillBinding

    private val viewModel: AutofillSearchViewModel by viewModels()

    private val navController by lazy {
        findNavControllerFromFragmentContainerView(R.id.nav_host_fragment)
    }

    private val assistStructure by lazy {
        intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)!!
    }

    private val fillRequest by lazy {
        intent.getParcelableExtra<FillRequest>(FILL_REQUEST_KEY)!!
    }

    private val redirectToItemCreation by lazy {
        intent.getBooleanExtra(REDIRECT_TO_ITEM_KEY, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutofillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBarWithNavController(navController)
        if (savedInstanceState == null) {
            viewModel.setAutofillInfos(assistStructure, fillRequest)
            requireAuthentication()
        }
        initObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    private fun initObservers() {
        viewModel.appName.observeEvent(this) { name ->
            if (redirectToItemCreation) {
                navController.navigate(
                    R.id.ItemFragment,
                    ItemFragmentArgs(itemName = name).toBundle()
                )
            }
        }
    }

    private fun requireAuthentication() {
        val intent = Intent(this, AuthActivity::class.java).apply {
            putExtra(AuthActivity.REDIRECT_TO_HOME_EXTRA, false)
        }
        startActivity(intent)
    }

    companion object {
        const val REDIRECT_TO_ITEM_KEY = "redirect_to_item_key"
        const val FILL_REQUEST_KEY = "fill_request_key"
    }

}
