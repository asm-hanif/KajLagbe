package com.hanif.kajlagbe

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatTest {

    @Test
    fun testChatMessageContent() {
        val message = Message(
            senderId = "user1",
            text = "Hello Worker!",
            time = 123456789L
        )

        assertEquals("user1", message.senderId)
        assertEquals("Hello Worker!", message.text)
        assertEquals(123456789L, message.time)
    }

    @Test
    fun testChatItemState() {
        val chat = ChatItem(
            chatId = "chat_123",
            userDeleted = false,
            workerDeleted = false
        )

        // Simulate deleting chat for user
        val updatedChat = chat.copy(userDeleted = true)
        
        assertEquals(true, updatedChat.userDeleted)
        assertEquals(false, updatedChat.workerDeleted)
    }
}
