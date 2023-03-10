package com.example.shopinglist.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.shopinglist.R
import com.example.shopinglist.domain.ShopItem

class ShopListAdapter : ListAdapter<ShopItem, ShopListViewHolder>(ShopItemDiffCallback()) {

    var onShopItemLongClickListener: ((ShopItem) -> Unit)? = null
    var onShopItemClickListener: ((ShopItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopListViewHolder {
        val layerId = when (viewType) {
            VIEW_TYPE_ENABLED -> R.layout.shop_item_enabled
            VIEW_TYPE_DISABLED -> R.layout.shop_item_disabled
            else -> throw RuntimeException("Unknown view type: $viewType")
        }
        val view = LayoutInflater.from(parent.context).inflate(layerId, parent, false)
        return ShopListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopListViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvName.text = item.name
        holder.tvCount.text = item.count.toString()
        holder.view.setOnLongClickListener {
            if (onShopItemLongClickListener != null) {
                onShopItemLongClickListener?.invoke(item)
            }
            true
        }
        holder.view.setOnClickListener{
            if (onShopItemClickListener != null) {
                onShopItemClickListener?.invoke(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.enabled) {
            VIEW_TYPE_ENABLED
        } else {
            VIEW_TYPE_DISABLED
        }
    }

    companion object {
        const val VIEW_TYPE_ENABLED = 1
        const val VIEW_TYPE_DISABLED = 0

        const val MAX_POOL_SIZE = 10
    }
}