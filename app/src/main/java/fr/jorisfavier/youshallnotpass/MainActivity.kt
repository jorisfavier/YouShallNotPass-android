package fr.jorisfavier.youshallnotpass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.jorisfavier.youshallnotpass.managers.FingerPrintAuthManager

class MainActivity : AppCompatActivity() {

    var fingerPrintManager: FingerPrintAuthManager = FingerPrintAuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fingerPrintManager.fingerPrintAuth(baseContext)
    }



}
