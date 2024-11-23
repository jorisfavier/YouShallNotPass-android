package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ActivityImportItemBinding
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import fr.jorisfavier.youshallnotpass.utils.observeEvent
import kotlin.math.roundToInt

@AndroidEntryPoint
class ImportItemActivity : AppCompatActivity(R.layout.activity_import_item) {

    private lateinit var binding: ActivityImportItemBinding

    private val adapter: ImportItemAdapter by lazy {
        ImportItemAdapter(this)
    }

    val viewModel: ImportItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initObserver()
        initUI()
        supportActionBar?.hide()
    }

    private fun initUI() {
        with(binding) {
            pager.adapter = adapter
            pager.isUserInputEnabled = false
            // Prevents a bug with edittext and ViewPager2:
            // https://github.com/android/views-widgets-samples/issues/107#issuecomment-726809766
            pager.offscreenPageLimit = 1
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    indicator.progress =
                        (((position + 1).toFloat() / adapter.itemCount.toFloat()) * 100f).roundToInt()
                    viewModel.onSlideChanged(position)
                }
            })
            skipButton.setOnClickListener { finish() }
            nextButton.setOnClickListener {
                viewModel.goToNextStep()
            }
            doneButton.setOnClickListener { finish() }
        }
    }

    private fun initObserver() {
        viewModel.navigate.observe(this) { event ->
            val itemIndex = event.getContentIfNotHandled() ?: return@observe
            binding.pager.currentItem = itemIndex
        }
        viewModel.importItemsState.observe(this) { importState ->
            binding.actionButtons.isVisible = false
            binding.doneButton.isVisible =
                importState == State.Error || importState is State.Success
        }
        viewModel.error.observeEvent(this) { error ->
            toast(error.messageResId)
        }
    }
}
