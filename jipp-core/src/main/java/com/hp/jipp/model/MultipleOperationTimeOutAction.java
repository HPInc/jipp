// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2023-11-27
//
// (Note: Uses Java, not Kotlin, to define public static values in the most efficient manner.)

package com.hp.jipp.model;

/**
 * Values applicable for "multiple-operation-time-out-action" keywords.
 *
 * @see <a href="https://ftp.pwg.org/pub/pwg/candidates/cs-ippnodriver20-20230301-5100.13.pdf">PWG5100.13</a>
 */
public class MultipleOperationTimeOutAction {
    public static final String abortJob = "abort-job";
    public static final String holdJob = "hold-job";
    public static final String processJob = "process-job";
}