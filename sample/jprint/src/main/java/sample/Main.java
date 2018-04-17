package sample;

import com.hp.jipp.model.IppPacket;
import com.hp.jipp.model.Operation;
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
    public static void main(String[] args) {

        if (args.length != 2) {
            fail("Arguments [PRINTER_PATH] [FILE] are required, received: " + Arrays.asList(args));
        }

        URI uri = URI.create(args[0]);
        File inputFile = new File(args[1]);
        if (!inputFile.exists()) {
            fail("Input file " + inputFile + " does not exist");
        }

        IppPacket printRequest = new IppPacket(Operation.printJob, 123,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint"),
                        documentFormat.of("application/octet-stream")));

        IppClientTransport transport = new HttpIppClientTransport();

        System.out.println("Sending " + printRequest.prettyPrint(1200, "  "));
        try {
            IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
            IppPacketData response = transport.sendData(uri, request);
            System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fail(String message) {
        System.out.println("*** " + message);
        System.exit(1);
    }
}