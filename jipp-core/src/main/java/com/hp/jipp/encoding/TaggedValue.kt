// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/**
 * Any attribute value which does not directly correspond to a common Java type.
 */
abstract class TaggedValue {
    /** Tag describing how the value is encoded */
    abstract val tag: Tag

    /** The value itself */
    abstract val value: Any
}
