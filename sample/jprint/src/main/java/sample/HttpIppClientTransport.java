// Copyright 2018 - 2022 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package sample;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.encoding.IppOutputStream;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jetbrains.annotations.NotNull;

/**
 * A simple HTTP/HTTPS transport for IPP.
 *
 * It is assumed that the remote server will not deliver additional data (just an IPP packet).
 */
class HttpIppClientTransport implements IppClientTransport {
    private static final String SSL_PROTOCOL = "TLSv1.2";

    private static final TrustManager[] TRUST_ALL_CERTS;
    private static final HostnameVerifier ALL_HOSTS_VALID;
    static {
        TRUST_ALL_CERTS = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        ALL_HOSTS_VALID = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    private final SSLContext sslContext;
	private final boolean acceptSelfSignedCerts;

    HttpIppClientTransport(boolean acceptSelfSignedCerts) {
        this.acceptSelfSignedCerts = acceptSelfSignedCerts;
        this.sslContext = createSSLContext();
    }

    /**
     * @param acceptSelfSignedCerts If true, auto-accept self-signed HTTPS certificates. Real implementations should
     * implement a Trust-On-First-Use approach to minimize the potential for MITM attacks.
     */
    HttpIppClientTransport() {
        this(false);
    }

    @Override
    @NotNull
    public IppPacketData sendData(@NotNull URI uri, @NotNull IppPacketData request) throws IOException {
        HttpURLConnection connection = createURLConnection(uri);
        connection.setConnectTimeout(6 * 1000);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-type", "application/ipp");
        connection.setChunkedStreamingMode(0);
        connection.setDoOutput(true);

        // Copy IppPacket to the output stream
        try (IppOutputStream output = new IppOutputStream(connection.getOutputStream())) {
            output.write(request.getPacket());
            InputStream extraData = request.getData();
            if (extraData != null) {
                copy(extraData, output);
                extraData.close();
            }
        }

        // Read the response from the input stream
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
        try (InputStream response = connection.getInputStream()) {
            copy(response, responseBytes);
        }

        // Parse it back into an IPP packet
        IppInputStream responseInput = new IppInputStream(new ByteArrayInputStream(responseBytes.toByteArray()));
        return new IppPacketData(responseInput.readPacket(), responseInput);
    }

    private void copy(InputStream data, OutputStream output) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int readAmount = data.read(buffer);
        while (readAmount != -1) {
            output.write(buffer, 0, readAmount);
            readAmount = data.read(buffer);
        }
    }

    private HttpURLConnection createURLConnection(URI uri) throws IOException {
        URL url = new URL(uri.toString().replaceAll("^ipp", "http"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (acceptSelfSignedCerts && connection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
		    httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		    httpsConnection.setHostnameVerifier(ALL_HOSTS_VALID);
        }
        return connection;
    }

    private SSLContext createSSLContext() {
        try {
            if (!acceptSelfSignedCerts) {
                return SSLContext.getDefault();
            }

            SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
