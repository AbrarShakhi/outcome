package com.github.abrarshakhi.outcome

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OutcomeTest {

    @Test
    fun `ofOk should create Ok`() {
        val outcome = Outcome.ofOk(42)
        assertTrue(outcome.isOk())
        assertEquals(42, outcome.getOrNull())
    }

    @Test
    fun `ofErr should create Err`() {
        val error = "fail"
        val outcome = Outcome.ofErr(error)
        assertTrue(outcome.isErr())
        assertEquals(error, outcome.errorOrNull())
    }

    @Test
    fun `maybeThrows returns Ok on success`() {
        val outcome = Outcome.maybeThrows { 5 + 3 }
        assertTrue(outcome.isOk())
        assertEquals(8, outcome.getOrNull())
    }

    @Test
    fun `maybeThrows returns Err on exception`() {
        val outcome = Outcome.maybeThrows { throw IllegalArgumentException("oops") }
        assertTrue(outcome.isErr())
        assertTrue(outcome.errorOrNull() is IllegalArgumentException)
    }

    @Test
    fun `maybeThrows rethrows CancellationException`() {
        assertFailsWith<CancellationException> {
            Outcome.maybeThrows { throw CancellationException() }
        }
    }

    @Test
    fun `isOk and isErr are mutually exclusive`() {
        val ok = Outcome.ofOk(1)
        val err = Outcome.ofErr("fail")

        assertTrue(ok.isOk())
        assertTrue(!ok.isErr())

        assertTrue(err.isErr())
        assertTrue(!err.isOk())
    }


    @Test
    fun `getOrThrow returns value for Ok`() {
        val outcome = Outcome.ofOk(100)
        assertEquals(100, outcome.getOrThrow())
    }

    @Test
    fun `getOrThrow throws error for Err`() {
        val error = IllegalStateException("fail")
        val outcome = Outcome.ofErr(error)
        assertFailsWith<IllegalStateException> {
            outcome.getOrThrow()
        }
    }

    @Test
    fun `errorOrThrow returns error for Err`() {
        val outcome = Outcome.ofErr("fail")
        assertEquals("fail", outcome.errorOrThrow())
    }

    @Test
    fun `errorOrThrow throws for Ok`() {
        val outcome = Outcome.ofOk(42)
        assertFailsWith<IllegalStateException> {
            outcome.errorOrThrow()
        }
    }

    @Test
    fun `getOrElse returns value for Ok`() {
        val outcome = Outcome.ofOk(10)
        val result = outcome.getOrElse { 0 }
        assertEquals(10, result)
    }

    @Test
    fun `getOrElse returns fallback for Err`() {
        val outcome = Outcome.ofErr("fail")
        val result = outcome.getOrElse { 100 }
        assertEquals(100, result)
    }

    @Test
    fun `getOrNull returns null for Err`() {
        val outcome = Outcome.ofErr("error")
        assertEquals(null, outcome.getOrNull())
    }

    @Test
    fun `errorOrNull returns null for Ok`() {
        val outcome = Outcome.ofOk(123)
        assertEquals(null, outcome.errorOrNull())
    }
}