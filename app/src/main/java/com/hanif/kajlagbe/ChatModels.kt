package com.hanif.kajlagbe

data class Message(
    val senderId: String = "",
    val text: String = "",
    val time: Long = 0L
)

data class ChatItem(
    val chatId: String = "",
    val userId: String = "",
    val workerId: String = "",
    val lastMessage: String = "",
    val lastTime: Long = 0L,
    val userDeleted: Boolean = false,
    val workerDeleted: Boolean = false
)