package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.hp.jipp.util.BuildError;
import com.hp.jipp.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/** An language-encoded string attribute type */
public class LangStringType extends AttributeType<LangString> {
    private static final String TYPE_NAME = "LangString";

    static final Attribute.Encoder<LangString> ENCODER = new Attribute.Encoder<LangString>(TYPE_NAME) {
        @Override
        public LangString readValue(DataInputStream in, Tag valueTag) throws IOException {
            byte[] bytes = OctetStringType.ENCODER.readValue(in, valueTag);
            DataInputStream inBytes = new DataInputStream(new ByteArrayInputStream(bytes));

            String lang = new String(Attribute.readValueBytes(inBytes), Util.UTF8);
            String string = new String(Attribute.readValueBytes(inBytes), Util.UTF8);
            return LangString.of(string, lang);
        }

        @Override
        public void writeValue(DataOutputStream out, LangString value) throws IOException {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(bytesOut);
            Optional<String> lang = value.getLang();
            if (!lang.isPresent()) {
                throw new BuildError("Cannot write a LangString without a language");
            }
            writeValueBytes(dataOut, lang.get().getBytes(Util.UTF8));
            writeValueBytes(dataOut, value.getString().getBytes(Util.UTF8));
            OctetStringType.ENCODER.writeValue(out, bytesOut.toByteArray());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.NameWithLanguage || valueTag == Tag.TextWithLanguage;
        }
    };

    public LangStringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }

    @Override
    @SuppressWarnings({"PMD.UselessParentheses", "unchecked"})
    public Optional<Attribute<LangString>> of(Attribute<?> attribute) {
        if (!(attribute.getValueTag().equals(Tag.NameWithoutLanguage) && getTag().equals(Tag.NameWithLanguage)) ||
                (attribute.getValueTag().equals(Tag.TextWithoutLanguage) && getTag().equals(Tag.TextWithLanguage))) {
            return Optional.absent();
        }
        // TODO: If we don't know the language this is actually a dangerous thing to do
        // Apply conversion from StringType to a LangStringType on demand
        return Optional.of(of(Lists.transform((List<String>) attribute.getValues(), LangString.FromStringFunc)));
    }
}
