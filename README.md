# JIPP: A Java implementation of IPP

This project contains the core IPP parser/builder for [IPP packets](https://en.wikipedia.org/wiki/Internet_Printing_Protocol).

Features:
* Supports construction of IPP servers, clients, routers, gateways, etc.
* Common operations and attributes are defined and ready to use.
* Can be extended to support new operations and attributes.
* Can be used over any transport (typically HTTP)
* Includes a pretty-printer for human-readable IPP packet display

What could I do with this?
* Scan and show available printers on your network to your users.
* Implement an Android Print Service
* Test IPP clients or IPP printers in interesting ways.
* Experiment with alternative IPP transports.
* Implement a cloud-based print server or client.

This library supplies a Java-compatible API but is actually implemented in [Kotlin](https://kotlinlang.org/).

## Usage

1. Add the current version of JIPP to your project
2. Create an `IppClientTransport` or `IppServerTransport`
3. Use the transport to create, parse and exchange `IppPacket` objects

## Dependencies

`jipp-core`'s only dependencies are JDK 6+ and the current Kotlin runtime.

## Building

`./gradlew build`

A full build of this project requires `python` (2.x) and `dot` to generate dependency graphs

## Related projects

* [`javax.print` (JSR6)](https://docs.oracle.com/javase/7/docs/api/javax/print/package-summary.html) - Standard Java printing APIs. IPP 1.1 only, client-side only. API is not supported by Android.
* [Cups4J](http://www.cups4j.org/) - GPL licensed, with a [port to Android](https://github.com/BenoitDuffez/AndroidCupsPrint).
* [JSPI](https://github.com/bhagyas/jspi)
