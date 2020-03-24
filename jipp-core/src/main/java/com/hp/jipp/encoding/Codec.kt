// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding

/** Reads/writes values of [T]. */
interface Codec<T> {
    val cls: Class<T>

    /** Return true if this codec handles the specified tag. */
    fun handlesTag(tag: Tag): Boolean

    /** Read a value from the input stream */
    fun readValue(input: IppInputStream, startTag: Tag): T

    /** Write value (assuming it is an instance of [T]). */
    fun writeValue(output: IppOutputStream, value: Any)

    /** The tag to use for a particular value. */
    fun tagOf(value: Any): Tag

    companion object {
        /** Construct a codec handling [TaggedValue] values, covering any number of [Tag] input values. */
        inline operator fun <reified T : TaggedValue> invoke(
            crossinline handlesTagFunc: (Tag) -> Boolean,
            crossinline readAttrFunc: IppInputStream.(startTag: Tag) -> T,
            crossinline writeAttrFunc: IppOutputStream.(value: T) -> Unit
        ) =
            object : Codec<T> {
                override val cls: Class<T> = T::class.java
                override fun tagOf(value: Any) = (value as T).tag
                override fun handlesTag(tag: Tag) = handlesTagFunc(tag)
                override fun readValue(input: IppInputStream, startTag: Tag): T =
                    input.readAttrFunc(startTag)
                override fun writeValue(output: IppOutputStream, value: Any) {
                    output.writeAttrFunc(value as T)
                }
            }

        /** Construct a codec handling values encoded by a particular [Tag]. */
        inline operator fun <reified T> invoke(
            valueTag: Tag,
            crossinline readAttrFunc: IppInputStream.(startTag: Tag) -> T,
            crossinline writeAttrFunc: IppOutputStream.(value: T) -> Unit
        ) =
            object : Codec<T> {
                override val cls: Class<T> = T::class.java
                override fun tagOf(value: Any) = valueTag
                override fun handlesTag(tag: Tag) = valueTag == tag
                override fun readValue(input: IppInputStream, startTag: Tag): T =
                    input.readAttrFunc(startTag)
                override fun writeValue(output: IppOutputStream, value: Any) {
                    output.writeAttrFunc(value as T)
                }
            }
    }
}
