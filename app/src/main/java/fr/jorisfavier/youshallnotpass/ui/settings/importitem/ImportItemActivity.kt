package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroPageTransformerType
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.ReviewImportedItemsFragment
import javax.inject.Inject

class ImportItemActivity : AppIntro() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel: ImportItemViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        addSlide(ImportSelectFileFragment.newInstance())
        addSlide(ProvideImportPasswordFragment.newInstance())
        addSlide(ReviewImportedItemsFragment.newInstance())
        addSlide(ImportResultFragment.newInstance())
        isIndicatorEnabled = true
        isSkipButtonEnabled = true
        isSystemBackButtonLocked = true
        setProgressIndicator()
        setScrollDurationFactor(4)
        setTransformer(AppIntroPageTransformerType.Depth)
        initObserver()
        supportActionBar?.hide()
    }

    private fun initObserver() {
        viewModel.navigate.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                goToNextSlide()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        setSwipeLock(true)
        viewModel.onSlideChanged(position)
    }

    override fun onIntroFinished() {
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        finish()
    }
}