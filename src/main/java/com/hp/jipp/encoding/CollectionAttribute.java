package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/** An attribute encoding a collection of named attributes as per RFC3382 */
public class CollectionAttribute {

    /** Used to terminate a collection */
    private static final Attribute EndCollectionAttribute =
            StringAttribute.create(Tag.EndCollection, "", "");

    /** Return a new collection attribute builder */
    public static Attribute.Builder<Map<String, Attribute<?>>> builder() {
        return Attribute.builder(ENCODER, Tag.BeginCollection).setName("");
    }

    /** Return a new collection attribute */
    @SafeVarargs
    public static Attribute<Map<String, Attribute<?>>> create(String name,
            Map<String, Attribute<?>>... values) {
        return builder().setValues(values).setName(name).build();
    }

    /** Return a new inner (nameless) collection attribute */
    @SafeVarargs
    public static Attribute<Map<String, Attribute<?>>> create(Map<String, Attribute<?>>... values) {
        return builder().setValues(values).build();
    }

    static Attribute.Encoder<Map<String, Attribute<?>>> ENCODER = new
            Attribute.Encoder<Map<String, Attribute<?>>>() {
        @Override
        public void writeValue(DataOutputStream out, Map<String, Attribute<?>> value)
                throws IOException {
            out.writeShort(0); // Empty value

            // Write name/value for each item in map
            for(Map.Entry<String, Attribute<?>> entry : value.entrySet()) {
                // Write a MemberAttributeName attribute
                Tag.MemberAttributeName.write(out);
                out.writeShort(0);
                writeValueBytes(out, entry.getKey().getBytes());

                // Write the child attribute itself
                entry.getValue().write(out);
            }
            // Terminating attribute
            EndCollectionAttribute.write(out);
        }

        @Override
        public Map<String, Attribute<?>> readValue(DataInputStream in, Tag valueTag)
                throws IOException {
            ImmutableMap.Builder<String, Attribute<?>> members = new ImmutableMap.Builder<>();

            // First collection value will be empty so skip it
            skipValueBytes(in);

            // Read attribute pairs until EndCollection is reached.
            while(true) {
                Tag tag = Tag.read(in);
                if (tag == Tag.EndCollection) {
                    // Skip the rest of this attr and return.
                    skipValueBytes(in);
                    skipValueBytes(in);
                    break;
                } else if (tag == Tag.MemberAttributeName) {
                    skipValueBytes(in);
                    String memberName = new String(readValueBytes(in));
                    Attribute memberValue = AttributeGroup.readAttribute(in, Tag.read(in));
                    members.put(memberName, memberValue);
                } else {
                    throw new IOException("Bad tag: " + tag);
                }
            }
            return members.build();
        }

        @Override
        public Attribute.Builder<Map<String, Attribute<?>>> builder(Tag valueTag) {
            return CollectionAttribute.builder();
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.BeginCollection;
        }
    };
}