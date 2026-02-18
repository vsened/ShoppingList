package com.example.shopinglist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shopinglist.databinding.ActivityMainBinding
import com.example.shopinglist.feature.shopitem.presentation.ShopItemActivity
import com.example.shopinglist.feature.shoplist.presentation.MainFragment

class MainActivity : AppCompatActivity(), MainFragment.OnShopListInteractionListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commit()
        }
    }

    override fun onAddItemClicked() {
        if (isOnePaneMode()) {
            val intent = ShopItemActivity.newIntentAddItem(this)
            startActivity(intent)
        } else {
            launchFragment(MainFragment())
        }
    }

    override fun onEditItemClicked(itemId: Int) {
        if (isOnePaneMode()) {
            val intent = ShopItemActivity.newIntentEditItem(this, itemId)
            startActivity(intent)
        } else {
            launchFragment(MainFragment())
        }
    }

    override fun onEditingFinished() {
        supportFragmentManager.popBackStack()
    }

    private fun isOnePaneMode(): Boolean {
        return binding.fragmentContainerItem == null
    }

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack()
        supportFragmentManager.beginTransaction()
            .replace(R.id.shop_item_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
