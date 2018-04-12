// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.model;

import java.io.IOException;
import java.io.InputStream;

// Note: this must remain a Java interface to allow Kotlin to declare as
// InputStreamFactory { ... }
// See https://youtrack.jetbrains.com/issue/KT-7770

public interface InputStreamFactory {
    // Note: Similar to Supplier<InputStream> but allows for IOException to be thrown.

    /** Return a valid input stream. It is presumed that the caller will close */
    InputStream createInputStream() throws IOException;
}
