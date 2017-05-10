package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class KeywordType<T extends Keyword> extends AttributeType<T> {

    /** An encoder for Keyword types */
    public static class Encoder<T extends Keyword> extends Attribute.Encoder<T> {

        @SuppressWarnings("unchecked")
        public static <T extends Keyword> Encoder<T> of(Class<T> cls, Keyword.Factory<T> factory) {
            ImmutableList.Builder<T> all = new ImmutableList.Builder<>();
            for (Object object : Util.getStaticObjects(cls)) {
                if (cls.isAssignableFrom(object.getClass())) {
                    //noinspection ResultOfMethodCallIgnored
                    all.add((T) object);
                }
            }
            return new Encoder(factory, all.build(), cls.getSimpleName());
        }

        private final Map<String, T> map;
        private final Keyword.Factory<T> factory;

        public Encoder(Keyword.Factory<T> factory, Collection<T> all, String name) {
            super(name);
            ImmutableMap.Builder<String, T> builder = new ImmutableMap.Builder<>();
            for (T keyword : all) {
                //noinspection ResultOfMethodCallIgnored
                builder.put(keyword.getName(), keyword);
            }
            map = builder.build();
            this.factory = factory;
        }

        @Override
        public T readValue(DataInputStream in, Tag valueTag) throws IOException {
            String value = StringType.ENCODER.readValue(in, valueTag);
            if (map.containsKey(value)) {
                return map.get(value);
            } else {
                return factory.of(value);
            }
        }

        @Override
        public void writeValue(DataOutputStream out, T value) throws IOException {
            StringType.ENCODER.writeValue(out, value.getName());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.Keyword;
        }

        public Collection<T> getAll() {
            return map.values();
        }
    }

    public KeywordType(Encoder<T> encoder, String name) {
        super(encoder, Tag.Keyword, name);
    }
}
