package com.hp.jipp.util

import org.junit.Assert.*

class KotlinTest {

    companion object {
        private val NOT_O = Any()
        @JvmStatic fun cover(o: Any, same: Any, diff: Any) {
            assertFalse(o == NOT_O)
            assertTrue(o == o)
            assertEquals(o, same)
            assertEquals(o.hashCode(), same.hashCode())
            assertEquals(o.toString(), same.toString())
            assertNotEquals(o, diff);
        }
    }
}
