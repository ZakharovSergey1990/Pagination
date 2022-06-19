package ru.salvadorvdali.pagination

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import ru.salvadorvdali.pagination.ui.theme.PaginationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val datasource = Datasource()
       // val repository = PaginationRepository(datasource)
        setContent {
            PaginationTheme {
                // A surface container using the 'background' color from the theme

                //val users by repository.getUsers(0, true).collectAsState(initial = emptyList())
                val users = datasource.getAll()
val factory: ItemFactory<User> = ItemFactoryImpl(datasource)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Pagination <User> (itemFactory = factory, batchSize = 30) { user ->
                                                       Text(
                                   user.name,
                                   modifier = Modifier.padding(10.dp),
                                   fontSize = MaterialTheme.typography.h5.fontSize
                               )
                    }
//                    val state = rememberLazyListState()
//                    LaunchedEffect(key1 = users, block = {state.scrollToItem(30)})
//                   LazyColumn(state = state){
//                       itemsIndexed(users) { index, user ->
//                           LaunchedEffect(key1 = user){
//                               delay(100)
//                              val info = state.layoutInfo.visibleItemsInfo.map { it.index }
//                               if(info.contains(index)){
//                               Log.d("Pagination", "index = $index")}
//                           }
//                           Log.d("Pagination", user.toString())
//                               Text(
//                                   user.name,
//                                   modifier = Modifier.padding(10.dp),
//                                   fontSize = MaterialTheme.typography.h5.fontSize
//                               )
//                           }
//                       }
                   }
                }
            }
        }
    }


@Composable
fun <T: Identifiable> Pagination(itemFactory: ItemFactory<T>, batchSize: Int,  contentItem: @Composable (T) -> Unit ){

    var list = remember{  mutableStateListOf<T>() }

    var initialId by remember{ mutableStateOf(0)}
    var isLoading by remember{ mutableStateOf(false)}



//    val items by itemFactory.loadItems(initialId = initialId, batchSize = batchSize).onEach {
//        list.addAll(it)
//        isLoading = false
//    }.collectAsState(initial = emptyList())

    LaunchedEffect(key1 = itemFactory){
        val items = itemFactory.loadItems(0, batchSize = batchSize)
        list.addAll(items)
    }

    LazyColumn(){
        itemsIndexed(list){ index, item ->
            if(index == list.size-1){
                Log.d("Pagination", "list = ${list.size}")
                LaunchedEffect(key1 = item){
                    isLoading = true
                    val items = itemFactory.loadItems(item.getID(), batchSize = batchSize)
                    list.addAll(items)
                    Log.d("Pagination", "list = ${list.size}")
                    isLoading = false
                }
            }
            contentItem(item)
        }
        if(isLoading){
         item {
             Row(modifier = Modifier.fillMaxWidth()) {
                 CircularProgressIndicator()
             }
         }
        }
    }

    @Composable
    fun getItems(){
        ////
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PaginationTheme {
        Greeting("Android")
    }
}

interface Paging

data class User(
    val name: String,
    val id: Int
) : Identifiable {
    override fun getID() = id
}


class Delimeter(val index: Int): Paging
