package com.hp.jipp.trans;

import com.hp.jipp.model.IppPacket;
import com.hp.jipp.model.IppPacketKt;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;

import java.net.InetSocketAddress;

/** A sample IPP server over the HTTP transport */
abstract public class HttpIppServerTransport implements IppServerTransport {

    private HttpServer server;

    public HttpIppServerTransport(String path, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, new IppHandler());
        server.setExecutor(null);
        server.start();
    }

    /** Closes the server */
    public void close() {
        server.stop(0);
    }

    private class IppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                InputStream inputStream = exchange.getRequestBody();
                IppPacket receivePacket = IppPacket.parse(inputStream);
                IppPacketData data = new IppPacketData(receivePacket, inputStream);
                IppPacketData response = HttpIppServerTransport.this.handle(exchange.getRequestURI(), data);
                DataOutputStream output = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()));
                IppPacketKt.writePacket(output, response.getIppPacket());
                InputStream extraData = response.getData();

                /* If response data is present, queue that also */
                if (extraData != null) {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int bytesRead = extraData.read(buffer);
                        if (bytesRead == -1) {
                            break;
                        }
                        output.write(buffer, 0, bytesRead);
                    }
                    extraData.close();
                }
                exchange.sendResponseHeaders(200, 0);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
