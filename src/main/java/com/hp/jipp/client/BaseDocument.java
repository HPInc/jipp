package com.hp.jipp.client;

import java.io.IOException;
import java.io.InputStream;

public abstract class BaseDocument {
    public abstract String getDocumentType();
    public abstract InputStream openDocument() throws IOException;

    /** Returns the document's name, or null if not known. */
    public String getName() {
        return null;
    }
}
