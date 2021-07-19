[![CircleCI](https://circleci.com/gh/HPInc/jipp.svg?style=svg&circle-token=4baa4b142e5cc6f6cf6e803a8c5832a9dd755a25)](https://circleci.com/gh/HPInc/jipp)
[![CodeCov](https://codecov.io/github/HPInc/jipp/coverage.svg?branch=master)](https://codecov.io/github/HPInc/jipp)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hp.jipp/jipp-core)
[![Core Docs](https://img.shields.io/badge/docs-core-brightgreen.svg)](https://hpinc.github.io/jipp/api/jipp-core)
[![PDL Docs](https://img.shields.io/badge/docs-pdl-brightgreen.svg)](https://hpinc.github.io/jipp/api/jipp-pdl)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.5.0-blue.svg)](https://kotlinlang.org/)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

# JIPP: A Java-compatible IPP library

This project includes:

* `jipp-core` is an IPP parser/builder for [IPP packets](https://en.wikipedia.org/wiki/Internet_Printing_Protocol).
* `jipp-pdls` converts rasterized documents into common page description languages (PCLm and PWG-Raster).
* `jprint` shows how `jipp-core` can be used to send a document to a printer.
* `jrender` shows how `jipp-pdl` can be used to convert a PDF to PCLm or PWG-Raster.

`jipp-core` features:

* Supports construction of IPP servers, clients, routers, gateways, etc.
* Common operations and attributes available for use.
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

The API is Java-compatible but implemented in Kotlin.

## Usage

1. Add the current version of JIPP to your project
```gradle
dependencies {
    compile 'com.hp.jipp:jipp-core:0.7.11'
    compile 'com.hp.jipp:jipp-pdl:0.7.11' // Only needed if transforming PDLs
}
```
2. Create an `IppClientTransport` or `IppServerTransport` (see example
[`HttpIppClientTransport.java`](https://github.com/HPInc/jipp/blob/master/sample/jprint/src/main/java/sample/HttpIppClientTransport.java))
3. Use the transport to send and receive `IppPacket` objects, e.g.:
```java
URI uri = URI.create("http://192.168.1.100:631/ipp/print");
IppPacket printRequest = IppPacket.printJob(uri)
        .putOperationAttributes(documentFormat.of("application/pdf")))
        .build();
transport.sendData(uri, new IppPacketData(printRequest, new FileInputStream(inputFile)));
```

## Sample Applications

### jprint

Demonstrates a simple print engine. To run:

```bash
# build the app
./gradlew jprint:build

# unzip in the current directory
unzip -o ./sample/jprint/build/distributions/jprint-*.zip

# Use IPP to print a file to the supplied HTTP/IPP endpoint.
# (The printer must natively support the supplied file type.)
jprint-*/bin/jprint -p sample.pdf ipp://192.168.1.102:631/ipp/print
```

### jrender

An example of rendering a PDF to PWG-Raster or PCLm. To run:

```bash
# build the app
./gradlew jrender:build

# unzip in the current directory
unzip -o ./sample/jrender/build/distributions/jrender-*.zip

# Convert a PDF-file to PWG-Raster.
jrender-*/bin/jrender sample.pdf sample.pwg

# Convert a PDF-file to PCLm.
jrender-*/bin/jrender sample.pdf sample.pclm
```

## API Maturity

Until 1.0, APIs may still be changed in non-backwards-compatible ways. See [HISTORY.md](HISTORY.md) for more details.

## Dependencies

`jipp-core`'s only dependencies are JDK 8+ and the current Kotlin runtime.

## Building

To build, run `./gradlew build`.

A full build of this project requires `python` (2.x) and `dot` to generate dependency graphs.
