# API Changes

## 0.6.18
* Allow both multiple groups and extensions to existing groups (follow up to #62).
* Allow mutable attribute groups (#65). Unfortunately this breaks `new AttributeGroup(...)`, which must be replaced with `groupOf(...)` or `mutableGroupOf(...)`.
* Simplified Kotlin DSL by using `MutableAttributeGroup`.

## 0.6.17
* Pass orientation into `PwgHeader` (#59).
* More flexible Kotlin DLS (#62).
* Latest symbols from IANA listing.

## 0.6.16
* Allow for more attribute groups in the IPP Packet DSL (#33).
* Add better `toString()` output for AttributeTypes (#55).
* Replace OutputSettings `outputBin` and `stackingOrder` with `reversed` (#56).
