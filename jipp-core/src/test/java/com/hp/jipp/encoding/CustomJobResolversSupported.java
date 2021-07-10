package com.hp.jipp.encoding;

import com.hp.jipp.model.JobResolversSupported;
import com.hp.jipp.model.MediaCol;
import com.hp.jipp.model.Types;
import com.hp.jipp.util.PrettyPrinter;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CustomJobResolversSupported implements AttributeCollection {
    @NotNull
    final String resolverName;
    @Nullable
    final MediaCol mediaCol;
    @Nullable
    final String sides;

    public CustomJobResolversSupported(
            @NotNull String resolverName,
            @Nullable MediaCol mediaCol,
            @Nullable String sides

    ) {
        this.resolverName = resolverName;
        this.mediaCol = mediaCol;
        this.sides = sides;
    }

    @NotNull
    @Override
    public List<Attribute<?>> getAttributes() {
        List<Attribute<?>> attributes = new ArrayList<>();
        attributes.add(JobResolversSupported.resolverName.of(resolverName));
        if (mediaCol != null) {
            attributes.add(Types.mediaCol.of(mediaCol));
        }
        if (sides != null) {
            attributes.add(Types.sides.of(sides));
        }
        // etc...
        return attributes;
    }

    @Override
    public void print(@NotNull PrettyPrinter printer) {
        printer.open(PrettyPrinter.OBJECT);
        printer.addAll(getAttributes());
        printer.close();
    }

    static Converter<CustomJobResolversSupported> converter = new AttributeCollectionConverterBase<CustomJobResolversSupported>() {
        @NotNull
        @Override
        public Class<CustomJobResolversSupported> getCls() {
            return CustomJobResolversSupported.class;
        }

        @NotNull
        @Override
        public CustomJobResolversSupported convert(@NotNull List<? extends Attribute<?>> attributes) {
            Name resolverName = extractOne(attributes, JobResolversSupported.resolverName);
            return new CustomJobResolversSupported(
                    resolverName == null ? "" : resolverName.asString(),
                    extractOne(attributes, Types.mediaCol),
                    extractOne(attributes, Types.sides)
                    // etc...
            );
        }
    };
}
