package io.zeitmaschine.zimzync.ui.screens.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.zeitmaschine.zimzync.data.remote.RemoteDao
import io.zeitmaschine.zimzync.ui.theme.ZimzyncTheme
import io.zeitmaschine.zimzync.ui.theme.containerBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class MainViewModel(private val dataStore: RemoteDao) : ViewModel() {


    private val selected = mutableListOf<Int>()
    private val _selectedState = MutableStateFlow(emptyList<Int>())

    private val _remotes = flow {
        val items = dataStore.getAll()
        emit(items)
    }

    val remotesState = combine(_remotes, _selectedState)
    { remotes, sel -> remotes.map { RemoteView(it.uid!!, it.name, it.url, sel.contains(it.uid)) } }


    fun select(remoteId: Int) {
        if (selected.contains(remoteId)) {
            selected.remove(remoteId)
        } else {
            selected.add(remoteId)
        }
        // Trigger state update
        _selectedState.value = selected.toList()
    }
}

@Composable
fun RemoteScreen(
    remoteDao: RemoteDao,
    // https://programmer.ink/think/a-new-way-to-create-a-viewmodel-creationextras.html
    viewModel: MainViewModel = viewModel(factory = viewModelFactory {
        initializer {
            MainViewModel(remoteDao)
        }
    }),
    openSync: (Int) -> Unit,
) {
    val remotes = viewModel.remotesState.collectAsState(initial = emptyList())
    RemoteComponent(remotes = remotes.value, openSync = openSync) { viewModel.select(it) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteComponent(remotes: List<RemoteView>, openSync: (Int) -> Unit, select: (Int) -> Unit) {

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(remotes) { remote ->
            Box(
                modifier = Modifier
                    // Note: Order matters!
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = containerBackground())
                    .then(
                        if (remote.selected) Modifier.border(
                            width = Dp(2f),
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { openSync(remote.uid) },
                        onLongClick = { select(remote.uid) })
                    .padding(16.dp)
                ) {
                Column {
                    Text(remote.name, color = MaterialTheme.colorScheme.onSurface)
                    Text(remote.url, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZimzyncTheme {
        val remotes = generateSequence(0) { it + 1 }.take(10).map {
            RemoteView(
                uid = it,
                name = "test $it",
                url = "https://blob.rawbot.zone/$it",
            )
        }.toList()

        RemoteComponent(remotes = remotes, openSync = {}) {}
    }
}

data class RemoteView(val uid: Int, val name: String, val url: String, val selected: Boolean = false)