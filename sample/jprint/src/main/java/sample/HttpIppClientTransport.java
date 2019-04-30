package sample;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.jetbrains.annotations.NotNull;

/**
 * A simple HTTP-only transport for IPP.
 *
 * It is assumed that the remote server will not deliver additional data (just an IPP packet).
 */
class HttpIppClientTransport implements IppClientTransport {
    @Override
    @NotNull
    public IppPacketData sendData(@NotNull URI uri, @NotNull IppPacketData request) throws IOException {
        URL url = new URL(uri.toString().replaceAll("^ipp", "http"));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(6 * 1000);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-type", "application/ipp");
        connection.setChunkedStreamingMode(0);
        connection.setDoOutput(true);

        // Copy IppPacket to the output stream
        try (OutputStream output = connection.getOutputStream()) {
            request.getPacket().write(new DataOutputStream(output));
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
}
