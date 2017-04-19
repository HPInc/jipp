package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CollectionType extends AttributeType<AttributeCollection> {

    /** Used to terminate a collection */
    private static final Attribute EndCollectionAttribute = new OctetStringType(Tag.EndCollection, "").of();

    static final Encoder<AttributeCollection> ENCODER = new Encoder<AttributeCollection>() {

        @Override
        public void writeValue(DataOutputStream out, AttributeCollection value)
                throws IOException {
            out.writeShort(0); // Empty value

            for (Attribute<?> attribute : value.getAttributes()) {
                // Write a MemberAttributeName attribute
                Tag.MemberAttributeName.write(out);
                out.writeShort(0);
                writeValueBytes(out, attribute.getName().getBytes(Util.UTF8));

                // Write the attribute, but without its name
                attribute.withName("").write(out);
            }

            // Terminating attribute
            EndCollectionAttribute.write(out);
        }

        @Override
        public AttributeCollection readValue(DataInputStream in, Tag valueTag)
                throws IOException {
            skipValueBytes(in);
            ImmutableList.Builder<Attribute<?>> builder = new ImmutableList.Builder<>();

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
                    String memberName = new String(readValueBytes(in), Util.UTF8);
                    Attribute memberValue = Attribute.read(in, Tag.read(in));
                    builder.add(memberValue.withName(memberName));
                } else {
                    throw new ParseError("Bad tag: " + tag);
                }
            }
            return new AttributeCollection(builder.build());
        }

        @Override
        boolean valid(Tag valueTag) {
            return valueTag == Tag.BeginCollection;
        }
    };

    public CollectionType(String name) {
        super(ENCODER, Tag.BeginCollection, name);
    }
}
