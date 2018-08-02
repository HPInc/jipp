// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06
@file:Suppress("MaxLineLength", "WildcardImport", "MagicNumber")

package com.hp.jipp.pwg

import com.hp.jipp.encoding.* // ktlint-disable no-wildcard-imports
import com.hp.jipp.util.getStaticObjects

/**
 * Attribute types for the Job Template group.
 */
object JobTemplateGroup {

    /**
     * "confirmation-sheet-print" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val confirmationSheetPrint = BooleanType("confirmation-sheet-print")

    /**
     * "copies" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val copies = IntType("copies")

    /**
     * "cover-back" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val coverBack = CoverBack.Type("cover-back")

    /**
     * "cover-front" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val coverFront = CoverFront.Type("cover-front")

    /**
     * "cover-sheet-info" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val coverSheetInfo = CoverSheetInfo.Type("cover-sheet-info")

    /**
     * "destination-statuses" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val destinationStatuses = DestinationStatuses.Type("destination-statuses")

    /**
     * "destination-uris" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf),
     * [PWG5100.17](http://ftp.pwg.org/pub/pwg/candidates/cs-ippscan10-20140918-5100.17.pdf).
     */
    @JvmField val destinationUris = DestinationUris.Type("destination-uris")

    /**
     * "feed-orientation" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     * May contain any keyword from [FeedOrientation].
     */
    @JvmField val feedOrientation = KeywordType("feed-orientation")

    /**
     * "finishings" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val finishings = Finishing.Type("finishings")

    /**
     * "finishings-col" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf),
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val finishingsCol = FinishingsCol.Type("finishings-col")

    /**
     * "font-name-requested" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val fontNameRequested = NameType("font-name-requested")

    /**
     * "font-size-requested" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val fontSizeRequested = IntType("font-size-requested")

    /**
     * "force-front-side" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val forceFrontSide = IntType("force-front-side")

    /**
     * "imposition-template" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [ImpositionTemplate] or a name.
     */
    @JvmField val impositionTemplate = KeywordType("imposition-template")

    /**
     * "insert-sheet" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val insertSheet = InsertSheet.Type("insert-sheet")

    /**
     * "job-account-id" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val jobAccountId = NameType("job-account-id")

    /**
     * "job-account-type" as defined in:
     * [PWG5100.16](http://ftp.pwg.org/pub/pwg/candidates/cs-ipptrans10-20131108-5100.16.pdf).
     * May contain any keyword from [JobAccountType] or a name.
     */
    @JvmField val jobAccountType = KeywordType("job-account-type")

    /**
     * "job-accounting-sheets" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val jobAccountingSheets = JobAccountingSheets.Type("job-accounting-sheets")

    /**
     * "job-accounting-user-id" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val jobAccountingUserId = NameType("job-accounting-user-id")

    /**
     * "job-copies" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val jobCopies = IntType("job-copies")

    /**
     * "job-cover-back" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val jobCoverBack = CoverBack.Type("job-cover-back")

    /**
     * "job-cover-front" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val jobCoverFront = CoverFront.Type("job-cover-front")

    /**
     * "job-delay-output-until" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     * May contain any keyword from [JobDelayOutputUntil] or a name.
     */
    @JvmField val jobDelayOutputUntil = KeywordType("job-delay-output-until")

    /**
     * "job-delay-output-until-time" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val jobDelayOutputUntilTime = DateTimeType("job-delay-output-until-time")

    /**
     * "job-error-action" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     * May contain any keyword from [JobErrorAction].
     */
    @JvmField val jobErrorAction = KeywordType("job-error-action")

    /**
     * "job-error-sheet" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val jobErrorSheet = JobErrorSheet.Type("job-error-sheet")

    /**
     * "job-finishings" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val jobFinishings = Finishing.Type("job-finishings")

    /**
     * "job-finishings-col" as defined in:
     * [PWG5100.5](http://ftp.pwg.org/pub/pwg/candidates/cs-ippdocobject10-20031031-5100.5.pdf).
     */
    @JvmField val jobFinishingsCol = FinishingsCol.Type("job-finishings-col")

    /**
     * "job-hold-until" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [JobHoldUntil] or a name.
     */
    @JvmField val jobHoldUntil = KeywordType("job-hold-until")

    /**
     * "job-hold-until-time" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val jobHoldUntilTime = DateTimeType("job-hold-until-time")

    /**
     * "job-message-to-operator" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val jobMessageToOperator = TextType("job-message-to-operator")

    /**
     * "job-pages-per-set" as defined in:
     * [PWG5100.1](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfinishings10-20010205-5100.1.pdf).
     */
    @JvmField val jobPagesPerSet = IntType("job-pages-per-set")

    /**
     * "job-phone-number" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val jobPhoneNumber = UriType("job-phone-number")

    /**
     * "job-priority" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val jobPriority = IntType("job-priority")

    /**
     * "job-recipient-name" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val jobRecipientName = NameType("job-recipient-name")

    /**
     * "job-save-disposition" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val jobSaveDisposition = JobSaveDisposition.Type("job-save-disposition")

    /**
     * "job-sheet-message" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val jobSheetMessage = TextType("job-sheet-message")

    /**
     * "job-sheets" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [JobSheet] or a name.
     */
    @JvmField val jobSheets = KeywordType("job-sheets")

    /**
     * "job-sheets-col" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val jobSheetsCol = JobSheetsCol.Type("job-sheets-col")

    /**
     * "materials-col" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     */
    @JvmField val materialsCol = MaterialsCol.Type("materials-col")

    /**
     * "media" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [Media] or a name.
     */
    @JvmField val media = KeywordType("media")

    /**
     * "media-col" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf),
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf),
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val mediaCol = MediaCol.Type("media-col")

    /**
     * "media-input-tray-check" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [Media] or a name.
     */
    @JvmField val mediaInputTrayCheck = KeywordType("media-input-tray-check")

    /**
     * "multiple-document-handling" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [MultipleDocumentHandling].
     */
    @JvmField val multipleDocumentHandling = KeywordType("multiple-document-handling")

    /**
     * "multiple-object-handling" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     * May contain any keyword from [MultipleObjectHandling].
     */
    @JvmField val multipleObjectHandling = KeywordType("multiple-object-handling")

    /**
     * "number-of-retries" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val numberOfRetries = IntType("number-of-retries")

    /**
     * "number-up" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val numberUp = IntType("number-up")

    /**
     * "orientation-requested" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val orientationRequested = Orientation.Type("orientation-requested")

    /**
     * "output-bin" as defined in:
     * [PWG5100.2](http://ftp.pwg.org/pub/pwg/candidates/cs-ippoutputbin10-20010207-5100.2.pdf).
     * May contain any keyword from [OutputBin] or a name.
     */
    @JvmField val outputBin = KeywordType("output-bin")

    /**
     * "output-device" as defined in:
     * [PWG5100.7](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobext10-20031031-5100.7.pdf).
     */
    @JvmField val outputDevice = NameType("output-device")

    /**
     * "overrides" as defined in:
     * [PWG5100.6](http://ftp.pwg.org/pub/pwg/candidates/cs-ipppageoverride10-20031031-5100.6.pdf).
     */
    @JvmField val overrides = Overrides.Type("overrides")

    /**
     * "page-delivery" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [PageDelivery].
     */
    @JvmField val pageDelivery = KeywordType("page-delivery")

    /**
     * "page-order-received" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [PageOrderReceived].
     */
    @JvmField val pageOrderReceived = KeywordType("page-order-received")

    /**
     * "page-ranges" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val pageRanges = IntRangeType("page-ranges")

    /**
     * "pages-per-subset" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     */
    @JvmField val pagesPerSubset = IntType("pages-per-subset")

    /**
     * "pdl-init-file" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val pdlInitFile = PdlInitFile.Type("pdl-init-file")

    /**
     * "platform-temperature" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     */
    @JvmField val platformTemperature = IntType("platform-temperature")

    /**
     * "presentation-direction-number-up" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [PresentationDirectionNumberUp].
     */
    @JvmField val presentationDirectionNumberUp = KeywordType("presentation-direction-number-up")

    /**
     * "print-accuracy" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     */
    @JvmField val printAccuracy = PrintAccuracy.Type("print-accuracy")

    /**
     * "print-base" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     * May contain any keyword from [PrintBase].
     */
    @JvmField val printBase = KeywordType("print-base")

    /**
     * "print-color-mode" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     * May contain any keyword from [PrintColorMode].
     */
    @JvmField val printColorMode = KeywordType("print-color-mode")

    /**
     * "print-content-optimize" as defined in:
     * [PWG5100.7](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobext10-20031031-5100.7.pdf).
     * May contain any keyword from [PrintContentOptimize].
     */
    @JvmField val printContentOptimize = KeywordType("print-content-optimize")

    /**
     * "print-objects" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     */
    @JvmField val printObjects = PrintObjects.Type("print-objects")

    /**
     * "print-quality" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printQuality = PrintQuality.Type("print-quality")

    /**
     * "print-rendering-intent" as defined in:
     * [PWG5100.13](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext3v10-20120727-5100.13.pdf).
     * May contain any keyword from [PrintRenderingIntent].
     */
    @JvmField val printRenderingIntent = KeywordType("print-rendering-intent")

    /**
     * "print-scaling" as defined in:
     * [PWG5100.16](http://ftp.pwg.org/pub/pwg/candidates/cs-ipptrans10-20131108-5100.16.pdf).
     * May contain any keyword from [PrintScaling].
     */
    @JvmField val printScaling = KeywordType("print-scaling")

    /**
     * "print-supports" as defined in:
     * [PWG5100.21](http://ftp.pwg.org/pub/pwg/candidates/cs-ipp3d10-20170210-5100.21.pdf).
     * May contain any keyword from [PrintSupports].
     */
    @JvmField val printSupports = KeywordType("print-supports")

    /**
     * "printer-resolution" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val printerResolution = ResolutionType("printer-resolution")

    /**
     * "proof-print" as defined in:
     * [PWG5100.11](http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf).
     */
    @JvmField val proofPrint = ProofPrint.Type("proof-print")

    /**
     * "retry-interval" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val retryInterval = IntType("retry-interval")

    /**
     * "retry-time-out" as defined in:
     * [PWG5100.15](http://ftp.pwg.org/pub/pwg/candidates/cs-ippfaxout10-20131115-5100.15.pdf).
     */
    @JvmField val retryTimeOut = IntType("retry-time-out")

    /**
     * "separator-sheets" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf),
     * [RFC8011](http://www.iana.org/go/rfc8011).
     */
    @JvmField val separatorSheets = SeparatorSheets.Type("separator-sheets")

    /**
     * "sheet-collate" as defined in:
     * [RFC3381](http://www.iana.org/go/rfc3381).
     * May contain any keyword from [SheetCollate].
     */
    @JvmField val sheetCollate = KeywordType("sheet-collate")

    /**
     * "sides" as defined in:
     * [RFC8011](http://www.iana.org/go/rfc8011).
     * May contain any keyword from [Sides].
     */
    @JvmField val sides = KeywordType("sides")

    /**
     * "x-image-position" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [XImagePosition].
     */
    @JvmField val xImagePosition = KeywordType("x-image-position")

    /**
     * "x-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val xImageShift = IntType("x-image-shift")

    /**
     * "x-side1-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val xSide1ImageShift = IntType("x-side1-image-shift")

    /**
     * "x-side2-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val xSide2ImageShift = IntType("x-side2-image-shift")

    /**
     * "y-image-position" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     * May contain any keyword from [YImagePosition].
     */
    @JvmField val yImagePosition = KeywordType("y-image-position")

    /**
     * "y-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val yImageShift = IntType("y-image-shift")

    /**
     * "y-side1-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val ySide1ImageShift = IntType("y-side1-image-shift")

    /**
     * "y-side2-image-shift" as defined in:
     * [PWG5100.3](http://ftp.pwg.org/pub/pwg/candidates/cs-ippprodprint10-20010212-5100.3.pdf).
     */
    @JvmField val ySide2ImageShift = IntType("y-side2-image-shift")
}
