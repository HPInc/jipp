// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Any attribute value which does not directly correspond to a common Java type.
 */
abstract class TaggedValue {
    // Defined as an abstract class so that it may be implemented by data classes.

    /** Tag describing how the value is encoded */
    abstract val tag: ValueTag

    /** The value itself. Must be [Any] because subclasses may support multiple Java types. */
    abstract val value: Any
}
