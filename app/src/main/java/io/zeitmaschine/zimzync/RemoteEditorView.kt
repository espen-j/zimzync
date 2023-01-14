package io.zeitmaschine.zimzync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.zeitmaschine.zimzync.ui.theme.ZimzyncTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditorModel(private val dao: RemoteDao, remoteId: Int?) : ViewModel() {

    private var remote: Remote = Remote(null, "", "", "", "", "")
    var uiState: MutableStateFlow<Remote> = MutableStateFlow(remote)

    init {
        viewModelScope.launch {
            remoteId?.let {
                remote = dao.loadById(remoteId)
                uiState.value = remote
            }
        }
    }
    // FIXME? https://www.rrtutors.com/tutorials/implement-room-database-in-jetpack-compose
    suspend fun saveEntry(remote: Remote) {
        if (remote.uid == null) {
           dao.insert(remote)
        } else {
            dao.update(remote)
        }
    }
}

@Composable
fun EditRemote(
    remoteDao: RemoteDao,
    remoteId: Int?,
    viewModel: EditorModel = viewModel(factory = viewModelFactory {
        initializer {
            EditorModel(remoteDao, remoteId)
        }
    }),
    saveEntry: (remote: Remote) -> Unit
) {

    val remote: State<Remote?> = viewModel.uiState.collectAsState()
    remote.value?.let {
        EditorCompose(remote = it) { remote ->
            viewModel.viewModelScope.launch {
                viewModel.saveEntry(remote)
            }
            saveEntry(remote)
        }

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EditorCompose(
    remote: Remote,
    saveEntry: (remote: Remote) -> Unit
) {

    var uid by remember { mutableStateOf(remote.uid) }
    var name by remember { mutableStateOf(remote.name) }
    var url by remember { mutableStateOf(remote.url) }
    var key by remember { mutableStateOf(remote.key) }
    var secret by remember { mutableStateOf(remote.secret) }
    var bucket by remember { mutableStateOf(remote.secret) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(all = 16.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            value = name,
            onValueChange = { name = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") },
            value = url,
            onValueChange = { url = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Key") },
            value = key,
            onValueChange = { key = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Secret") },
            value = secret,
            onValueChange = { secret = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Bucket") },
            value = bucket,
            onValueChange = { bucket = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                // TODO
                saveEntry(Remote(uid, name, url, key, secret, bucket))
            }
        )
        {
            Text(text = "Save")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EditPreview() {
    ZimzyncTheme {
        EditorCompose(remote = Remote(null, "name", "urö", "key", "secret", "bucket")) {}
    }
}
