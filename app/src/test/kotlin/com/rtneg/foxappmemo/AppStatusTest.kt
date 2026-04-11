package com.rtneg.foxappmemo

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for AppStatus enum parsing.
 */
class AppStatusTest {

    @Test
    fun `fromLabel returns correct status for each label`() {
        val statuses = listOf("trying", "main", "avoid", "blacklist", "reconsider")
        statuses.forEach { label ->
            val status = com.rtneg.foxappmemo.data.entity.AppStatus.fromLabel(label)
            assertEquals(label, status.label)
        }
    }

    @Test
    fun `fromLabel returns TRYING for unknown label`() {
        val status = com.rtneg.foxappmemo.data.entity.AppStatus.fromLabel("unknown")
        assertEquals(com.rtneg.foxappmemo.data.entity.AppStatus.TRYING, status)
    }
}
