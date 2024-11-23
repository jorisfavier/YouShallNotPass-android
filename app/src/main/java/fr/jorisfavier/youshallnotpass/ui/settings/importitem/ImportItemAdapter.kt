package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import fr.jorisfavier.youshallnotpass.ui.settings.importitem.review.ReviewImportedItemsFragment

class ImportItemAdapter(
    parentActivity: FragmentActivity,
) : FragmentStateAdapter(parentActivity) {
    override fun getItemCount(): Int = ImportItemStep.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            ImportItemStep.SELECT_FILE.ordinal -> ImportSelectFileFragment()
            ImportItemStep.PASSWORD_NEEDED.ordinal -> ProvideImportPasswordFragment()
            ImportItemStep.REVIEW_ITEM.ordinal -> ReviewImportedItemsFragment()
            ImportItemStep.SUCCESS_FAIL.ordinal -> ImportResultFragment()
            else -> error("Unknown position")
        }
    }

}
