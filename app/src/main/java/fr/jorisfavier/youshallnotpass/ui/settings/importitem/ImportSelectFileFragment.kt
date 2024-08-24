package fr.jorisfavier.youshallnotpass.ui.settings.importitem

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.appintro.SlidePolicy
import dagger.hilt.android.AndroidEntryPoint
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentImportSelectFileBinding
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.toast

@AndroidEntryPoint
class ImportSelectFileFragment : Fragment(R.layout.fragment_import_select_file), SlidePolicy {

    private val viewModel: ImportItemViewModel by activityViewModels()
    private var binding: FragmentImportSelectFileBinding by autoCleared()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val uriValue = uri ?: return@registerForActivityResult
            viewModel.setUri(uriValue)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImportSelectFileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.importSelectFileButton.setOnClickListener { getContent.launch("*/*") }
    }

    override val isPolicyRespected: Boolean
        get() = viewModel.isFileSelected

    override fun onUserIllegallyRequestedNextPage() {
        context?.toast(R.string.please_select_file)
    }

    companion object {
        fun newInstance(): ImportSelectFileFragment {
            return ImportSelectFileFragment()
        }
    }
}
