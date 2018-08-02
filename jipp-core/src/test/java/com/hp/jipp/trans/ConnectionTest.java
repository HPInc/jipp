package com.hp.jipp.trans;

import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.IppPacket;
import com.hp.jipp.pwg.Operation;
import com.hp.jipp.pwg.Status;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static org.junit.Assert.assertEquals;

public class ConnectionTest {
    private int port = 10631;
    private String path = "/ipp/printer";
    private IppClientTransport client = new HttpIppClientTransport();
    private HttpIppServerTransport server = null;
    private IppPacketData clientRequest = null;
    private IppPacketData serverResponse = null;

    @Before
    public void setup() throws IOException {
        server = new HttpIppServerTransport(path, port) {
            @NotNull
            @Override
            public IppPacketData handle(@NotNull URI uri, @NotNull IppPacketData request) throws IOException {
                ConnectionTest.this.clientRequest = request;
                return serverResponse;
            }
        };
    }

    @After
    public void shutdown() {
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void simple() throws IOException {
        IppPacket requestPacket = new IppPacket(Operation.getPrinterAttributes, 0x123,
                groupOf(Tag.operationAttributes));

        IppPacket responsePacket = new IppPacket(Status.successfulOk, 0x123,
                groupOf(Tag.operationAttributes),
                groupOf(Tag.printerAttributes));
        serverResponse = new IppPacketData(responsePacket, null);

        IppPacketData response = client.sendData(URI.create("ipp://localhost:" + port + path),
                new IppPacketData(requestPacket, null));

        assertEquals(responsePacket, response.getPacket());
    }
}
