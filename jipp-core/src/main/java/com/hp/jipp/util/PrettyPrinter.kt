// Copyright 2017 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util

import java.util.ArrayList
import java.util.Stack

/**
 * Builds pretty-printed output from structured data
 *
 * @param prefix prefix to appear before items
 * @param style display for items added
 * @param indent a single level of indent
 * @param maxWidth maximum width of a line before forcing grouped items each onto their own line
 */
class PrettyPrinter internal constructor(
    prefix: String,
    style: Style,
    private val indent: String,
    private val maxWidth: Int
) {
    private val mGroups = Stack<Group>()

    init {
        // Push a root group
        mGroups.push(Group(SILENT, "", maxWidth))
        // Push the user's initial group (closed by print())
        mGroups.push(Group(style, prefix, maxWidth))
    }

    /**
     * Open a new pretty-printed group of the specified style and prefix. Any items added will be appended to this
     * group, until [close] is called.
     */
    @JvmOverloads fun open(style: Style, prefix: String = ""): PrettyPrinter {
        mGroups.push(Group(style, prefix, maxWidth))
        return this
    }

    /**
     * Close the current group, falling back to the parent group. Any new items added will appear
     * in the previously opened group.
     */
    fun close(): PrettyPrinter {
        checkUnprinted()
        if (mGroups.size < GROUP_SIZE_MIN_OPEN) {
            throw IllegalArgumentException("nothing open to close")
        }
        innerClose()
        return this
    }

    private fun innerClose() {
        val closed = mGroups.pop()
        val startPos = mGroups.size * (indent.length - 1)
        if (startPos + closed.width() < maxWidth) {
            innerAdd(closed.compressed())
        } else {
            innerAdd(closed.expanded(startPos, String(CharArray(mGroups.size))
                    .replace("\u0000", indent)))
        }
    }

    private fun checkUnprinted() {
        if (mGroups.size < GROUP_SIZE_MIN_CLOSED) {
            throw IllegalArgumentException("print already called")
        }
    }

    /**
     * Add items to the current group
     */
    fun addAll(items: Collection<Any>): PrettyPrinter {
        checkUnprinted()
        for (item in items) {
            innerAdd(item)
        }
        return this
    }

    /**
     * Add items to the current group
     */
    fun add(vararg items: Any): PrettyPrinter {
        checkUnprinted()
        for (item in items) {
            innerAdd(item)
        }
        return this
    }

    private fun innerAdd(item: Any) {
        val group = mGroups.peek()
        if (item is PrettyPrintable) {
            item.print(this)
        } else {
            group.items.add(item)
        }
    }

    /**
     * Closes all open groups and builds a result. After making this call, no more items or groups can be added
     */
    fun print(): String {
        while (mGroups.size > 1) innerClose()
        return mGroups.peek().items[0].toString()
    }

    companion object {
        private const val NEWLINE = "\n"
        private const val GROUP_SIZE_MIN_CLOSED = 2
        private const val GROUP_SIZE_MIN_OPEN = 3
        private const val SHORT_DIVISOR = 3

        /** A style for arrays, e.g. "Me [ A, B, C ]"  */
        @JvmField val ARRAY = Style("[", "]", ",", " ")

        /** A style for objects, e.g. "Me { A, B, C }"  */
        @JvmField val OBJECT = Style("{", "}", ",", " ")

        /** A style for key/value pairs e.g. "Me A/B/C". Works best when there is only one value.  */
        @JvmField val KEY_VALUE = Style(" ", "", "/", "")

        /** A style with no indenting or separators at all */
        @JvmField val SILENT = Style("", "", ",", "")

        /** Style used for delimiting members of a pretty-printed group  */
        class Style internal constructor(
            val opener: String,
            val closer: String,
            val separator: String,
            val spacer: String
        )

        /** A group of objects currently being pretty-printed  */
        internal class Group internal constructor(
            private val style: Style,
            private val prefix: String,
            private val maxWidth: Int
        ) {

            val items = ArrayList<Any>()

            internal fun width(): Int {
                var result = 0
                if (!prefix.isEmpty()) {
                    result += prefix.length + style.spacer.length
                }
                result += style.opener.length + style.spacer.length + items.sumBy { it.toString().length }
                result += (items.size - 1) * style.separator.length + style.spacer.length
                result += style.spacer.length + style.closer.length
                return result
            }

            internal fun compressed(): String {
                val out = StringBuilder()
                if (!prefix.isEmpty()) {
                    out.append(prefix)
                    out.append(style.spacer)
                }
                out.append(style.opener)
                out.append(style.spacer)
                out.append(items.joinToString(separator = style.separator + style.spacer))
                out.append(style.spacer)
                out.append(style.closer)
                return out.toString()
            }

            internal fun expanded(startPos: Int, indent: String): String {
                val out = StringBuilder()
                if (!prefix.isEmpty()) {
                    out.append(prefix)
                    out.append(style.spacer)
                }
                out.append(style.opener)

                // If all contained items are relatively short, use a semi-expanded form
                val compact = items.none { it.toString().length > maxWidth / SHORT_DIVISOR }

                if (compact) {
                    out.append(style.spacer)
                    wrap(out, startPos, indent)
                } else {
                    out.append(NEWLINE)
                    out.append(indent)
                    out.append(items.joinToString(separator = style.separator + NEWLINE + indent))
                }
                out.append(style.spacer)
                out.append(style.closer)
                return out.toString()
            }

            private fun wrap(out: StringBuilder, startPos: Int, indent: String) {
                var curWidth = startPos + out.length

                if (items.isNotEmpty()) {
                    val itemString = items.first().toString()
                    out.append(itemString)
                    curWidth += itemString.length
                }

                items.drop(1).forEach {
                    val itemString = it.toString()
                    val expectedWidth = style.separator.length + style.spacer.length + itemString.length
                    if (curWidth + expectedWidth > maxWidth) {
                        out.append(style.separator)
                        out.append(NEWLINE)
                        out.append(indent)
                        out.append(itemString)
                        curWidth = indent.length + itemString.length
                    } else {
                        out.append(style.separator)
                        out.append(style.spacer)
                        out.append(itemString)
                        curWidth += style.separator.length
                        curWidth += style.spacer.length
                        curWidth += itemString.length
                    }
                }
            }
        }
    }
}
