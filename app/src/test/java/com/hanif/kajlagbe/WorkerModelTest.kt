package com.hanif.kajlagbe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkerModelTest {

    @Test
    fun testWorkerAvailabilityState() {
        // Test that your Worker model correctly handles the 'isBusy' and 'isAvailable' flags
        val worker = Worker(
            uid = "worker123",
            name = "Test Worker",
            isAvailable = true,
            isBusy = false
        )

        // Initial state
        assertTrue(worker.isAvailable)
        assertEquals(false, worker.isBusy)

        // Simulate taking a job
        worker.isBusy = true
        assertEquals(true, worker.isBusy)
        
        // Simulate going offline
        worker.isAvailable = false
        assertEquals(false, worker.isAvailable)
    }
}
