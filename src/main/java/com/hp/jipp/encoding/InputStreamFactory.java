package com.hp.jipp.encoding;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamFactory {
    // Note: Similar to Supplier<InputStream> but allows for IOException to be thrown.

    /** Return a valid input stream. It is presumed that the caller will close */
    InputStream createInputStream() throws IOException;
}
