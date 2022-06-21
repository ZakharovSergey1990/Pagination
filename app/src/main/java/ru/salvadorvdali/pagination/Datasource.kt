package ru.salvadorvdali.pagination

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class Datasource {
    private val users = mutableListOf<User>()

    init{
        for (i in 0..1000){
            users.add(User("user$i", i))
        }
    }

    fun getUsers(initialId: Int, butchSize: Int, isMore: Boolean): List<User>{
        val index = users.indexOfFirst { it.id == initialId }
        val secondIndex = if(isMore) index + butchSize else index - butchSize
        return users.subList(initialId, secondIndex)
    }

    fun getAll(): List<User>{
    return users
    }

   suspend fun getUsersList(initialId: Int, batchSize: Int, isMoreDirection: Boolean): List<User> {
       return withContext(Dispatchers.IO) {
           Log.d("Pagination", "getUsersList: initialId = ${initialId}")
           Log.d("Pagination", "getUsersList: delay ")
           if(isMoreDirection){
               val index = users.indexOfFirst { it.id == initialId }
               val secondIndex = index + batchSize
               val result = users.subList(initialId, secondIndex)
               Log.d("Pagination", "getUsersList: result = ${result.map { it.name }}")
               // delay(2000)
               result
           }
           else{
               val index = users.indexOfFirst { it.id == initialId }
               val startIndex = if((index - batchSize)>0) index - batchSize else 0
               val result = users.subList(startIndex, initialId)
               Log.d("Pagination", "getUsersList: result = ${result.map { it.name }}")
               // delay(2000)
               result
           }

       }
    }
}