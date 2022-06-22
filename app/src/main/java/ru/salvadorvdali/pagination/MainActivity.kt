package ru.salvadorvdali.pagination

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.salvadorvdali.pagination.ui.theme.PaginationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val datasource = Datasource() // val repository = PaginationRepository(datasource)
        setContent {
            PaginationTheme { // A surface container using the 'background' color from the theme
                val users = datasource.getAll()
                var randomIndex by remember{ mutableStateOf(0)}
                val uuid = MutableStateFlow(users[randomIndex].uuid)
                //val users by repository.getUsers(0, true).collectAsState(initial = emptyList())

                val factory: ItemFactory<User> = ItemFactoryImpl(datasource)
                Scaffold(floatingActionButton = {
                    Button(onClick = {
                        randomIndex = (0..999).random()
                        uuid.value = users[randomIndex].uuid }) {
                        Text(text = randomIndex.toString())
                    }
                }) {
                    Surface(
                        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                    ) {

                        Pagination<User>(
                            scrollToID = uuid,
                            itemFactory = factory,
                            batchSize = 20
                        ) { user ->
                            Text(
                                user.name,
                                modifier = Modifier.padding(10.dp),
                                fontSize = MaterialTheme.typography.h5.fontSize
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun <T : Identifiable> Pagination(
    itemFactory: ItemFactory<T>,
    batchSize: Int,
    scrollToID: Flow<String?> = MutableStateFlow(null),
    contentItem: @Composable (T) -> Unit
) {
    var list = remember { mutableStateListOf<T>() }
    val scope = rememberCoroutineScope()
    var initialId by remember { mutableStateOf(0) }
    var isUpLoading by remember { mutableStateOf(false) }
    var isDownLoading by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }
    var isForwardDirection = true
    var firstVisibleItemIndex = 0
    val state = rememberLazyListState()
    val scrollId by scrollToID.onEach {   Log.d("Pagination", "scrollToID = ${scrollToID} ") }.collectAsState(initial = "")
val context = LocalContext.current
    if(scrollId.isNullOrBlank()){
        // initial list
   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
       CircularProgressIndicator()
        }
    }
    else{
        LaunchedEffect(scrollId) {
            list.clear()
            val items = mutableListOf<T>()
            val firstMoreList = itemFactory.loadMoreDirectionItems(scrollId!!, batchSize = batchSize*2)
            Log.d("Pagination", "firstMoreList = ${firstMoreList.size} ")
            val firstLessList = itemFactory.loadLessDirectionItems(scrollId!!, batchSize = batchSize*2)
            Log.d("Pagination", "firstLessList = ${firstLessList.size} ")
            var difMore = 0
            var difLess = 0
            if( firstMoreList.size < batchSize*2 ){
                 difMore = batchSize*2 - firstMoreList.size
            }
            if( firstLessList.size < batchSize*2 ){
                 difLess = batchSize*2 - firstLessList.size
            }

            when{
                difLess>0 && difMore>0 -> {
                    items.addAll(firstLessList)
                    items.addAll(firstMoreList)
                }
                difLess>0 && difMore==0 -> {
                    val difList = itemFactory.loadMoreDirectionItems(firstMoreList.last().getID(), batchSize = difLess)
                    items.addAll(firstLessList)
                    items.addAll(firstMoreList)
                    items.addAll(difList)
                }
                difMore>0 && difLess==0 -> {
                    val difList = itemFactory.loadLessDirectionItems(firstLessList[0].getID(), batchSize = difMore)
                    items.addAll(difList)
                    items.addAll(firstLessList)
                    items.addAll(firstMoreList)
                }
                difLess==0 && difMore==0 ->{
                    items.addAll(firstLessList)
                    items.addAll(firstMoreList)
                }
            }
            list.addAll(items)
            state.scrollToItem(items.indexOfFirst { it.getID() == scrollId }, )
        }
        LazyColumn(state = state, modifier = Modifier) {

            if (isDownLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                }
            }

            itemsIndexed(list) { index, item ->
                Log.d("Pagination", "item = $item")
                Log.d("Pagination", "firstVisibleItemIndex = ${firstVisibleItemIndex} ")
                Log.d("Pagination", "list.last = ${list.last()} ")
                if (state.isScrollInProgress) {
                    when {
                        state.firstVisibleItemIndex > firstVisibleItemIndex -> {
                            Log.d("Pagination", "isForwardDirection ")
                            isForwardDirection = true
                            firstVisibleItemIndex = state.firstVisibleItemIndex
                        }
                        state.firstVisibleItemIndex < firstVisibleItemIndex -> {
                            Log.d("Pagination", "!isForwardDirection")
                            isForwardDirection = false
                            firstVisibleItemIndex = state.firstVisibleItemIndex
                        }
                    }
                }


                        when {
                            index == batchSize * 3 && isForwardDirection -> {
                                LaunchedEffect(index) {
                                    scope.launch {
                                        isUpLoading = true
                                        val items = itemFactory.loadMoreDirectionItems(
                                            list.last().getID(),
                                            batchSize = batchSize
                                        )
                                        Log.d("Pagination", "items = ${items}")
                                        val firstVisibleItem = state.firstVisibleItemIndex
                                        val firstVisibleItemOffcet =
                                            state.firstVisibleItemScrollOffset
                                        Log.d(
                                            "Pagination",
                                            "firstVisibleItem = $firstVisibleItem, firstVisibleItemOffcet = $firstVisibleItemOffcet"
                                        )
                                        list.removeRange(0, items.size)
                                        list.addAll(items)
                                        Log.d(
                                            "Pagination",
                                            "scrollToItem ${firstVisibleItem - batchSize}"
                                        )
                                        state.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                                            //  Toast.makeText(context, "PreventUserInput", Toast.LENGTH_SHORT).show()

                                        }
                                        state.scrollToItem(
                                            firstVisibleItem - items.size,
                                            firstVisibleItemOffcet
                                        )
                                        Log.d(
                                            "Pagination",
                                            "scrollToItem firstVisibleItem = $firstVisibleItem"
                                        )
                                        Log.d("Pagination", "list2 = ${list.size}")
                                        isUpLoading = false
                                    }
                                }
                            }
                            index == batchSize && !isForwardDirection -> {
                                LaunchedEffect(index) {
                                    scope.launch {
                                        Log.d(
                                            "Pagination",
                                            "index == batchSize && !isForwardDirection"
                                        )
                                        isDownLoading = true
                                        val items = itemFactory.loadLessDirectionItems(
                                            list[0].getID(),
                                            batchSize = batchSize
                                        )
                                        Log.d("Pagination", "items = ${items}")
                                        val firstVisibleItem = state.firstVisibleItemIndex
                                        val firstVisibleItemOffcet =
                                            state.firstVisibleItemScrollOffset
                                        Log.d(
                                            "Pagination",
                                            "firstVisibleItem = $firstVisibleItem, firstVisibleItemOffcet = $firstVisibleItemOffcet"
                                        )
                                        list.removeRange(list.size - items.size, list.size)
                                        list.addAll(0, items)
                                        Log.d(
                                            "Pagination",
                                            "scrollToItem ${firstVisibleItem - batchSize}"
                                        )
                                        state.scroll(scrollPriority = MutatePriority.PreventUserInput) {
                                            //  Toast.makeText(context, "PreventUserInput", Toast.LENGTH_SHORT).show()
                                        }
                                        state.scrollToItem(
                                            firstVisibleItem + items.size,
                                            firstVisibleItemOffcet
                                        )
                                        Log.d(
                                            "Pagination",
                                            "scrollToItem firstVisibleItem = $firstVisibleItem"
                                        )
                                        Log.d("Pagination", "list2 = ${list.size}")
                                        isDownLoading = false
                                    }
                                }
                            }
                            else -> Unit
                    }

                contentItem(item)
            }
            if (isUpLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

    }

}




interface Paging

data class User(
    val name: String,
    val uuid: String
) : Identifiable {
    override fun getID() = uuid
}


class Delimeter(val index: Int) : Paging


fun Modifier.gesturesDisabled(disabled: Boolean = true) = if (disabled) {
    pointerInput(Unit) {
        awaitPointerEventScope { // we should wait for all new pointer events
            while (true) {
                awaitPointerEvent(pass = PointerEventPass.Initial).changes.forEach(
                        PointerInputChange::consumeAllChanges
                    )
            }
        }
    }
} else {
    Modifier
}

fun LazyListState.reenableScrolling(scope: CoroutineScope) {
    scope.launch {
        scroll(scrollPriority = MutatePriority.PreventUserInput) { // Do nothing, just cancel the previous indefinite "scroll"
        }
    }
}