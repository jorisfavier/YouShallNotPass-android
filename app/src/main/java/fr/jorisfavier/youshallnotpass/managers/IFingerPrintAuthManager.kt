package fr.jorisfavier.youshallnotpass.managers

import android.content.Context
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

interface IFingerPrintAuthManager {
    fun fingerPrintAuth(context: Context, callback: FingerprintManagerCompat.AuthenticationCallback)
}