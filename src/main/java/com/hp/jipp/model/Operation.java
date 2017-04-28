package com.hp.jipp.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.hp.jipp.encoding.NameCodeType;
import com.hp.jipp.encoding.NameCode;

/**
 * An operation code as found in request packets and elsewhere.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-5.2.2">RFC2911 Section 5.2.2</a>
 */
@AutoValue
public abstract class Operation extends NameCode {
    // Note: this is really an OperationCode. If there are ever real Operation objects, rename.

    public final static Operation PrintJob = of("Print-Job", 0x0002);
    public final static Operation PrintUri = of("Print-URI", 0x0003);
    public final static Operation ValidateJob = of("Validate-Job", 0x0004);
    public final static Operation CreateJob = of("Create-Job", 0x0005);
    public final static Operation SendDocument = of("Send-Document", 0x0006);
    public final static Operation SendUri = of("Send-URI", 0x0007);
    public final static Operation CancelJob = of("Cancel-Job", 0x0008);
    public final static Operation GetJobAttributes = of("Get-Job-Attributes", 0x0009);
    public final static Operation GetJobs = of("Get-Jobs", 0x000A);
    public final static Operation GetPrinterAttributes = of("Get-Printer-Attributes", 0x000B);
    public final static Operation HoldJob = of("Hold-Job", 0x000C);
    public final static Operation ReleaseJob = of("Release-Job", 0x000D);
    public final static Operation RestartJob = of("Restart-Job", 0x000E);
    public final static Operation PausePrinter = of("Pause-Printer", 0x0010);
    public final static Operation ResumePrinter = of("Resume-Printer", 0x0011);
    public final static Operation PurgeJobs = of("Purge-Jobs", 0x0012);

    /** The encoder for converting integers to Operation objects */
    public final static NameCodeType.Encoder<Operation> ENCODER = NameCodeType.encoder(
            "operation-id", ImmutableSet.of(
                    PrintJob, PrintUri, ValidateJob, CreateJob, SendDocument, SendUri, CancelJob, GetJobAttributes,
                    GetJobs, GetPrinterAttributes, HoldJob, ReleaseJob, RestartJob, PausePrinter, ResumePrinter,
                    PurgeJobs
            ), new NameCode.Factory<Operation>() {
                @Override
                public Operation of(String name, int code) {
                    return Operation.of(name, code);
                }
            });

    /** Create and return a {@link NameCodeType} based on this NameCode */
    public static NameCodeType<Operation> createType(String attributeName) {
        return new NameCodeType<>(ENCODER, attributeName);
    }

    /**
     * Returns a new instance
     * @param name human-readable name of the the operation
     * @param code machine-readable identifier for the operation
     */
    public static Operation of(String name, int code) {
        return new AutoValue_Operation(name, code);
    }
}
