// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Any attribute value which does not directly correspond to a common Java type.
 */
interface TaggedValue {
    /** Tag describing how the value is encoded */
    val tag: ValueTag

    /** The value itself. Must be [Any] because subclasses may support multiple Java types. */
    val value: Any
}
