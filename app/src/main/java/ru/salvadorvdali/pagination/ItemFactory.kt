package ru.salvadorvdali.pagination

interface ItemFactory <T> {
   suspend fun loadLessDirectionItems(itemId: String, batchSize: Int): List<T>
   suspend fun loadMoreDirectionItems(itemId: String, batchSize: Int): List<T>
}

class ItemFactoryImpl(private val datasource: Datasource): ItemFactory<User> {
//    override suspend fun loadItems(initialId: Int, batchSize: Int, direction: Direction): List<User> {
//        Log.d("Pagination", "loadItems: initialId = ${initialId}")
//        when(direction){
//
//        }
//        return datasource.getUsersList(initialId, batchSize, direction)
//    }

    override suspend fun loadLessDirectionItems(itemId: String, batchSize: Int): List<User> {
       return datasource.getUsersList(itemId, batchSize, false)
    }

    override suspend fun loadMoreDirectionItems(itemId: String, batchSize: Int): List<User> {
       return datasource.getUsersList(itemId, batchSize, true)
    }
}

interface Identifiable{
    fun getID(): String
}

enum class Direction(){
    MORE, LESS, NONE
}