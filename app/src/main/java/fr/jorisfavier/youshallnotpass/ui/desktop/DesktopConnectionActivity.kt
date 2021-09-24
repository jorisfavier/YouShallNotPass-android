package fr.jorisfavier.youshallnotpass.ui.desktop

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.doOnLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.AndroidInjection
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.databinding.ActivityDesktopConnectionBinding
import fr.jorisfavier.youshallnotpass.model.QRCodeAnalyzer
import fr.jorisfavier.youshallnotpass.utils.CustomLifecycle
import fr.jorisfavier.youshallnotpass.utils.State
import fr.jorisfavier.youshallnotpass.utils.extensions.aspectRatio
import fr.jorisfavier.youshallnotpass.utils.extensions.toast
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class DesktopConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDesktopConnectionBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: DesktopConnectionViewModel by viewModels { viewModelFactory }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraLifecycle: CustomLifecycle

    private val cameraPermission: Int
        get() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

    private val onCameraPermissionGranted =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestPermissionIfNeeded()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        cameraLifecycle = CustomLifecycle(this)
        supportActionBar?.hide()
        binding = ActivityDesktopConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.close.setOnClickListener { onBackPressed() }
        initObserver()
    }

    override fun onStart() {
        super.onStart()
        cameraExecutor = Executors.newSingleThreadExecutor()
        requestPermissionIfNeeded()
    }

    private fun initObserver() {
        viewModel.qrCodeAnalyseState.observe(this) { state ->
            when (state) {
                is State.Loading -> cameraLifecycle.doOnPause()
                is State.Error -> {
                    cameraLifecycle.doOnResume()
                    toast(R.string.unable_to_read_qr_code)
                }
                is State.Success<*> -> {
                    toast(R.string.qr_code_success)
                    finish()
                }
            }
        }
    }

    private fun requestPermissionIfNeeded() {
        if (cameraPermission == PermissionChecker.PERMISSION_DENIED) {
            showEducationalDialog()
        } else {
            binding.preview.doOnLayout { initCamera() }
        }
    }

    private fun initCamera() {
        binding.preview.implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val analyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(binding.preview.aspectRatio())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(cameraExecutor, QRCodeAnalyzer(viewModel::onCodeFound))

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(cameraLifecycle, cameraSelector, preview, analyzer)
                preview.setSurfaceProvider(binding.preview.surfaceProvider)

            } catch (exc: Exception) {
                Timber.e(exc, "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun showEducationalDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.camera_permission_title))
            .setMessage(getString(R.string.camera_permission_explanation))
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onCameraPermissionGranted.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                showEducationalDialog()
            }
            .show()
    }
}