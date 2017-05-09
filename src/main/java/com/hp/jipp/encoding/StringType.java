package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * An attribute bearing a string for which language is irrelevant.
 *
 * Some string types have a length-limit.
 */
public class StringType extends AttributeType<String> {
    private static final String TYPE_NAME = "String";

    public static final Attribute.Encoder<String> ENCODER = new Attribute.Encoder<String>(TYPE_NAME) {
        @Override
        public void writeValue(DataOutputStream out, String value) throws IOException {
            writeValueBytes(out, value.getBytes(Util.UTF8));
        }

        @Override
        public String readValue(DataInputStream in, Tag valueTag) throws IOException {
            return new String(Attribute.readValueBytes(in), Util.UTF8);
        }

        @Override
        public boolean valid(Tag valueTag) {
            return (valueTag.getCode() & 0x40) == 0x40;
        }
    };

    public StringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }

    @Override
    @SuppressWarnings({"PMD.UselessParentheses", "unchecked"})
    public Optional<Attribute<String>> of(Attribute<?> attribute) {
        if (!(attribute.getValueTag().equals(Tag.NameWithLanguage) && getTag().equals(Tag.NameWithoutLanguage)) ||
                (attribute.getValueTag().equals(Tag.TextWithLanguage) && getTag().equals(Tag.TextWithoutLanguage))) {
            return Optional.absent();
        }
        // Apply conversion from StringType to a LangStringType attribute
        return Optional.of(of(Lists.transform((List<LangString>) attribute.getValues(), LangString.ToStringFunc)));
    }
}
