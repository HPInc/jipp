package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;

public class UriEncoder extends AttributeEncoder<URI> {

    private static final UriEncoder INSTANCE = new UriEncoder();
    public static UriEncoder getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeValue(DataOutputStream out, URI value) throws IOException {
        StringEncoder.getInstance().writeValue(out, value.toString());
    }

    @Override
    public URI readValue(DataInputStream in, Tag valueTag) throws IOException {
        return URI.create(StringEncoder.getInstance().readValue(in, valueTag));
    }

    @Override
    boolean valid(Tag valueTag) {
        return valueTag == Tag.Uri;
    }
}
