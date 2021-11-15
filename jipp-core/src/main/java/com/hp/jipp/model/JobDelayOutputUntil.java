// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2021-10-14
//
// (Note: Uses Java, not Kotlin, to define public static values in the most efficient manner.)

package com.hp.jipp.model;

/**
 * Values applicable for "job-delay-output-until" keywords (or names).
 *
 * Also used by: "job-delay-output-until-default", "job-delay-output-until-supported".
 *
 * @see <a href="https://ftp.pwg.org/pub/pwg/candidates/cs-ippjobext20-20190816-5100.7.pdf">PWG5100.7</a>
 */
public class JobDelayOutputUntil {
    public static final String dayTime = "day-time";
    public static final String evening = "evening";
    public static final String indefinite = "indefinite";
    public static final String night = "night";
    public static final String noDelayOutput = "no-delay-output";
    public static final String secondShift = "second-shift";
    public static final String thirdShift = "third-shift";
    public static final String weekend = "weekend";
}