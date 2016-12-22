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
        super(Tags.BeginCollection, name, values);
    }

    @SafeVarargs
    public CollectionAttribute(String name, Map<String, Attribute>... value) {
        super(Tags.BeginCollection, name, new ArrayList<Map<String, Attribute>>());
        values.addAll(Arrays.asList(value));
    }

    private static final Attribute EndCollectionAttribute =
            new StringAttribute(Tags.EndCollection, "", "");

    @Override
    void writeValue(DataOutputStream out, Map<String, Attribute> value) throws IOException {
        out.writeShort(0); // Empty value
        // One pair of attributes for each item in map
        for(Map.Entry<String, Attribute> entry : value.entrySet()) {
            new StringAttribute(Tags.MemberAttributeName, "", entry.getKey()).write(out);
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
            byte tag = in.readByte();
            if (tag == Tags.EndCollection) {
                // Skip the rest of this attr and return.
                nameLength = in.readShort();
                in.skip(nameLength);
                valueLength = in.readShort();
                in.skip(valueLength);
                break;
            } else if (tag == Tags.MemberAttributeName) {
                nameLength = in.readShort();
                in.skip(nameLength);
                valueLength = in.readShort();
                byte bytes[] = new byte[valueLength];
                in.read(bytes);
                String memberName = new String(bytes);
                Attribute memberValue = AttributeGroup.readAttribute(in, in.readByte());
                members.put(memberName, memberValue);
            } else {
                throw new IOException("Unexpected tag in collection: " + Tags.toString(tag));
            }
        }
        return members;
    }

    public static CollectionAttribute read(DataInputStream in, int valueTag) throws IOException {
        if (valueTag != Tags.BeginCollection) {
            throw new IOException("Attempt to read collection from " + Tags.toString(valueTag));
        }
        CollectionAttribute attribute = new CollectionAttribute(
                readName(in),
                new ArrayList<Map<String, Attribute>>());
        readValues(in, attribute);
        return attribute;
    }

    public static boolean hasTag(byte tag) {
        return tag == Tags.BeginCollection;
    }
}