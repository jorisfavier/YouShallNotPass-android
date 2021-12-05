package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroPageTransformerType
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.ReviewImportedItemsFragment
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.getThemeColor

@AndroidEntryPoint
class ImportItemActivity : AppIntro() {

    val viewModel: ImportItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setNextArrowColor(getThemeColor(R.attr.colorOnBackground))
        setColorSkipButton(getThemeColor(R.attr.colorOnBackground))
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
        val importState = viewModel.importItemsState.value
        if (importState == State.Error || importState is State.Success) {
            finish()
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        finish()
    }
}