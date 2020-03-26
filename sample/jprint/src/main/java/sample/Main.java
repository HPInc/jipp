package sample;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Types;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.hp.jipp.encoding.Tag.printerAttributes;
import static com.hp.jipp.model.Types.*;

class Main {
    private final static String FORMAT_PDF = "application/pdf";
    private final static String CMD_NAME = "jprint";

    private final static IppClientTransport transport = new HttpIppClientTransport();

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        options.addOption("h", "help", false, "show help");
        OptionGroup commandGroup = new OptionGroup();
        commandGroup.addOption(new Option("a", "get-attributes", false, "get printer attributes"));
        commandGroup.addOption(new Option("p", "print-job", true, "print a file"));
        options.addOptionGroup(commandGroup);
        options.addOption("d", "media-col-database", false, "get-attributes also queries media-col-database");
        options.addOption("m", "mime-type", true, "print-job mime-type (default=" + FORMAT_PDF + ")");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine command = parser.parse(options, args);

            if (command.hasOption("h")) {
                help(options);
            }

            List<String> argList = command.getArgList();
            if (argList.size() != 1) {
                throw new ParseException("Must supply a single PRINTER_PATH");
            }
            URI path;
            try {
                 path = URI.create(argList.get(0));
            } catch (IllegalArgumentException e) {
                throw new ParseException("Failed to parse PRINTER_PATH");
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
            help(options);
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
                .addOperationAttributes(requestingUserName.of(CMD_NAME), requested)
                .build();

        System.out.println("Sending " + attributeRequest.prettyPrint(100, "  "));
        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
    }

    private static void print(URI uri, CommandLine command) throws IOException, ParseException {
        String format = command.getOptionValue("m");
        if (format == null) {
            format = FORMAT_PDF;
        }

        File inputFile = new File(command.getOptionValue("p"));
        System.out.println("File is " + inputFile);

        // Query for supported document formats
        IppPacket attributeRequest = IppPacket.getPrinterAttributes(uri)
                .addOperationAttributes(
                        requestingUserName.of(CMD_NAME),
                        requestedAttributes.of(documentFormatSupported.getName()))
                .build();

        System.out.println("Sending " + attributeRequest.prettyPrint(100, "  "));
        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));

        // Make sure the format is supported
        List<String> formats = response.getPacket().getStrings(printerAttributes, documentFormatSupported);
        if (!formats.contains(format)) {
            throw new ParseException(format + " format not supported by printer in " + formats);
        }

        // Deliver the print request
        IppPacket printRequest = IppPacket.printJob(uri)
                .addOperationAttributes(
                        requestingUserName.of("jprint"),
                        documentFormat.of(format))
                .build();

        System.out.println("Sending " + printRequest.prettyPrint(100, "  "));
        request = new IppPacketData(printRequest, new FileInputStream(inputFile));
        response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
    }

    private static void help(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(120);
        helpFormatter.setOptionComparator(null);
        helpFormatter.printHelp(CMD_NAME, options, true);
        System.exit(0);
    }
}
