package com.example.shopinglist.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.shopinglist.domain.ShopItem
import com.example.shopinglist.domain.ShopListRepository
import kotlin.random.Random

object ShopListRepositoryImpl : ShopListRepository {
    private val shopListLiveData = MutableLiveData<List<ShopItem>>()
    private val shopList = sortedSetOf<ShopItem>({o1, o2 -> o1.id.compareTo(o2.id) })

    private var autoIncrementId = 0
    init {
        for (i in 0..100) {
            val item = ShopItem("item: $i", i, Random.nextBoolean(), i)
            shopList.add(item)
        }
        updateLiveData()
    }
    override fun addShopItem(item: ShopItem) {
        if (item.id == ShopItem.UNDEFINED_ID) {
            item.id = autoIncrementId++
        }
        shopList.add(item)
        updateLiveData()
    }

    override fun deleteShopItem(item: ShopItem) {
        shopList.remove(item)
        updateLiveData()
    }

    override fun editShopItem(item: ShopItem) {
        val oldElement = getShopItem(item.id)
        deleteShopItem(oldElement)
        addShopItem(item)
    }

    override fun getShopItem(id: Int): ShopItem {
        return shopList.find { it.id == id }
            ?: throw RuntimeException("Element with id $id not found")
    }

    override fun getShopList(): LiveData<List<ShopItem>> {
        return shopListLiveData
    }

    private fun updateLiveData() {
        shopListLiveData.value = shopList.toList()
    }
}