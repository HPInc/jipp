package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;

public class UriType extends AttributeType<URI> {
    static final Attribute.Encoder<URI> ENCODER = new Attribute.Encoder<URI>() {
        @Override
        public String getType() {
            return UriType.class.getSimpleName();
        }

        @Override
        public void writeValue(DataOutputStream out, URI value) throws IOException {
            StringType.ENCODER.writeValue(out, value.toString());
        }

        @Override
        public URI readValue(DataInputStream in, Tag valueTag) throws IOException {
            return URI.create(StringType.ENCODER.readValue(in, valueTag));
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.Uri;
        }
    };

    public UriType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }
}
