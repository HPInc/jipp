package com.hp.jipp.client;

import com.google.common.base.Optional;

import java.io.IOException;
import java.io.InputStream;

public abstract class IppDocument {

    public abstract String getDocumentType();
    public abstract InputStream openDocument() throws IOException;
    public abstract String getName();
}
