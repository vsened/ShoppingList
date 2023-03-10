package com.example.shopinglist.presentation

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopinglist.R

class ShopListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val tvName: TextView = view.findViewById(R.id.textViewItemName)
    val tvCount: TextView = view.findViewById(R.id.textViewItemCount)
}