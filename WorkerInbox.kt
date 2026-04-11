package com.hanif.kajlagbe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerInbox(navController: NavController) {

    val firestore = FirebaseFirestore.getInstance()
    val workerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var chats by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedChat by remember { mutableStateOf<ChatItem?>(null) }

    /* Load Worker Chats*/
    LaunchedEffect(Unit) {
        firestore.collection("chats")
            .whereEqualTo("workerId", workerId)
            .whereEqualTo("workerDeleted", false)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    chats = snapshot.documents.map { doc ->
                        ChatItem(
                            chatId = doc.id,
                            workerId = doc.getString("workerId") ?: "",
                            userId = doc.getString("userId") ?: "",
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastTime = doc.getLong("lastTime") ?: 0L,
                            userDeleted = doc.getBoolean("userDeleted") ?: false,
                            workerDeleted = doc.getBoolean("workerDeleted") ?: false
                        )
                    }.sortedByDescending { it.lastTime }
                    loading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.worker_inbox)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (chats.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_chats),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(chats, key = { it.chatId }) { chat ->
                        InboxRow(
                            chat = chat,
                            currentId = workerId,
                            onClick = {
                                navController.navigate("chat/${chat.chatId}")
                            },
                            onLongPress = {
                                selectedChat = chat
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    /* ✅ Delete Chat Popup */
    if (showDeleteDialog && selectedChat != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_conversation)) },
            text = { Text(stringResource(R.string.delete_conversation_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    val chat = selectedChat!!
                    firestore.collection("chats")
                        .document(chat.chatId)
                        .update("workerDeleted", true)
                        .addOnSuccessListener {
                            if (chat.userDeleted) {
                                firestore.collection("chats")
                                    .document(chat.chatId)
                                    .delete()
                            }
                        }
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.yes), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}
