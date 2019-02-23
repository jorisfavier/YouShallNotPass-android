package fr.jorisfavier.youshallnotpass.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat


class BiometricUtils {
    companion object {
        fun isSdkVersionSupported() : Boolean {
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        }

        fun isHardwareSupported(context: Context): Boolean {
            val fingerPrintManager = FingerprintManagerCompat.from(context)
            return fingerPrintManager.isHardwareDetected
        }

        fun isFingerprintAvailable(context: Context): Boolean {
            val fingerPrintManager = FingerprintManagerCompat.from(context)
            return fingerPrintManager.hasEnrolledFingerprints()
        }

        fun isPermissionGranted(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }


}