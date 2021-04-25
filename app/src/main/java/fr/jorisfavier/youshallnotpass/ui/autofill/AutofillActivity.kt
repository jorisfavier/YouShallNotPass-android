package fr.jorisfavier.youshallnotpass.ui.autofill

import android.app.Activity
import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.autofill.AutofillManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.databinding.ActivityAutofillBinding
import fr.jorisfavier.youshallnotpass.ui.auth.AuthActivity
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AutofillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutofillBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: AutofillViewModel by viewModels { viewModelFactory }

    private val assistStructure by lazy {
        intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)
    }

    private val searchAdapter by lazy {
        AutofillAdapter(viewModel::onItemClicked)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        binding = ActivityAutofillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        supportActionBar?.hide()
        if (savedInstanceState == null) {
            viewModel.setAssistStructure(assistStructure)
            requireAuthentication()
        }
        initRecyclerView()
        initObservers()
    }

    private fun requireAuthentication() {
        Intent(this, AuthActivity::class.java).let {
            it.putExtra(AuthActivity.REDIRECT_TO_HOME_EXTRA, false)
            startActivity(it)
        }
    }

    private fun initRecyclerView() {
        with(binding.searchRecyclerview) {
            adapter = searchAdapter
        }
    }

    private fun initObservers() {
        viewModel.results.observe(this, searchAdapter::submitList)

        viewModel.autofillResponse.observe(this) {
            it.getContentIfNotHandled()?.let { intent ->
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}