package ru.salvadorvdali.pagination

import kotlinx.coroutines.flow.Flow

interface ItemFactory <T> {
   suspend fun loadItems(initialId: Int, batchSize: Int): List<T>
}

class ItemFactoryImpl(private val datasource: Datasource): ItemFactory<User> {
    override suspend fun loadItems(initialId: Int, batchSize: Int): List<User> {
        return datasource.getUsersList(initialId, batchSize)
    }
}

interface Identifiable{
    fun getID(): Int
}

