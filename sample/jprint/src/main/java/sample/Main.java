package sample;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Types;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Tag.*;
import static com.hp.jipp.model.Types.*;

class Main {
    private final static String FORMAT_PDF = "application/pdf";
    private final static String USAGE = "Usage: [PRINTER_PATH] [FILE] <MIME_FORMAT>\n";

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException(USAGE + "Received: " +
                    Arrays.asList(args));
        }
        String format = args.length > 2 ? args[2] : FORMAT_PDF;
        URI uri = URI.create(args[0]);
        File inputFile = new File(args[1]);

        IppClientTransport transport = new HttpIppClientTransport();

        // Query for supported document formats
        IppPacket attributeRequest = new IppPacket(Operation.getPrinterAttributes, 1,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint"),
                        requestedAttributes.of(documentFormatSupported.getName())));

        System.out.println("Sending " + attributeRequest.prettyPrint(100, "  "));
        IppPacketData request = new IppPacketData(attributeRequest, new FileInputStream(inputFile));
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));

        // Make sure the format is supported
        List<String> formats = response.getPacket().getStrings(printerAttributes, documentFormatSupported);
        if (!formats.contains(format)) {
            throw new IllegalArgumentException(USAGE + format + " format not supported in " + formats);
        }

        // Deliver the print request
        IppPacket printRequest = new IppPacket(Operation.printJob, 2,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint"),
                        documentFormat.of(format)));

        System.out.println("Sending " + printRequest.prettyPrint(100, "  "));
        request = new IppPacketData(printRequest, new FileInputStream(inputFile));
        response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
    }
}
