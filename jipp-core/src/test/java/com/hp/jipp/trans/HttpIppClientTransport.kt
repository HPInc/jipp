package com.hp.jipp.trans

import com.hp.jipp.model.IppPacket
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.DataInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * A simple HTTP-only transport for IPP.
 *
 * It is assumed that the remote server will not deliver additional data (just an IPP packet).
 */
class HttpIppClientTransport : IppClientTransport {

    @Throws(IOException::class)
    override fun sendData(uri: URI, request: IppPacketData): IppPacketData {
        val url = URL(uri.toString().replace("^ipp".toRegex(), "http"))
        println("Opening connection to $url")
        val connection = openConnection(url)

        println("Setting up connection")
        connection.requestMethod = "POST"
        connection.addRequestProperty("Content-type", "application/ipp")
        connection.doOutput = true
        connection.setChunkedStreamingMode(0)

        return DataOutputStream(connection.outputStream).use { output ->
            println("Writing packet")
            request.ippPacket.write(output)
            println("Writing Extra Data")
            request.data?.apply {
                copyTo(output)
            }
            output.flush()
        }.let {
            println("Reading the output stream")
            val rxBytes = ByteArrayOutputStream()
            connection.inputStream.apply {
                copyTo(rxBytes)
            }

            DataInputStream(ByteArrayInputStream(rxBytes.toByteArray())).use {
                println("Parsing received data")
                IppPacketData(IppPacket.parse(it))
            }
        }
    }

    /** Opens a new [HttpURLConnection] for the specified URL */
    private fun openConnection(url: URL): HttpURLConnection {
        val connection = url.openConnection() as HttpURLConnection?
                ?: throw IOException("could not open connection")
        connection.connectTimeout = CONNECT_TIMEOUT
        return connection
    }

    companion object {
        /** Timeout when connecting */
        const val CONNECT_TIMEOUT = 12 * 1000
    }
}
