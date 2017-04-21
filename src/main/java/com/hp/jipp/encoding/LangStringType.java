package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/** An language-encoded string attribute type */
public class LangStringType extends AttributeType<LangString> {

    static final Attribute.Encoder<LangString> ENCODER = new Attribute.Encoder<LangString>() {
        @Override
        LangString readValue(DataInputStream in, Tag valueTag) throws IOException {
            String lang = new String(readValueBytes(in), Util.UTF8);
            String string = new String(readValueBytes(in), Util.UTF8);
            return LangString.of(string, lang);
        }

        @Override
        void writeValue(DataOutputStream out, LangString value) throws IOException {
            if (!value.getLang().isPresent()) {
                throw new BuildError("Cannot write a LangString without a language");
            }
            writeValueBytes(out, value.getLang().get().getBytes(Util.UTF8));
            writeValueBytes(out, value.getString().getBytes(Util.UTF8));
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.NameWithLanguage || valueTag == Tag.TextWithLanguage;
        }
    };

    public LangStringType(Tag tag, String name) {
        super(ENCODER, tag, name);
    }

    @Override
    @SuppressWarnings({"PMD.UselessParentheses", "unchecked"})
    public Optional<Attribute<LangString>> from(Attribute<?> attribute) {
        if (!(attribute.getValueTag().equals(Tag.NameWithoutLanguage) && getTag().equals(Tag.NameWithLanguage)) ||
                (attribute.getValueTag().equals(Tag.TextWithoutLanguage) && getTag().equals(Tag.TextWithLanguage))) {
            return Optional.absent();
        }
        // Apply conversion from LangStringType to a StringType on demand
        return Optional.of(of(Lists.transform((List<String>) attribute.getValues(), LangString.FromStringFunc)));
    }
}
