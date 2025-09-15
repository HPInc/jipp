// © Copyright 2018 - 2022 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package sample;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Types;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static com.hp.jipp.encoding.Tag.printerAttributes;
import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.documentFormatSupported;
import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;

class Main {
    private final static String FORMAT_PDF = "application/pdf";
    private final static String CMD_NAME = "jprint";
    private static IppClientTransport transport;
    private final static Map<String, String> extensionTypes = new HashMap<String, String>() {{
        put("pdf", FORMAT_PDF);
        put("pclm", "application/PCLm");
        put("pwg", "image/pwg-raster");
    }};

    private final static Options options;

    static {
        Option fileOption = new Option("p", "print-job", true, "print a file");
        fileOption.setArgName("FILE");

        OptionGroup requiredOptions = new OptionGroup()
                .addOption(new Option("a", "get-attributes", false, "get printer attributes"))
                .addOption(fileOption);
        requiredOptions.setRequired(true);

        Option mimeTypeOption = new Option("m", "mime-type", true, "specify mime-type");
        mimeTypeOption.setArgName("TYPE");

        options = new Options()
                .addOption("h", "help", false, "show help")
                .addOptionGroup(requiredOptions)
                .addOption("d", "media-col-database", false, "get-attributes also queries media-col-database")
                .addOption(mimeTypeOption)
                .addOption("s", "self-signed", false, "accept self signed certificates");
    }

    public static void main(String[] args) throws IOException {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine command = parser.parse(options, args);

            if (command.hasOption("h")) {
                help();
            }

            transport = new HttpIppClientTransport(command.hasOption("s"));

            List<String> argList = command.getArgList();
            if (argList.size() != 1) {
                throw new ParseException("Must supply a single PRINTER_URL");
            }
            URI path;
            try {
                 path = URI.create(argList.get(0));
            } catch (IllegalArgumentException e) {
                throw new ParseException("Failed to parse PRINTER_URL");
            }

            if (command.hasOption("a")) {
                getAttributes(path, command);
            } else if (command.hasOption("p")) {
                print(path, command);
            } else {
                throw new ParseException("No command");
            }
        } catch (ParseException e) {
            System.err.println("\n" + e.getMessage());
            help();
        }
    }

    private static void getAttributes(URI uri, CommandLine command) throws IOException {
        // Query for supported document formats
        Attribute<String> requested;
        if (command.hasOption("d")) {
            requested = requestedAttributes.of(Types.mediaColDatabase.getName(), "all");
        } else {
            requested = requestedAttributes.of("all");
        }

        IppPacket attributeRequest = IppPacket.getPrinterAttributes(uri)
                .putOperationAttributes(requestingUserName.of(CMD_NAME), requested)
                .build();

        System.out.println("\nSending " + attributeRequest.prettyPrint(100, "  "));
        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("\nReceived: " + response.getPacket().prettyPrint(100, "  "));
    }

    private static void print(URI uri, CommandLine command) throws IOException, ParseException {
        String fileName = command.getOptionValue("p"); // new File(
        System.out.println("File is " + fileName);

        String format = command.getOptionValue("m");
        if (format == null) {
            if (fileName.contains(".")) {
                format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
            }
        }

        File inputFile = new File(fileName);
        if (!inputFile.isFile()) {
            throw new ParseException("Cannot read " + fileName);
        }

        // Query for supported document formats
        IppPacket attributeRequest = IppPacket.getPrinterAttributes(uri)
                .putOperationAttributes(
                        requestingUserName.of(CMD_NAME),
                        requestedAttributes.of(documentFormatSupported.getName()))
                .build();

        System.out.println("\nSending " + attributeRequest.prettyPrint(100, "  "));
        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("\nReceived: " + response.getPacket().prettyPrint(100, "  "));

        // Make sure the format is supported
        List<String> formats = response.getPacket().getStrings(printerAttributes, documentFormatSupported);
        if (!formats.contains(format)) {
            throw new ParseException(format + " format not supported by printer in " + formats);
        }

        // Deliver the print request
        IppPacket printRequest = IppPacket.printJob(uri)
                .putOperationAttributes(
                        requestingUserName.of(CMD_NAME),
                        documentFormat.of(format))
                .build();

        System.out.println("\nSending " + printRequest.prettyPrint(100, "  "));
        request = new IppPacketData(printRequest, new FileInputStream(inputFile));
        response = transport.sendData(uri, request);
        System.out.println("\nReceived: " + response.getPacket().prettyPrint(100, "  "));
    }

    private static void help() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(120);
        helpFormatter.setOptionComparator(null);
        helpFormatter.printHelp(CMD_NAME + " [OPTIONS] PRINTER_URL\nOPTIONS:", options, true);
        System.exit(0);
    }
}
