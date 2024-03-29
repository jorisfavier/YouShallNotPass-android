package fr.jorisfavier.youshallnotpass.ui.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import fr.jorisfavier.youshallnotpass.BuildConfig
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.FragmentPrivacyPolicyBinding
import fr.jorisfavier.youshallnotpass.ui.common.WebViewWithExtraClient
import fr.jorisfavier.youshallnotpass.utils.autoCleared
import fr.jorisfavier.youshallnotpass.utils.extensions.forceDarkMode
import fr.jorisfavier.youshallnotpass.utils.extensions.isDarkMode

class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {

    private var binding: FragmentPrivacyPolicyBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setTitle(R.string.privacy_policy)
            show()
        }
        binding.webview.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewWithExtraClient()
            forceDarkMode(isDarkModeOn = requireContext().isDarkMode())
            loadUrl(BuildConfig.PRIVACY_POLICY_URL)
        }
    }

}