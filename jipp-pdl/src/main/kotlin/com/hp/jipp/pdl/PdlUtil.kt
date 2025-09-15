// © Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl

/** Return true if the integer is odd. */
val Int.isOdd: Boolean
    get() = this % 2 == 1

/** Return true if the integer is odd. */
val Int.isEven: Boolean
    get() = !this.isOdd
