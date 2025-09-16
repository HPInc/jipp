// © Copyright 2017 - 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

import org.junit.Assert.* // ktlint-disable no-wildcard-imports

class KotlinTest {
    companion object {
        @JvmStatic fun cover(o: Any, same: Any, diff: Any) {
            val notObject = Any()
            assertFalse(o == notObject)
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
