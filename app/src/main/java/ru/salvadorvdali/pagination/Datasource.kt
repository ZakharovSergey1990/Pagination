package ru.salvadorvdali.pagination

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*

class Datasource {
    private val users = mutableListOf<User>()

    init{
        for (i in 0..1000){
            users.add(User("user$i", UUID.randomUUID().toString()))
        }
    }

//    fun getUsers(initialId: Int, butchSize: Int, isMore: Boolean): List<User>{
//        val index = users.indexOfFirst { it.uuid == initialId }
//        val secondIndex = if(isMore) index + butchSize else index - butchSize
//        return users.subList(initialId, secondIndex)
//    }

    fun getAll(): List<User>{
    return users
    }

   suspend fun getUsersList(itemId: String, batchSize: Int, isMoreDirection: Boolean): List<User> {
       return withContext(Dispatchers.IO) {
           Log.d("Pagination", "getUsersList: initialId = ${itemId}")
           Log.d("Pagination", "getUsersList: delay ")
           delay(3000)
           if(isMoreDirection){
               val index = users.indexOfFirst { it.uuid == itemId }
               var secondIndex = index + batchSize
               if(secondIndex > users.size ){
                   secondIndex = users.size
               }
               val result = users.subList(index, secondIndex)
               Log.d("Pagination", "getUsersList: result = ${result.map { it.name }}")
               result
           }
           else{
               val index = users.indexOfFirst { it.uuid == itemId }
               val startIndex = if((index - batchSize)>0) index - batchSize else 0
               val result = users.subList(startIndex, index)
               Log.d("Pagination", "getUsersList: result = ${result.map { it.name }}")
               result
           }
       }
    }
}