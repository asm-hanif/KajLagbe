package com.hanif.kajlagbe

import org.junit.Assert.assertEquals
import org.junit.Test
import com.google.firebase.Timestamp

class ReviewTest {

    @Test
    fun testReviewDataIntegrity() {
        // Test that a Review object correctly maps its fields
        val review = Review(
            reviewId = "rev_001",
            workerId = "worker_001",
            userId = "user_001",
            userName = "John Doe",
            rating = 4,
            comment = "Great job!"
        )

        assertEquals("rev_001", review.reviewId)
        assertEquals("worker_001", review.workerId)
        assertEquals(4, review.rating)
        assertEquals("Great job!", review.comment)
    }
}
