package com.hp.jipp.encoding

import com.google.common.base.Optional

/** A string, possibly encoded with language  */
data class LangString(val string: String, val lang: Optional<String>) {
    constructor(string: String) : this(string, Optional.absent<String>())
    constructor(string: String, lang: String) : this(string, Optional.of<String>(lang))

    override fun toString() = "\"$string\" of " + if (lang.isPresent) lang.get() else "?"
}
