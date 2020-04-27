package fr.jorisfavier.youshallnotpass.features.item

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.jorisfavier.youshallnotpass.R
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        setSupportActionBar(itemview_toolbar)
        supportActionBar?.apply {
            title = getString(R.string.item_create_title)
            setDisplayHomeAsUpEnabled(true)
        }
        loadEditFragment()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadEditFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        var editFragment = supportFragmentManager.findFragmentByTag(ItemEditFragment.editItemFragmentTag)
        if (editFragment == null) {
            editFragment = ItemEditFragment.newInstance()
        }
        fragmentTransaction.add(R.id.itemview_content,editFragment,ItemEditFragment.editItemFragmentTag)
        fragmentTransaction.commit()
    }
}
