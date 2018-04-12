// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

/** An error in creation of anything intended to be sent  */
class BuildError(s: String) : RuntimeException(s)
