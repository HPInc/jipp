package com.hp.jipp.util

import org.junit.Assert.* // ktlint-disable no-wildcard-imports

class KotlinTest {
    companion object {
        val NOT_O = Any()
        @JvmStatic fun cover(o: Any, same: Any, diff: Any) {
            assertFalse(o == NOT_O)
            assertTrue(o == o)
            assertEquals(o, same)
            assertEquals(o.hashCode(), same.hashCode())
            assertEquals(o.toString(), same.toString())
            assertNotEquals(o, diff)

            // Call all component methods, if they are present
            o::class.java.declaredMethods.forEach { method ->
                if (method.name.startsWith("component")) {
                    method.invoke(o)
                }
            }
        }
    }
}
