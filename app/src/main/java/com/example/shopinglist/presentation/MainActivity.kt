package com.example.shopinglist.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.shopinglist.R
import com.example.shopinglist.domain.ShopItem

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var llShopList: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.shopListLiveData.observe(this) {
            showList(it)
        }
    }

    private fun initViews() {
        llShopList = findViewById(R.id.linear_layout_shop_list)
    }

    private fun showList(list: List<ShopItem>) {
        llShopList.removeAllViews()
        for(shopItem in list) {
            val layoutId = if (shopItem.enabled) R.layout.shop_item_enabled
            else R.layout.shop_item_disabled
            val view = LayoutInflater.from(this).inflate(
                layoutId,
                llShopList,
                false
            )
            val textViewName = view.findViewById<TextView>(R.id.textViewItemName)
            val textViewCount = view.findViewById<TextView>(R.id.textViewItemCount)
            textViewName.text = shopItem.name
            textViewCount.text = shopItem.count.toString()
            view.setOnLongClickListener{
                viewModel.changeEnableState(shopItem)
                true
            }
            llShopList.addView(view)
        }
    }
}