package ru.salvadorvdali.pagination

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

//class PaginationRepository( val datasource: Datasource) {
//
//    val butchSize = 50
//
//    val paging = MutableStateFlow<List<Paging>>(listOf())
//
//    var firstPage = listOf<User>()
//    var secondPage = listOf<User>()
//    var thirdPage = listOf<User>()
//    var firstDelimeter = Delimeter(1)
//    var secondDelimeter = Delimeter(2)
//
//
//    fun getUsers(initialId: Int, isMore: Boolean ): Flow <List<Paging>> {
//        firstPage = if(initialId>0) {
//            datasource.getUsers(initialId, butchSize, !isMore)
//        } else emptyList()
//        Log.d("MainActivity", "firstPage = $firstPage")
//        secondPage = datasource.getUsers(initialId, butchSize, isMore)
//        thirdPage = datasource.getUsers(initialId+butchSize, butchSize, isMore)
//        Log.d("MainActivity", "thirdPage = ${thirdPage.size}")
//
//        val result = mutableListOf<Paging>()
//        result.addAll(firstPage)
//        result.add(firstDelimeter)
//        result.addAll(secondPage)
//        result.add(secondDelimeter)
//        result.addAll(thirdPage)
//        paging.value = result
//        return paging
//    }
//
//    fun updateUsers(){
//        firstPage = secondPage
//        secondPage = thirdPage
//        thirdPage = datasource.getUsers(secondPage.last().id, butchSize, isMore = true)
//        val result = mutableListOf<Paging>()
//        result.addAll(firstPage)
//        result.add(firstDelimeter)
//        result.addAll(secondPage)
//        result.add(secondDelimeter)
//        result.addAll(thirdPage)
//        paging.value = result
//        Log.d("MainActivity", "updateUsers result = $result")
//    }
//
//
//
//}