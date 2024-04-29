// Copyright 2018 - 2021 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package util

import org.junit.Assert

object KotlinTest {
    @JvmStatic fun cover(o: Any, same: Any, diff: Any) {
        val notObject = Any()
        Assert.assertFalse(o == notObject)
        Assert.assertTrue(o == o)
        Assert.assertEquals(o, same)
        Assert.assertEquals(o.hashCode(), same.hashCode())
        Assert.assertEquals(o.toString(), same.toString())
        Assert.assertNotEquals(o, diff)

        // Call all component methods, if they are present
        o::class.java.declaredMethods.forEach { method ->
            if (method.name.startsWith("component")) {
                method.invoke(o)
            }
        }
    }
}
