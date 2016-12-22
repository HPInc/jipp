package com.hp.jipp.model;

import com.google.common.collect.ImmutableSet;
import com.hp.jipp.util.Id;

import java.util.HashMap;

/**
 * Operation identifiers as defined by RFC2911 (https://tools.ietf.org/html/rfc2911).
 */
public class Operations {
    public final static Operation PrintJob = Operation.create("Print-Job", 0x0002);
    public final static Operation PrintUri = Operation.create("Print-URI", 0x0003);
    public final static Operation ValidateJob = Operation.create("Validate-Job", 0x0004);
    public final static Operation CreateJob = Operation.create("Create-Job", 0x0005);
    public final static Operation SendDocument = Operation.create("SendDocument", 0x0006);
    public final static Operation SendUri = Operation.create("Send-URI", 0x0007);
    public final static Operation CancelJob = Operation.create("Cancel-Job", 0x0008);
    public final static Operation GetJobAttributes = Operation.create("Get-Job-Attributes", 0x0009);
    public final static Operation GetJobs = Operation.create("Get-Jobs", 0x000A);
    public final static Operation GetPrinterAttributes = Operation.create("Get-Printer-Attributes",
            0x000B);
    public final static Operation HoldJob = Operation.create("Hold-Job", 0x000C);
    public final static Operation ReleaseJob = Operation.create("Release-Job", 0x000D);
    public final static Operation RestartJob = Operation.create("Restart-Job", 0x000E);
    public final static Operation PausePrinter = Operation.create("Pause-Printer", 0x0010);
    public final static Operation ResumePrinter = Operation.create("Resume-Printer", 0x0011);
    public final static Operation PurgeJobs = Operation.create("Purge-Jobs", 0x0012);

    /** A set of all known Operation identifiers */
    public final static ImmutableSet<Operation> All = new ImmutableSet.Builder<Operation>().add(
            PrintJob, PrintUri, ValidateJob, CreateJob, SendDocument, SendUri, CancelJob,
            GetJobAttributes, GetJobs, GetPrinterAttributes, HoldJob, ReleaseJob, RestartJob,
            PausePrinter, ResumePrinter, PurgeJobs
    ).build();
}
