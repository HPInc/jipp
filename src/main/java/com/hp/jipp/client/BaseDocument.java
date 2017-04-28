package com.hp.jipp.client;

import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;

public abstract class BaseDocument {

    public abstract String getDocumentType();
    public abstract InputStream openDocument() throws IOException;

    /** Returns the document's name if known. */
    public Optional<String> getName() {
        return Optional.absent();
    }
}
