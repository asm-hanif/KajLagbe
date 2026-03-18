package com.hanif.kajlagbe

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun InboxRow(
    chat: ChatItem,
    currentId: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var otherName by remember { mutableStateOf("Loading...") }

    LaunchedEffect(chat.chatId) {
        val otherId = if (chat.userId == currentId) chat.workerId else chat.userId
        val collection = if (chat.userId == currentId) "workers" else "users"

        firestore.collection(collection)
            .document(otherId)
            .get()
            .addOnSuccessListener {
                otherName = it.getString("name") ?: "Unknown"
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                otherName, 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}