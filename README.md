[![CircleCI](https://circleci.com/gh/HPInc/jipp-core.svg?style=svg&circle-token=4baa4b142e5cc6f6cf6e803a8c5832a9dd755a25)](https://circleci.com/gh/HPInc/jipp-core)
[![CodeCov](https://codecov.io/github/HPInc/jipp-core/coverage.svg?branch=master)](https://codecov.io/github/HPInc/jipp-core)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core)
[![Dokka](https://img.shields.io/badge/docs-dokka-brightgreen.svg)](https://hpinc.github.io/jipp-core/javadoc/index.html)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

# JIPP: A Java-compatible IPP library

This project contains:

* `jipp-core`, the core IPP parser/builder for [IPP packets](https://en.wikipedia.org/wiki/Internet_Printing_Protocol).
* `jprint`, a sample app showing how jipp-core can be used to send a document to a printer.

`jipp-core` features:
* Supports construction of IPP servers, clients, routers, gateways, etc.
* Common operations and attributes are defined and ready to use.
* Can be extended to support new operations and attributes.
* Can be used over any transport (typically HTTP)
* Includes a pretty-printer for human-readable IPP packet display
* Kotlin users can access a type-safe packet building [DSL](https://kotlinlang.org/docs/reference/type-safe-builders.html)

What could I do with this?
* Scan and show available printers on your network to your users.
* Implement an Android Print Service
* Test IPP clients or IPP printers in interesting ways.
* Experiment with alternative IPP transports.
* Implement a cloud-based print server or client.

The API is fully Java-compatible but is actually implemented in [Kotlin](https://kotlinlang.org/).
[https://hpinc.github.io/jipp-core/javadoc/index.html](JavaDoc) is available.

## Usage

In short:

1. Add the current version of JIPP to your project
2. Create an `IppClientTransport` or `IppServerTransport`
3. Use the transport to create, parse and exchange `IppPacket` objects

A very basic use case is demonstrated by the `jclient` sample app. To run it:

```
# build the app
./gradlew jprint:build

# unzip in the current directory
unzip -o ./sample/jprint/build/distributions/jprint-*.zip

# Use IPP to print a file to the supplied HTTP/IPP endpoint.
# (The printer must natively support the file type.)
jprint-*/bin/jprint "ipp://192.168.1.102:631/ipp/print" sample.pdf
```

## API Maturity

As an 0.5 project, APIs may still be changed in non-backwards-compatible ways.

## Dependencies

`jipp-core`'s only dependencies are JDK 6+ and the current Kotlin runtime.

## Building

`./gradlew build`

A full build of this project requires `python` (2.x) and `dot` to generate dependency graphs

## Related projects

* [`javax.print` (JSR6)](https://docs.oracle.com/javase/7/docs/api/javax/print/package-summary.html) - Standard Java printing APIs. IPP 1.1 only, client-side only. API is not supported by Android.
* [Cups4J](http://www.cups4j.org/) - GPL licensed, with a [port to Android](https://github.com/BenoitDuffez/AndroidCupsPrint).
* [JSPI](https://github.com/bhagyas/jspi)
