package com.github.abrarshakhi.outcome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OutcomeExtensionsTest {

    @Test
    fun `mapCatching transforms Ok correctly`() {
        val outcome: Outcome<Int, Throwable> = Outcome.ofOk(5)
        val mapped = outcome.mapCatching { it * 2 }
        assertTrue(mapped.isOk())
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `mapCatching converts thrown exception into Err`() {
        val outcome: Outcome<Int, Throwable> = Outcome.ofOk(5)
        val mapped = outcome.mapCatching { throw IllegalStateException("fail") }
        assertTrue(mapped.isErr())
        assertTrue(mapped.errorOrNull() is IllegalStateException)
        assertEquals("fail", mapped.errorOrNull()?.message)
    }

    @Test
    fun `map leaves Ok transformed and Err unchanged`() {
        val okOutcome = Outcome.ofOk(3).map { it * 4 }
        assertEquals(12, okOutcome.getOrNull())

        val errOutcome = Outcome.ofErr("oops").map { it.toString() }
        assertEquals("oops", errOutcome.errorOrNull())
    }

    @Test
    fun `mapError transforms Err and leaves Ok unchanged`() {
        val okOutcome = Outcome.ofOk(10).mapError { it.toString() }
        assertEquals(10, okOutcome.getOrNull())

        val errOutcome = Outcome.ofErr(5).mapError { it * 2 }
        assertEquals(10, errOutcome.errorOrNull())
    }

    @Test
    fun `flatMap chains Ok and leaves Err unchanged`() {
        val okOutcome = Outcome.ofOk(2).flatMap { Outcome.ofOk(it + 3) }
        assertEquals(5, okOutcome.getOrNull())

        val errOutcome = Outcome.ofErr("fail").flatMap { Outcome.ofOk(100) }
        assertEquals("fail", errOutcome.errorOrNull())
    }

    @Test
    fun `fold executes correct branch`() {
        val okOutcome = Outcome.ofOk(10)
        val errOutcome = Outcome.ofErr("fail")

        val okResult = okOutcome.fold({ it * 2 }, { 0 })
        val errResult = errOutcome.fold({ it: Int -> it * 2 }, { -1 })

        assertEquals(20, okResult)
        assertEquals(-1, errResult)
    }

    @Test
    fun `onOk performs action only on Ok`() {
        var captured = 0
        Outcome.ofOk(5).onOk { captured = it * 2 }
        assertEquals(10, captured)

        captured = 0
        Outcome.ofErr("fail").onOk { captured = 100 }
        assertEquals(0, captured)
    }

    @Test
    fun `onErr performs action only on Err`() {
        var captured: String? = null
        Outcome.ofOk(5).onErr { captured = it }
        assertEquals(null, captured)

        Outcome.ofErr("oops").onErr { captured = it }
        assertEquals("oops", captured)
    }

    @Test
    fun `getOrThrowWith returns value for Ok and throws for Err`() {
        val okOutcome = Outcome.ofOk(42)
        assertEquals(42, okOutcome.getOrThrowWith { IllegalStateException("fail") })

        val errOutcome = Outcome.ofErr("error")
        assertFailsWith<IllegalArgumentException> {
            errOutcome.getOrThrowWith { IllegalArgumentException("custom") }
        }
    }

    @Test
    fun `errorOrThrowWith returns error for Err and throws for Ok`() {
        val errOutcome = Outcome.ofErr("fail")
        assertEquals("fail", errOutcome.errorOrThrowWith { IllegalStateException("fail") })

        val okOutcome = Outcome.ofOk(10)
        assertFailsWith<IllegalArgumentException> {
            okOutcome.errorOrThrowWith { IllegalArgumentException("custom") }
        }
    }

    @Test
    fun `recoverWith replaces Err with Ok`() {
        val outcome: Outcome<Int, String> = Outcome.ofErr("fail")
        val recovered = outcome.recoverWith { Outcome.ofOk(100) }
        assertTrue(recovered.isOk())
        assertEquals(100, recovered.getOrNull())

        val okOutcome = Outcome.ofOk(50)
        val unchanged = okOutcome.recoverWith { Outcome.ofOk(0) }
        assertEquals(50, unchanged.getOrNull())
    }

    @Test
    fun `flatten unwraps nested Outcome`() {
        val nestedOk: Outcome<Outcome<Int, String>, String> = Outcome.ofOk(Outcome.ofOk(42))
        val flatOk = nestedOk.flatten()
        assertEquals(42, flatOk.getOrNull())

        val nestedErr: Outcome<Outcome<Int, String>, String> = Outcome.ofErr("fail")
        val flatErr = nestedErr.flatten()
        assertEquals("fail", flatErr.errorOrNull())
    }

    @Test
    fun `toPair returns value and null error for Ok`() {
        val outcome = Outcome.ofOk(42)

        val (value, error) = outcome.toPair()

        assertEquals(42, value)
        assertEquals(null, error)
    }

    @Test
    fun `toPair returns null value and error for Err`() {
        val outcome = Outcome.ofErr("fail")

        val (value, error) = outcome.toPair()

        assertEquals(null, value)
        assertEquals("fail", error)
    }

    @Test
    fun `map does not catch exception`() {
        val outcome = Outcome.ofOk(5)

        assertFailsWith<IllegalStateException> {
            outcome.map { throw IllegalStateException("boom") }
        }
    }


    @Test
    fun `recoverWith can return another Err`() {
        val outcome: Outcome<Int, String> = Outcome.ofErr("fail")

        val recovered = outcome.recoverWith { Outcome.ofErr("newFail") }

        assertTrue(recovered.isErr())
        assertEquals("newFail", recovered.errorOrNull())
    }

    @Test
    fun `flatten unwraps inner Err correctly`() {
        val nested: Outcome<Outcome<Int, String>, String> = Outcome.ofOk(Outcome.ofErr("innerFail"))

        val flat = nested.flatten()

        assertTrue(flat.isErr())
        assertEquals("innerFail", flat.errorOrNull())
    }

    @Test
    fun `flatMap does not execute on Err`() {
        var executed = false

        val outcome: Outcome<Int, String> = Outcome.ofErr("fail")

        outcome.flatMap {
            executed = true
            Outcome.ofOk(it)
        }

        assertTrue(!executed)
    }
}