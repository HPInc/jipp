package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/** Tools for encoding collections of named attributes as per RFC3382 */
public class CollectionEncoder extends AttributeEncoder<Map<String, Attribute<?>>> {

    private static final CollectionEncoder INSTANCE = new CollectionEncoder();
    public static CollectionEncoder getInstance() {
        return INSTANCE;
    }

    /** Used to terminate a collection */
    private static final Attribute EndCollectionAttribute =
            StringEncoder.getInstance().builder(Tag.EndCollection).setName("").setValues("")
                    .build();

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
    boolean valid(Tag valueTag) {
        return valueTag == Tag.BeginCollection;
    }
}