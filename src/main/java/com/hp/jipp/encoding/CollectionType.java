package com.hp.jipp.encoding;

import com.google.common.collect.ImmutableList;
import com.hp.jipp.util.ParseError;
import com.hp.jipp.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A type for attribute collections.
 *
 * @see <a href="https://tools.ietf.org/html/rfc3382">RFC3382</a>
 */
public class CollectionType extends AttributeType<AttributeCollection> {
    private static final String TYPE_NAME = "Collection";

    /** Used to terminate a collection */
    private static final Attribute<byte[]> EndCollectionAttribute = new OctetStringType(Tag.EndCollection, "").of();

    static final Attribute.BaseEncoder<AttributeCollection>
            ENCODER = new Attribute.BaseEncoder<AttributeCollection>() {

        @Override
        public String getType() {
            return TYPE_NAME;
        }

        @Override
        public void writeValue(DataOutputStream out, AttributeCollection value)
                throws IOException {
            out.writeShort(0); // Empty value

            for (Attribute<?> attribute : value.getAttributes()) {
                // Write a MemberAttributeName attribute
                Tag.MemberAttributeName.write(out);
                out.writeShort(0);
                Attribute.writeValueBytes(out, attribute.getName().getBytes(Util.UTF8));

                // Write the attribute, but without its name
                attribute.withName("").write(out);
            }

            // Terminating attribute
            EndCollectionAttribute.write(out);
        }

        @Override
        public AttributeCollection readValue(DataInputStream in, Attribute.EncoderFinder finder, Tag valueTag)
                throws IOException {
            Attribute.skipValueBytes(in);
            ImmutableList.Builder<Attribute<?>> builder = new ImmutableList.Builder<>();

            // Read attribute pairs until EndCollection is reached.
            while (true) {
                Tag tag = Tag.read(in);
                if (tag == Tag.EndCollection) {
                    // Skip the rest of this attr and return.
                    Attribute.skipValueBytes(in);
                    Attribute.skipValueBytes(in);
                    break;
                } else if (tag == Tag.MemberAttributeName) {
                    Attribute.skipValueBytes(in);
                    String memberName = new String(Attribute.Companion.readValueBytes(in), Util.UTF8);
                    Tag memberTag = Tag.read(in);

                    // Read and throw away the blank attribute name
                    Attribute.readValueBytes(in);
                    builder.add(finder.find(memberTag, memberName).read(in, finder, memberTag, memberName));

                } else {
                    throw new ParseError("Bad tag in collection: " + tag);
                }
            }
            return new AttributeCollection(builder.build());
        }

        @Override
        public boolean valid(Tag valueTag) {
            return valueTag == Tag.BeginCollection;
        }
    };

    public CollectionType(String name) {
        super(ENCODER, Tag.BeginCollection, name);
    }
}
