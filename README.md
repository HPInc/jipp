# JIPP (Java IPP)
[![CircleCI](https://circleci.com/gh/e2em/jipp.svg?style=shield&circle-token=6641e01d90c7cf22d45e6b01d46bf2e9630777c4)](https://circleci.com/gh/e2em/jipp)
[![CodeCov](https://codecov.io/github/e2em/jipp/coverage.svg?branch=master&token=tBlASKX9VN)](https://codecov.io/github/e2em/jipp)

Core IPP parser/builder for [IPP packets](https://en.wikipedia.org/wiki/Internet_Printing_Protocol).

Features:
* Supports construction of IPP servers, clients, and routers.
* IPP operations and attributes for basic operations are present.
* Can be extended to support new operations and attributes.
* Can be used over any transport.

What can I do with this?
* Implement an Android Print Service
* Scan and show available printers on your network to your users.
* Test IPP clients or IPP printers in interesting ways.
* Experiment with alternative transports.
* Implement a cloud-based print server or client.

# API Documentation

JavaDoc is currently available from the latest build artifacts at https://circleci.com.

# Build requirements

A full build of this project requires `python` (2.x) and `dot` which are readily accessible in most distributions.

# Related projects

* [`javax.print` (JSR6)](https://docs.oracle.com/javase/7/docs/api/javax/print/package-summary.html) - Standard Java printing APIs. IPP 1.1 only, client-side only. API is not supported by Android.
* [Cups4J](http://www.cups4j.org/) - GPL licensed, with a [port to Android](https://github.com/BenoitDuffez/AndroidCupsPrint).
* [JSPI](https://github.com/bhagyas/jspi) - Copied from an old open source project, installed as a maven repo.
