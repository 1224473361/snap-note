package com.hushnote.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashDedupeTest {
    @Test
    fun collapseDuplicateIds_keepsFirstCopy() {
        val items = mutableListOf("a", "b", "a")
        val changed = items.collapseDuplicateIds { it }
        assertTrue(changed)
        assertEquals(listOf("a", "b"), items)
    }

    @Test
    fun collapseDuplicateIds_noChangeWhenIdsAreUnique() {
        val items = mutableListOf("a", "b")
        val changed = items.collapseDuplicateIds { it }
        assertFalse(changed)
        assertEquals(listOf("a", "b"), items)
    }
}
