// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

import java.io.IOException

/** A parsing error occurred */
class ParseError(s: String) : IOException(s)
