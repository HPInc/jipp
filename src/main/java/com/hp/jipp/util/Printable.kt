package com.hp.jipp.util

/** An object that knows how to pretty-print itself  */
interface PrettyPrintable {
    /** Add a representation of self to the printer  */
    fun print(printer: PrettyPrinter)
}
