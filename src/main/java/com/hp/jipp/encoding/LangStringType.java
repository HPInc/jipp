package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

public class LangStringType extends AttributeType<LangString> {

    public static Encoder<LangString> ENCODER = new Encoder<LangString>() {

        @Override
        LangString readValue(DataInputStream in, Tag valueTag) throws IOException {
            String lang = new String(readValueBytes(in));
            String string = new String(readValueBytes(in));
            return LangString.of(string, lang);
        }

        @Override
        void writeValue(DataOutputStream out, LangString value) throws IOException {
            writeValueBytes(out, value.getLang().get().getBytes());
            writeValueBytes(out, value.getString().getBytes());
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.NameWithLanguage || valueTag == Tag.TextWithLanguage;
        }
    };

    public LangStringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }

    public Optional<Attribute<LangString>> adopt(Attribute<?> attribute) {
        if (!((attribute.getValueTag().equals(Tag.NameWithoutLanguage) && getTag().equals(Tag.NameWithLanguage)) ||
                (attribute.getValueTag().equals(Tag.TextWithoutLanguage) && getTag().equals(Tag.TextWithLanguage)))) {
            return Optional.absent();
        }
        // Apply conversion from LangStringType to a StringType on demand
        return Optional.of(of(Collections2.transform((Collection<String>) attribute.getValues(),
                LangString.FromStringFunc)));
    }
}
