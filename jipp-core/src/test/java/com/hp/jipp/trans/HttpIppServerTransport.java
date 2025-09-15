// © Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.trans;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                IppInputStream inputStream = new IppInputStream(exchange.getRequestBody());
                IppPacketData data = new IppPacketData(inputStream.readPacket(), inputStream);
                IppPacketData response = HttpIppServerTransport.this.handle(exchange.getRequestURI(), data);
                exchange.sendResponseHeaders(200, 0);
                try (OutputStream output = exchange.getResponseBody()) {
                    new IppOutputStream(output).write(response.getPacket());

                    /* If response data is present, queue that also */
                    InputStream extraData = response.getData();
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
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
