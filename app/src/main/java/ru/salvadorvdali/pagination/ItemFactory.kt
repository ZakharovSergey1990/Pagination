package ru.salvadorvdali.pagination

import android.util.Log
import kotlinx.coroutines.flow.Flow

interface ItemFactory <T> {
   suspend fun loadItems(initialId: Int, batchSize: Int, isMoreDirection: Boolean): List<T>
}

class ItemFactoryImpl(private val datasource: Datasource): ItemFactory<User> {
    override suspend fun loadItems(initialId: Int, batchSize: Int, isMoreDirection: Boolean): List<User> {
        Log.d("Pagination", "loadItems: initialId = ${initialId}")
        return datasource.getUsersList(initialId, batchSize, isMoreDirection)
    }
}

interface Identifiable{
    fun getID(): Int
}

