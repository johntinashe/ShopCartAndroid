package com.shopcart.shopcart.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.data.ChatMessage
import com.shopcart.shopcart.data.ChatRepository
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.asDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val chat: ChatRepository, private val auth: AuthRepository) : ViewModel() {
    val uid = auth.requireUid()
    val messages: StateFlow<List<ChatMessage>> =
        chat.messages(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var sending by mutableStateOf(false)
        private set

    fun send(text: String, onSent: () -> Unit) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || sending) return
        viewModelScope.launch {
            sending = true
            runCatching {
                val name = runCatching { auth.profile(uid).first().fullName }.getOrDefault("")
                chat.send(uid, name.ifBlank { auth.userNow?.email.orEmpty() }, trimmed)
            }.onSuccess { onSent() }
            sending = false
        }
    }

    fun markSeen(list: List<ChatMessage>) {
        viewModelScope.launch { runCatching { chat.markRepliesSeen(uid, list) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { ChatViewModel(container.chat, container.auth) }
    val messages by vm.messages.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
            vm.markSeen(messages)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & support") },
                navigationIcon = { IconButton(onClick = onBack) { AppIcon(R.drawable.ic_arrow_back, "Back") } },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 4000) draft = it },
                        placeholder = { Text("Type a message") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        modifier = Modifier.weight(1f),
                    )
                    FilledIconButton(
                        onClick = { vm.send(draft) { draft = "" } },
                        enabled = draft.isNotBlank() && !vm.sending,
                    ) { AppIcon(R.drawable.ic_send, "Send") }
                }
            }
        },
    ) { padding ->
        if (messages.isEmpty()) {
            EmptyState(
                R.drawable.ic_chat,
                "How can we help?",
                "Ask about an order, delivery or a product. We usually reply within a few hours.",
                Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages, key = { it.id }) { m -> Bubble(m, mine = m.from == vm.uid) }
            }
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage, mine: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .background(
                    if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (mine) 16.dp else 4.dp,
                        bottomEnd = if (mine) 4.dp else 16.dp,
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                message.text,
                color = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                message.time.asDate("d MMM, HH:mm"),
                style = MaterialTheme.typography.labelSmall,
                color = (if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    .copy(alpha = 0.75f),
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

