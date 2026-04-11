package com.hanif.kajlagbe

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ---------------- TIME FORMAT ---------------- */
fun formatInboxTime(time: Long): String {
    if (time == 0L) return ""
    
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(time), 
        ZoneId.systemDefault()
    )
    
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}

fun formatMessageTime(time: Long): String {
    if (time == 0L) return ""
    
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(time), 
        ZoneId.systemDefault()
    )
    
    // Format: "15 Mar, 10:30 AM"
    val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}
