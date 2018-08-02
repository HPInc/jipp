// Copyright 2018 HP Development Company, L.P.
// SPDX-License-Identifier: MIT
//
// DO NOT MODIFY. Code is auto-generated by genTypes.py. Content taken from registry at
// https://www.iana.org/assignments/ipp-registrations/ipp-registrations.xml, updated on 2018-04-06

package com.hp.jipp.pwg;

/**
 * Values applicable for "which-jobs" keywords, as defined in
 * <a href="http://ftp.pwg.org/pub/pwg/candidates/cs-ippjobprinterext10-20101030-5100.11.pdf">PWG5100.11</a>,
 * <a href="http://ftp.pwg.org/pub/pwg/candidates/cs-ippinfra10-20150619-5100.18.pdf">PWG5100.18</a>,
 * <a href="http://www.iana.org/go/rfc8011">RFC8011</a>.
 *
 * Also used by:
 *   * `which-jobs-supported`
 */
public class WhichJobs {
    public static final String aborted = "aborted";
    public static final String all = "all";
    public static final String canceled = "canceled";
    public static final String completed = "completed";
    public static final String fetchable = "fetchable";
    public static final String notCompleted = "not-completed";
    public static final String pending = "pending";
    public static final String pendingHeld = "pending-held";
    public static final String processing = "processing";
    public static final String processingStopped = "processing-stopped";
    public static final String proofPrint = "proof-print";
    public static final String saved = "saved";
}