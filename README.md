[![CircleCI](https://circleci.com/gh/HPInc/jipp.svg?style=svg&circle-token=4baa4b142e5cc6f6cf6e803a8c5832a9dd755a25)](https://circleci.com/gh/HPInc/jipp)
[![CodeCov](https://codecov.io/github/HPInc/jipp/coverage.svg?branch=master)](https://codecov.io/github/HPInc/jipp)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core)
[![Dokka](https://img.shields.io/badge/docs-dokka-brightgreen.svg)](https://hpinc.github.io/jipp/javadoc/index.html)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.3.20-blue.svg)](https://kotlinlang.org/)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

# JIPP: A Java-compatible IPP library

This project contains:

* `jipp-core`, the core IPP parser/builder for [IPP packets](https://en.wikipedia.org/wiki/Internet_Printing_Protocol).
* `jipp-pdls`, which converts raster format docs to common page description languages (PCLm and PWG-Raster).
* `jprint`, a sample app showing how `jipp-core` can be used to send a document to a printer.

`jipp-core` features:
* Supports construction of IPP servers, clients, routers, gateways, etc.
* Common operations and attributes are defined and ready to use.
* Can be extended to support new operations and attributes.
* Can be used over any transport (typically HTTP).
* Includes a pretty-printer for human-readable IPP packet display.
* Kotlin users can access a type-safe packet building [DSL](https://kotlinlang.org/docs/reference/type-safe-builders.html)

What could I do with this?
* Scan and show available printers on your network to your users.
* Implement an Android Print Service.
* Test IPP clients or IPP printers in interesting ways.
* Experiment with alternative IPP transports.
* Implement a cloud-based print server or client.

The API is fully Java-compatible but is actually implemented in Kotlin.
[JavaDoc](https://hpinc.github.io/jipp/javadoc/index.html) is available for the Java-facing API.

## Usage

In short:

1. Add the current version of JIPP to your project
```
dependencies {
    compile 'com.hp.jipp:jipp-core:0.6.12'
    compile 'com.hp.jipp:jipp-pdl:0.6.12' // Only needed if transforming PDLs
}
```
2. Create an `IppClientTransport` or `IppServerTransport` (see example
[`HttpIppClientTransport.java`](https://github.com/HPInc/jipp/blob/master/sample/jprint/src/main/java/sample/HttpIppClientTransport.java))
3. Use the transport to send and receive `IppPacket` objects, e.g.:
```
URI uri = URI.create("http://192.168.1.100:631/ipp/print");
IppPacket printRequest = new IppPacket(Operation.printJob, 123,
        groupOf(operationAttributes,
                attributesCharset.of("utf-8"),
                attributesNaturalLanguage.of("en"),
                printerUri.of(uri),
                requestingUserName.of("user"),
                documentFormat.of("application/octet-stream")));
transport.sendData(uri, new IppPacketData(printRequest, new FileInputStream(inputFile)));
```

A very basic use case is demonstrated by the `jprint` sample app. To run it:

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

Until 1.0, APIs may still be changed in non-backwards-compatible ways.

## Migration to v0.6.12

* PCLM and PWG Raster got upgrades, in that they properly handle multi-page and duplex cases. You're only responsible for providing a normal, front-to-back RenderableDocument, and to pass along valid settings based on known printer attributes and user output requirements. The PDL library internally handles page re-ordering, page rotation, flipping, etc to provide the best possible output. As a result the `PwgCapabilities` and `PclmCapabilties` classes have been removed in favor of `PwgSettings` and `PclmSettings`.

## Migration to v0.6.6

* The `KeywordOrName` type was introduced to allow for fields that can legitimately contain either IANA-registered
  keywords OR unregistered, locally-defined names. For example, `MediaCol.mediaType` must contain a KeywordOrName,
  not simply a String (Keyword).

## Migration to v0.6

0.6 includes some API changes. Some changes may require changes in your code.

* `Types` now includes all defined types as published as of 2018-04-06 at
  [IANA](https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml).
* Collection types such as `MediaCol` are now expressed as POJO and are automatically constructed/deconstructed.
* `com.hp.jipp.model.IppPacket` moved to `com.hp.jipp.encoding.IppPacket` so that all PWG generated code lives in `.model`.
* Keyword attribute values now present as simple, untyped Strings. Allowed strings are provided in static objects
  (e.g. `Media.java` defines all possible media types.)
* The `MediaSize` type is removed in favor of Media strings. If needed, the `MediaSizes` utility class provides a method
  to extract x- and y-dimensions from a Media keyword containing dimensions.
* The `Attribute` class is now a `List` of attribute values, so it is no longer necessary to call `.getValues()`
  to obtain them.
* The `AttributeGroup` class is now a `List` of `Attribute<*>` so it may be iterated directly to access attributes it
  contains.
* Attributes of `Name` or `Text` types now appear in those types to allow clients to access language information if
  present and to distinguish from ordinary keywords. The following additional methods may help to convert attribute
  values to String:
  * `Name.asString()` and `Text.asString()` are `Stringable` and extract the value in string form. `.getValue()` also does this.
  * `Attribute.strings()`, `AttributeGroup.getStrings()`, and `IppPacket.getStrings()` do the same for all types.
* `Status.ok` is now `Status.successfulOk`, etc.

## Dependencies

`jipp-core`'s only dependencies are JDK 6+ and the current Kotlin runtime.

## Building

`./gradlew build`

A full build of this project requires `python` (2.x) and `dot` to generate dependency graphs

## Related projects

* [`javax.print` (JSR6)](https://docs.oracle.com/javase/7/docs/api/javax/print/package-summary.html) - Standard Java printing APIs. IPP 1.1 only, client-side only. API is not supported by Android.
* [Cups4J](http://www.cups4j.org/) - GPL licensed, with a [port to Android](https://github.com/BenoitDuffez/AndroidCupsPrint).
* [JSPI](https://github.com/bhagyas/jspi)
