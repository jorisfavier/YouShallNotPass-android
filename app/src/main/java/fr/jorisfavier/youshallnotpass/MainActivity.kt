package fr.jorisfavier.youshallnotpass

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import fr.jorisfavier.youshallnotpass.features.home.HomeViewModel
import fr.jorisfavier.youshallnotpass.features.search.SearchActivity
import fr.jorisfavier.youshallnotpass.managers.IFingerPrintAuthManager
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var fingerPrintManager: IFingerPrintAuthManager
    private lateinit var viewmodel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerAppComponent.create().inject(this)
        viewmodel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        viewmodel.authManager = fingerPrintManager
        setContentView(R.layout.activity_main)
        viewmodel.requestAuth(baseContext)
        initObserver()
    }

    fun initObserver(){
        viewmodel.authSuccess.observe(this, Observer<Boolean> { success ->
            if(success) {
                redirectToSearchPage()
            }
            else {
                displayErrorModal()
            }
        })
    }

    fun redirectToSearchPage(){
        val searchPahgeIntent = Intent(this,SearchActivity::class.java)
        startActivity(searchPahgeIntent)
    }

    fun displayErrorModal(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Authentication failed")
        builder.setMessage("Authentication failed please try again.")
        builder.setPositiveButton(android.R.string.yes,null)
        builder.show()
    }



}
