package edu.hm.cs.nwi.stze.libary;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;

/**
 *
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public class CatSenderSocket {
    /**
     * Port for ACKs
     */
    private static final int ACK_PORT = 19112;

    /**
     * Timeout for Datagram Socket
     */
    private static final int SO_TIMEOUT = 500;

    private InetAddress inetAddress;
    private int port;
    private ReceiverState receiverState;

    /**
     * Grundklasse zum Versenden über das Cat-Protocol :)
     *
     * @param inetAddress IP-Adresse oder Hostname
     * @param port Port
     */
    public CatSenderSocket(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
    }

    /**
     * Sendet einen Stream über UDP
     * Unterteil den Stream in mehrere Pakete einer bestimmten Größe und Formats {@code CatPacket}.
     *
     * @param stream InputStream
     * @throws IOException Wenn der Stream nicht gelesen werden kann
     */
    public void send(InputStream stream) throws IOException {
        this.receiverState = ReceiverState.WaitForZero;

        try(DatagramSocket socket = new DatagramSocket(ACK_PORT)) {
            socket.setSoTimeout(SO_TIMEOUT);

            byte[] buf = new byte[CatPacket.MAX_BODY_SIZE];
            int len = 0;
            while ((len = stream.read(buf)) != -1) {
                byte[] buffer = Arrays.copyOf(buf, len);

                sendPacket(buffer, socket);
            }

            //Sende ein 0xf 0xf zum schließen am Ende
            sendPacket(new byte[] {0xf, 0xf}, socket);
        }
    }

    /**
     * Sendet die einzelnen Bytes bis das korrekte ACK kommt
     *
     * @param buffer Zu sendende Daten
     * @param socket Socket
     * @throws IOException
     */
    private void sendPacket(byte[] buffer, DatagramSocket socket) throws IOException {
        DatagramPacket rdtPacket = createRdtPacket(buffer);

        //Solange wiederholen bis ACK ok ist.
        do {
            socket.send(rdtPacket);
        }
        while(!checkACK(socket));
    }

    /**
     * Check das DatagramSocket ob das korrekte ACK kommt
     *
     * @param socket
     * @return True, wenn das korrekte ACK empfangen wurde
     * @throws IOException
     */
    private boolean checkACK(DatagramSocket socket) throws IOException {
        CatPacket catPacket;
        byte[] tmp = new byte[CatPacket.HEADER_SIZE];
        DatagramPacket packet = new DatagramPacket( tmp, tmp.length );

        //Receive Package
        try {
            socket.receive( packet );
        } catch (SocketTimeoutException e) {
            //Timeout for package
            return false;
        }

        //Get Package Info
        try {
            catPacket = CatPacket.fromDatagramPacket(packet);
        } catch (CatException e) {
            //Corrup package
            return false;
        }

        if(receiverState == ReceiverState.WaitForZero) {
            if(catPacket.getSeqNumber() == CatSeqNumber.ACK_ZERO
                    && catPacket.isChecksumOK()) {
                receiverState = ReceiverState.WaitForOne;
                return true;
            }
        } else if(receiverState == ReceiverState.WaitForOne) {

            if(catPacket.getSeqNumber() == CatSeqNumber.ACK_ONE
                    && catPacket.isChecksumOK()) {
                receiverState = ReceiverState.WaitForZero;
                return true;
            }
        }

        return false;
    }

    /**
     * Erstellt ein Packet mit dem definierten Format ({@code CatPacket}) für die Übertragung
     *
     * @param buffer Paketinhalt
     * @return Paket vom Typ {@code DatagramPacket} mit dem Format {@code CatPacket}
     */
    private DatagramPacket createRdtPacket(byte[] buffer) {
        CatSeqNumber seqNumber = CatSeqNumber.ACK_ZERO;

        if(this.receiverState == ReceiverState.WaitForOne)
            seqNumber = CatSeqNumber.ACK_ONE;

        CatPacket catPacket = new CatPacket(seqNumber, buffer);
        byte[] data = catPacket.toByte();
        DatagramPacket packet = new DatagramPacket(data, data.length, this.inetAddress, this.port);

        return packet;
    }
}
