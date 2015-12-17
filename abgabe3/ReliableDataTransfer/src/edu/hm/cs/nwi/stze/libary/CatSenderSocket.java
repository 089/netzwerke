package edu.hm.cs.nwi.stze.libary;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Created by Kevin on 17.12.2015.
 *
 * @author stieglit
 * @version 1.0
 */
public class CatSenderSocket {
    private InetAddress inetAddress;
    private int port;
    private CatSeqNumber seqNumber;

    /**
     * Grundklasse zum versenden über das Cat-Protocol :)
     *
     * @param inetAddress
     * @param port
     */
    public CatSenderSocket(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.seqNumber = CatSeqNumber.ACK_ZERO;
    }

    /**
     * Sendet einen Stream über UDP
     * Unterteil den Stream in mehrere Pakete
     *
     * @param stream InputStream
     * @throws IOException Wenn der Stream nicht gelesen werden kann
     */
    public void send(InputStream stream) throws IOException {
        try(DatagramSocket toSocket = new DatagramSocket()) {
            byte[] buf = new byte[CatPacket.MAX_BODY_SIZE];
            int len = 0;
            while ((len = stream.read(buf)) != -1) {
                byte[] buffer = Arrays.copyOf(buf, len);
                DatagramPacket rdtPacket = createRdtPackage(buffer);

                toSocket.send(rdtPacket);
            }
        }
    }

    /**
     * Erstellt ein rdt Packet für die Übertragung
     *
     * @param buffer Paketinhalt
     * @return
     */
    private DatagramPacket createRdtPackage(byte[] buffer) {
        CatPacket catPacket = new CatPacket(this.seqNumber, buffer);
        byte[] data = catPacket.toByte();
        DatagramPacket packet = new DatagramPacket( data, data.length, this.inetAddress, this.port);

        return packet;
    }
}
