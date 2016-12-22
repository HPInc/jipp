package com.hp.jipp.encoding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An attribute encoding a collection of named attributes as per RFC3382 */
public class CollectionAttribute extends Attribute<Map<String, Attribute>> {

    public CollectionAttribute(String name, List<Map<String, Attribute>> values) {
        super(Tag.BeginCollection, name, values);
    }

    @SafeVarargs
    public CollectionAttribute(String name, Map<String, Attribute>... value) {
        super(Tag.BeginCollection, name, new ArrayList<Map<String, Attribute>>());
        values.addAll(Arrays.asList(value));
    }

    private static final Attribute EndCollectionAttribute =
            new StringAttribute(Tag.EndCollection, "", "");

    @Override
    void writeValue(DataOutputStream out, Map<String, Attribute> value) throws IOException {
        out.writeShort(0); // Empty value
        // One pair of attributes for each item in map
        for(Map.Entry<String, Attribute> entry : value.entrySet()) {
            new StringAttribute(Tag.MemberAttributeName, "", entry.getKey()).write(out);
            entry.getValue().write(out);
        }
        // Terminating attribute
        EndCollectionAttribute.write(out);
    }

    @Override
    Map<String, Attribute> readValue(DataInputStream in) throws IOException {
        // We don't care about value so skip it to get to the next tag
        int nameLength;
        int valueLength = in.readShort();
        in.skip(valueLength);
        Map<String, Attribute> members = new HashMap<>();

        // Read attribute pairs until EndCollection is reached.
        while(true) {
            Tag tag = Tag.read(in);
            if (tag == Tag.EndCollection) {
                // Skip the rest of this attr and return.
                nameLength = in.readShort();
                in.skip(nameLength);
                valueLength = in.readShort();
                in.skip(valueLength);
                break;
            } else if (tag == Tag.MemberAttributeName) {
                nameLength = in.readShort();
                in.skip(nameLength);
                valueLength = in.readShort();
                byte bytes[] = new byte[valueLength];
                in.read(bytes);
                String memberName = new String(bytes);
                Attribute memberValue = AttributeGroup.readAttribute(in, Tag.read(in));
                members.put(memberName, memberValue);
            } else {
                throw new IOException("Bad tag: " + tag);
            }
        }
        return members;
    }

    public static CollectionAttribute read(DataInputStream in, Tag valueTag) throws IOException {
        if (valueTag != Tag.BeginCollection) {
            return null;
        }
        CollectionAttribute attribute = new CollectionAttribute(readName(in),
                new ArrayList<Map<String, Attribute>>());
        readValues(in, attribute);
        return attribute;
    }
}