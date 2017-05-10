package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;

public class UriType extends AttributeType<URI> {
    private static final String TYPE_NAME = "URI";

    static final Attribute.SimpleEncoder<URI> ENCODER = new Attribute.SimpleEncoder<URI>(TYPE_NAME) {
        @Override
        public void writeValue(DataOutputStream out, URI value) throws IOException {
            StringType.ENCODER.writeValue(out, value.toString());
        }

        @Override
        public URI readValue(DataInputStream in, Tag valueTag) throws IOException {
            return URI.create(StringType.ENCODER.readValue(in, valueTag));
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.Uri;
        }
    };

    public UriType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
