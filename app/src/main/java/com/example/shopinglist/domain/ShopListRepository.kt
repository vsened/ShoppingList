package com.example.shopinglist.domain

interface ShopListRepository {

    fun addShopItem(item: ShopItem)

    fun deleteShopItem(item: ShopItem)

    fun editShopItem(id: Int)

    fun getShopItem(id: Int): ShopItem

    fun getShopList(): List<ShopItem>

}