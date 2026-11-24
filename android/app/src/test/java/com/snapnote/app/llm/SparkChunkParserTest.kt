package com.snapnote.app.llm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SparkChunkParserTest {
    @Test
    fun parsesContentAndLastStatus() {
        val json = """
        {"header":{"code":0,"message":"Success","status":2},
         "payload":{"choices":{"status":2,"text":[{"content":"真香","role":"assistant"}]}}}
        """.trimIndent()
        val chunk = SparkChunkParser.parse(json)
        assertEquals(0, chunk.code)
        assertEquals("真香", chunk.contentDelta)
        assertTrue(chunk.isLast)
    }

    @Test
    fun nonZeroCodeIsErrorNotLastContent() {
        val json = """{"header":{"code":10013,"message":"illegal access","status":2}}"""
        val chunk = SparkChunkParser.parse(json)
        assertEquals(10013, chunk.code)
        assertEquals("illegal access", chunk.message)
        assertEquals("", chunk.contentDelta)
        assertTrue(chunk.isLast)
    }

    @Test
    fun intermediateFrameNotLast() {
        val json = """
        {"header":{"code":0,"message":"Success","status":1},
         "payload":{"choices":{"status":1,"text":[{"content":"今"}]}}}
        """.trimIndent()
        val chunk = SparkChunkParser.parse(json)
        assertEquals("今", chunk.contentDelta)
        assertFalse(chunk.isLast)
    }
}
