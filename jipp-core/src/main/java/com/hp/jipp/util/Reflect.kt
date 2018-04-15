// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

import java.lang.reflect.Modifier

/** Use reflection to return all static, accessible, initialized objects in the class  */
fun Class<*>.getStaticObjects(): List<Any> =
        declaredFields
                .filter { Modifier.isStatic(it.modifiers) }
                .mapNotNull {
                    try {
                        it.get(null)
                    } catch (ignored: IllegalAccessException) {
                        null
                    }
                }
