package sample;

import com.hp.jipp.model.IppPacket;
import com.hp.jipp.pwg.Operation;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Tag.*;
import static com.hp.jipp.model.Types.*;

class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Arguments [PRINTER_PATH] [FILE] are required, received: " +
                    Arrays.asList(args));
        }
        URI uri = URI.create(args[0]);
        File inputFile = new File(args[1]);

        IppPacket printRequest = new IppPacket(Operation.printJob, 123,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint"),
                        documentFormat.of("application/octet-stream")));

        System.out.println("Sending " + printRequest.prettyPrint(1200, "  "));
        IppClientTransport transport = new HttpIppClientTransport();
        IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
    }
}
