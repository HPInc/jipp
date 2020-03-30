# History of Changes

## 0.7.0
* Stabilize `IppInputStream` and `IppOutputStream` APIs (#79).
* Replace Kotlin DSL with Java-friendly IppPacket.Builder (#77).
* Subclass `Tag` objects to prevent misuse (#82).
* Update types for IANA registrations as of 2020-02-20 (#83).
* Remove useless types from `.model` package (#47).
* Cleaner `toString()` output for collection types (#28).
* Only allow multiple attribute values when permitted by the spec (#29).

## 0.6.22
* Handle PWG raster color space of 1 (RGB) (#72).

## 0.6.21
* Do not assume packet will be read completely before parsing it (#71).

## 0.6.19
* Allow both multiple groups and extensions to existing groups (follow up to #62).
* Allow mutable attribute groups (#65). Unfortunately this breaks `new AttributeGroup(...)`, which must be replaced with `groupOf(...)` or `mutableGroupOf(...)`.
* Simplified Kotlin DSL by introducing `MutableAttributeGroup`, which replaces `InAttributeGroup`.
* Added format check to jprint (#66).

## 0.6.17
* Pass orientation into `PwgHeader` (#59).
* More flexible Kotlin DLS (#62).
* Latest symbols from IANA listing.

## 0.6.16
* Allow for more attribute groups in the IPP Packet DSL (#33).
* Add better `toString()` output for AttributeTypes (#55).
* Replace OutputSettings `outputBin` and `stackingOrder` with `reversed` (#56).

## 0.6.12
* PCLM and PWG Raster got upgrades, in that they properly handle multi-page and duplex cases. You're only responsible for providing a normal, front-to-back RenderableDocument, and to pass along valid settings based on known printer attributes and user output requirements. The PDL library internally handles page re-ordering, page rotation, flipping, etc to provide the best possible output. As a result the `PwgCapabilities` and `PclmCapabilties` classes have been removed in favor of `PwgSettings` and `PclmSettings`.
* `Attribute.unknown` etc was moved to `Attributes.unknown` for backwards-compatibility with Java.

## 0.6.6
* The `KeywordOrName` type was introduced to allow for fields that can legitimately contain either IANA-registered
  keywords OR unregistered, locally-defined names. For example, `MediaCol.mediaType` must contain a KeywordOrName,
  not simply a String (Keyword).

## Earlier versions
* `Types` includes all defined types as published as of 2018-04-06 at
  [IANA](https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml).
* Collection types such as `MediaCol` are expressed as POJO and are automatically constructed/deconstructed.
* `com.hp.jipp.model.IppPacket` moved to `com.hp.jipp.encoding.IppPacket` so that all PWG generated code lives in `.model`.
* Keyword attribute values are present as simple, untyped Strings. Allowed strings are provided in static objects
  (e.g. `Media.java` defines all possible media types.)
* The `MediaSize` type is removed in favor of Media strings. If needed, the `MediaSizes` utility class provides a method
  to extract x- and y-dimensions from a Media keyword containing dimensions.
* The `Attribute` class is a `List` of attribute values, so it is no longer necessary to call `.getValues()`
  to obtain them.
* The `AttributeGroup` class is a `List` of `Attribute<*>` so it may be iterated directly to access attributes it
  contains.
* Attributes of `Name` or `Text` types appear in those types to allow clients to access language information if
  present and to distinguish from ordinary keywords. The following additional methods may help to convert attribute
  values to String:
  * `Name.asString()` and `Text.asString()` are `Stringable` and extract the value in string form. `.getValue()` also does this.
  * `Attribute.strings()`, `AttributeGroup.getStrings()`, and `IppPacket.getStrings()` do the same for all types.
* `Status.ok` becomes `Status.successfulOk`, etc.
