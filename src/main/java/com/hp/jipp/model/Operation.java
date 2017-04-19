package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.EnumType;
import com.hp.jipp.encoding.NameCode;

import java.util.Collection;

/**
 * An operation code as found in request packets and elsewhere.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-5.2.2">RFC2911 Section 5.2.2</a>
 */
@AutoValue
public abstract class Operation extends NameCode {

    public final static Operation PrintJob = create("Print-Job", 0x0002);
    public final static Operation PrintUri = create("Print-URI", 0x0003);
    public final static Operation ValidateJob = create("Validate-Job", 0x0004);
    public final static Operation CreateJob = create("Create-Job", 0x0005);
    public final static Operation SendDocument = create("SendDocument", 0x0006);
    public final static Operation SendUri = create("Send-URI", 0x0007);
    public final static Operation CancelJob = create("Cancel-Job", 0x0008);
    public final static Operation GetJobAttributes = create("Get-Job-Attributes", 0x0009);
    public final static Operation GetJobs = create("Get-Jobs", 0x000A);
    public final static Operation GetPrinterAttributes = create("Get-Printer-Attributes", 0x000B);
    public final static Operation HoldJob = create("Hold-Job", 0x000C);
    public final static Operation ReleaseJob = create("Release-Job", 0x000D);
    public final static Operation RestartJob = create("Restart-Job", 0x000E);
    public final static Operation PausePrinter = create("Pause-Printer", 0x0010);
    public final static Operation ResumePrinter = create("Resume-Printer", 0x0011);
    public final static Operation PurgeJobs = create("Purge-Jobs", 0x0012);

    private final static EnumType.Encoder<Operation> ENCODER = EnumType.encoder(
            "operation-id", ImmutableSet.of(
                    PrintJob, PrintUri, ValidateJob, CreateJob, SendDocument, SendUri, CancelJob, GetJobAttributes,
                    GetJobs, GetPrinterAttributes, HoldJob, ReleaseJob, RestartJob, PausePrinter, ResumePrinter,
                    PurgeJobs
            ), new NameCode.Factory<Operation>() {
                @Override
                public Operation create(String name, int code) {
                    return Operation.create(name, code);
                }
            });

    /** Create and return a {@link EnumType} based on this NameCode */
    public static EnumType<Operation> createType(String attributeName) {
        return new EnumType<>(ENCODER, attributeName);
    }

    /** Return true if this is a known operation code */
    public static boolean isKnown(int code) {
        return ENCODER.getEnumsMap().containsKey(code);
    }

    /**
     * Look up or convert an operation code into an Operation object
     */
    public static Operation toOperation(int code) {
        return ENCODER.getEnum(code);
    }

    /**
     * Returns a new instance
     * @param name human-readable name of the the operation
     * @param code machine-readable identifier for the operation
     */
    public static Operation create(String name, int code) {
        return new AutoValue_Operation(name, code);
    }
}
