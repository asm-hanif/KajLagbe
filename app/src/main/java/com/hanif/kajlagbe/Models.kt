package com.hanif.kajlagbe

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Worker(
    val uid: String = "",
    val name: String = "",
    val gender: String = "",
    val workType: String = "",
    val institute: String = "",
    val contact: String = "",
    val location: String = "",
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    @get:JvmName("getIsAvailable")
    @set:JvmName("setIsAvailable")
    var isAvailable: Boolean = true,
    @get:PropertyName("isBusy")
    @set:PropertyName("isBusy")
    @get:JvmName("getIsBusy")
    @set:JvmName("setIsBusy")
    var isBusy: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0
)

data class Review(
    val reviewId: String = "",
    val workerId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class JobRequest(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val workType: String = "",
    val location: String = "",
    val note: String = "",
    val status: String = "pending", // pending, accepted, completed, cancelled
    val timestamp: Long = 0L,
    val scheduledDate: String = "" // For booking calendar feature
)
