package com.hp.jipp.encoding;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KeyValueType extends AttributeType<Map<String, String>> {

    /** An encoder for Key/Value types */
    public static final Attribute.SimpleEncoder<Map<String, String>> ENCODER =
            new Attribute.SimpleEncoder<Map<String, String>>("KeyValue") {
                static final String ELEMENT_SEPARATOR = ";";
            static final String PART_SEPARATOR = "=";

            @Override
            public Map<String, String> readValue(DataInputStream in, Tag valueTag) throws IOException {
                String input = StringType.ENCODER.readValue(in, valueTag);
                return decode(input);
            }

            @Override
            public void writeValue(DataOutputStream out, Map<String, String> value) throws IOException {
                StringType.ENCODER.writeValue(out, encode(value));
            }

            @Override
            public boolean valid(Tag valueTag) {
                return valueTag == Tag.OctetString;
            }

            private Map<String, String> decode(String input) {
                Map<String, String> map = new LinkedHashMap<>();
                for (String element : Splitter.on(ELEMENT_SEPARATOR).split(input)) {
                    List<String> parts = ImmutableList.copyOf(Splitter.on(PART_SEPARATOR).split(element));
                    if (parts.size() == 2 && !parts.get(0).isEmpty() && !parts.get(1).isEmpty()) {
                        map.put(parts.get(0), parts.get(1));
                    }
                }
                return ImmutableMap.copyOf(map);
            }

            private String encode(Map<String, String> input) {
                StringBuilder out = new StringBuilder();
                for (Map.Entry<String, String> entry : input.entrySet()) {
                    out.append(entry.getKey());
                    out.append(PART_SEPARATOR);
                    out.append(entry.getValue());
                    out.append(ELEMENT_SEPARATOR);
                }
                return out.toString();
            }
        };

    public KeyValueType(String name) {
        super(ENCODER, Tag.OctetString, name);
    }
}
