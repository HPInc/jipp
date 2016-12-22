package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * An operation identifier
 */
@AutoValue
public abstract class Operation {

    public final static Operation PrintJob = create("Print-Job", 0x0002);
    public final static Operation PrintUri = create("Print-URI", 0x0003);
    public final static Operation ValidateJob = create("Validate-Job", 0x0004);
    public final static Operation CreateJob = create("Create-Job", 0x0005);
    public final static Operation SendDocument = create("SendDocument", 0x0006);
    public final static Operation SendUri = create("Send-URI", 0x0007);
    public final static Operation CancelJob = create("Cancel-Job", 0x0008);
    public final static Operation GetJobAttributes = create("Get-Job-Attributes", 0x0009);
    public final static Operation GetJobs = create("Get-Jobs", 0x000A);
    public final static Operation GetPrinterAttributes = create("Get-Printer-Attributes",
            0x000B);
    public final static Operation HoldJob = create("Hold-Job", 0x000C);
    public final static Operation ReleaseJob = create("Release-Job", 0x000D);
    public final static Operation RestartJob = create("Restart-Job", 0x000E);
    public final static Operation PausePrinter = create("Pause-Printer", 0x0010);
    public final static Operation ResumePrinter = create("Resume-Printer", 0x0011);
    public final static Operation PurgeJobs = create("Purge-Jobs", 0x0012);

    /** A set of all known Operation identifiers */
    public final static ImmutableSet<Operation> All = new ImmutableSet.Builder<Operation>().add(
            PrintJob, PrintUri, ValidateJob, CreateJob, SendDocument, SendUri, CancelJob,
            GetJobAttributes, GetJobs, GetPrinterAttributes, HoldJob, ReleaseJob, RestartJob,
            PausePrinter, ResumePrinter, PurgeJobs
    ).build();

    private final static ImmutableMap<Integer, Operation> CODE_TO_OPERATION;
    static {
        ImmutableMap.Builder<Integer, Operation> builder = new ImmutableMap.Builder<>();
        for (Operation op : All) {
            builder.put(op.getValue(), op);
        }
        CODE_TO_OPERATION = builder.build();
    }

    /**
     * Look up or convert an operation code into an Operation object
     */
    public static Operation toOperation(int operationCode) {
        Operation found = CODE_TO_OPERATION.get(operationCode);
        if (found != null) return found;
        return create("UNKNOWN(x" + Integer.toHexString(operationCode) + ")", operationCode);
    }

    /**
     * Returns a new instance
     * @param name human-readable name of the the operation
     * @param value machine-readable identifier for the operation
     */
    public static Operation create(String name, int value) {
        return new AutoValue_Operation(name, value);
    }

    abstract public String getName();
    abstract public int getValue();

    @Override
    public final String toString() {
        return getName();
    }
}