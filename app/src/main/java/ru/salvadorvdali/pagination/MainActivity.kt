package ru.salvadorvdali.pagination

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.salvadorvdali.pagination.ui.theme.PaginationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val datasource = Datasource() // val repository = PaginationRepository(datasource)
        setContent {
            PaginationTheme { // A surface container using the 'background' color from the theme

                //val users by repository.getUsers(0, true).collectAsState(initial = emptyList())
                val users = datasource.getAll()
                val factory: ItemFactory<User> = ItemFactoryImpl(datasource)
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {

                    Pagination<User>(itemFactory = factory, batchSize = 20) { user ->
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


@Composable
fun <T : Identifiable> Pagination(
    itemFactory: ItemFactory<T>,
    batchSize: Int,
    scrollToIndex: Flow<Int> = MutableStateFlow(0),
    contentItem: @Composable (T) -> Unit
) {

    val scrollIndex by scrollToIndex.collectAsState(initial = 0)
    var list = remember { mutableStateListOf<T>() }
    var initialId by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }
    var isForwardDirection = true
    var firstVisibleItemIndex = 0
    val state = rememberLazyListState()


    LaunchedEffect(key1 = itemFactory) {
        val items = itemFactory.loadItems(0, batchSize = batchSize * 4, true)
        list.addAll(items)
    }


    LazyColumn(state = state, modifier = Modifier) {
        itemsIndexed(list) { index, item ->
            Log.d("Pagination", "item = $item")
            Log.d("Pagination", "state.firstVisibleItemIndex = ${state.firstVisibleItemIndex} ")
            Log.d("Pagination", "firstVisibleItemIndex = ${firstVisibleItemIndex} ")
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
            LaunchedEffect(index) {
                when {
                    index == batchSize * 3 && isForwardDirection -> {
                        isLoading = true
                        val items =
                            itemFactory.loadItems(list.last().getID(), batchSize = batchSize, true)
                        Log.d("Pagination", "items = ${items}")
                        val firstVisibleItem = state.firstVisibleItemIndex
                        val firstVisibleItemOffcet = state.firstVisibleItemScrollOffset
                        Log.d(
                            "Pagination",
                            "firstVisibleItem = $firstVisibleItem, firstVisibleItemOffcet = $firstVisibleItemOffcet"
                        )
                        list.removeRange(0, batchSize)
                        list.addAll(items)
                        Log.d("Pagination", "scrollToItem ${firstVisibleItem - batchSize}")
                        state.scroll(scrollPriority = MutatePriority.PreventUserInput) { }
                        state.scrollToItem(firstVisibleItem - batchSize, firstVisibleItemOffcet)
                        Log.d("Pagination", "scrollToItem firstVisibleItem = $firstVisibleItem")
                        Log.d("Pagination", "list2 = ${list.size}")
                        isLoading = false
                    }
                    index == batchSize && !isForwardDirection -> {
                        Log.d("Pagination", "index == batchSize && !isForwardDirection")
                        isLoading = true
                        val items =
                            itemFactory.loadItems(list[0].getID(), batchSize = batchSize, false)
                        Log.d("Pagination", "items = ${items}")
                        val firstVisibleItem = state.firstVisibleItemIndex
                        val firstVisibleItemOffcet = state.firstVisibleItemScrollOffset
                        Log.d(
                            "Pagination",
                            "firstVisibleItem = $firstVisibleItem, firstVisibleItemOffcet = $firstVisibleItemOffcet"
                        )
                        list.removeRange(list.size - items.size, list.size)
                        list.addAll(0, items)
                        Log.d("Pagination", "scrollToItem ${firstVisibleItem - batchSize}")
                        state.scroll(scrollPriority = MutatePriority.PreventUserInput) { }
                        state.scrollToItem(firstVisibleItem + items.size, firstVisibleItemOffcet)
                        Log.d("Pagination", "scrollToItem firstVisibleItem = $firstVisibleItem")
                        Log.d("Pagination", "list2 = ${list.size}")
                        isLoading = false
                    }
                    else -> Unit
                }

            }
            contentItem(item)
        }
        if (isLoading) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    @Composable
    fun getItems() { ////
    }
}




interface Paging

data class User(
    val name: String, val id: Int
) : Identifiable {
    override fun getID() = id
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