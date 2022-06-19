package ru.salvadorvdali.pagination

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

   suspend fun getUsersList(initialId: Int, batchSize: Int): List<User> {
       delay(2000)
       val index = users.indexOfFirst { it.id == initialId }
       val secondIndex = index + batchSize
       return users.subList(initialId, secondIndex)
    }
}