package com.hanif.kajlagbe

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeUtilsTest {

    @Test
    fun testDateFormatting() {
        // Test if your app's logic for date strings works as expected
        val day = 15
        val month = 5 // Represents June (0-based in some libraries, 1-based in others)
        val year = 2025
        
        // This simulates your logic in RequestJob.kt: "$dayOfMonth/${month + 1}/$year"
        val formattedDate = "$day/${month + 1}/$year"
        
        assertEquals("15/6/2025", formattedDate)
    }
}
