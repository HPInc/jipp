package com.hp.jipp.encoding;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamFactory {

    /** Return a valid input stream. It is presumed that the caller will close */
    InputStream createInputStream() throws IOException;
}
