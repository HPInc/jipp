package com.hp.jipp.model

import com.hp.jipp.encoding.Keyword
import com.hp.jipp.encoding.KeywordType

class IdentifyAction(override val name: String) : Keyword() {

    companion object {
        @JvmField val Display = IdentifyAction("display")
        @JvmField val Flash = IdentifyAction("flash")
        @JvmField val Sound = IdentifyAction("sound")
        @JvmField val Speak = IdentifyAction("speak")

        val ENCODER: KeywordType.Encoder<IdentifyAction> = KeywordType.Encoder.of(
                IdentifyAction::class.java, object : Keyword.Factory<IdentifyAction> {
            override fun of(name: String): IdentifyAction {
                return IdentifyAction(name)
            }
        })

        @JvmStatic fun typeOf(name: String): KeywordType<IdentifyAction> {
            return KeywordType(ENCODER, name)
        }
    }
}
