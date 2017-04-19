package com.hp.jipp.encoding;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

/** An attribute bearing a string for which language is irrelevant */
public class StringType extends AttributeType<String> {

    static Encoder<String> ENCODER = new Encoder<String>() {

        @Override
        public void writeValue(DataOutputStream out, String value) throws IOException {
            writeValueBytes(out, value.getBytes());
        }

        @Override
        public String readValue(DataInputStream in, Tag valueTag) throws IOException {
            return new String(readValueBytes(in));
        }

        @Override
        boolean valid(Tag valueTag) {
            return (valueTag.getValue() & 0x40) == 0x40 && valueTag != Tag.Uri;
        }
    };

    public StringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }

    @Override
    public Optional<Attribute<String>> adopt(Attribute<?> attribute) {
        if (!((attribute.getValueTag().equals(Tag.NameWithLanguage) && getTag().equals(Tag.NameWithoutLanguage)) ||
                (attribute.getValueTag().equals(Tag.TextWithLanguage) && getTag().equals(Tag.TextWithoutLanguage)))) {
            return Optional.absent();
        }
        // Apply conversion from StringType to a LangStringType attribute
        return Optional.of(of(Collections2.transform((Collection<LangString>) attribute.getValues(),
                LangString.ToStringFunc)));
    }
}
