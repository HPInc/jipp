// Copyright 2017 - 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

import java.io.IOException

/** A parsing error occurred */
class ParseError : IOException {
    constructor(s: String) : super(s)
    constructor(s: String, t: Throwable) : super(s, t)
}
