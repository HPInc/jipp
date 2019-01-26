// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.pdl.util

import java.io.OutputStream

/** An output stream that writes data to nowhere. */
object NullOutputStream : OutputStream() {
    override fun write(p0: Int) { }
}