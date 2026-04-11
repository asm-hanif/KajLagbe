package com.hanif.kajlagbe

import org.junit.Assert.assertEquals
import org.junit.Test

class JobRequestTest {

    @Test
    fun testJobRequestDefaultValues() {
        val jobRequest = JobRequest(
            requestId = "123",
            userName = "Test User",
            status = "pending"
        )

        assertEquals("123", jobRequest.requestId)
        assertEquals("Test User", jobRequest.userName)
        assertEquals("pending", jobRequest.status)
    }

    @Test
    fun testJobRequestStatusUpdate() {
        var jobRequest = JobRequest(status = "pending")
        // Simulate changing status
        jobRequest = jobRequest.copy(status = "accepted")
        
        assertEquals("accepted", jobRequest.status)
    }
}
