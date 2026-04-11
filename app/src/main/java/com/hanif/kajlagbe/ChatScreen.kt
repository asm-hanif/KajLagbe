package com.hanif.kajlagbe

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatId: String) {

    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedMsgTime by remember { mutableStateOf<Long?>(null) }

    /* ✅ Load Messages */
    LaunchedEffect(chatId) {
        firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("time")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.map { doc ->
                        Message(
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            time = doc.getLong("time") ?: 0L
                        )
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {

            /* ✅ Messages List */
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {

                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        /* 🕒 Message Time */
                        Text(
                            text = formatMessageTime(msg.time),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        if (isMe) {
                                            selectedMsgTime = msg.time
                                            showDeleteDialog = true
                                        }
                                    }
                                )
                        ) {
                            Text(
                                msg.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            /* ✅ Send Box */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_message)) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newMessage.isBlank()) return@IconButton

                        val msgData = mapOf(
                            "senderId" to currentUserId,
                            "text" to newMessage,
                            "time" to System.currentTimeMillis()
                        )

                        firestore.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .add(msgData)

                        firestore.collection("chats")
                            .document(chatId)
                            .get()
                            .addOnSuccessListener { doc ->
                                val userId = doc.getString("userId") ?: ""
                                val workerId = doc.getString("workerId") ?: ""

                                val updates = mutableMapOf<String, Any>(
                                    "lastMessage" to newMessage,
                                    "lastTime" to System.currentTimeMillis()
                                )

                                if (currentUserId == userId) updates["userDeleted"] = false
                                if (currentUserId == workerId) updates["workerDeleted"] = false

                                firestore.collection("chats")
                                    .document(chatId)
                                    .update(updates)
                            }
                        newMessage = ""
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = stringResource(R.string.send))
                }
            }
        }
    }

    /* ✅ Delete Message Popup */
    if (showDeleteDialog && selectedMsgTime != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_message)) },
            text = { Text(stringResource(R.string.delete_message_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    firestore.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .whereEqualTo("time", selectedMsgTime)
                        .get()
                        .addOnSuccessListener { docs ->
                            for (d in docs) {
                                d.reference.delete()
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
