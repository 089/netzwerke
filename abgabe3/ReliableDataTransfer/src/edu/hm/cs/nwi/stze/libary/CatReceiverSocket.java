package edu.hm.cs.nwi.stze.libary;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Kevin on 17.12.2015.
 *
 * @author stieglit
 * @version 1.0
 */
public class CatReceiverSocket implements AutoCloseable {

    DatagramSocket socket;
    ReceiverState state;

    InetAddress remoteInetAddress;
    int remotePort;

    /**
     * Konstruktor
     *
     * @param port Port auf dem gehört wird
     */
    public CatReceiverSocket(int port) throws SocketException {
        this.socket = new DatagramSocket( port );
        this.state = ReceiverState.WaitForZero;
    }

    public void receive(CatReceiverListener listener) throws IOException, CatException {
        while (true) {
            // Auf Anfrage warten

            byte[] tmp = new byte[CatPacket.MAX_PACKAGE_SIZE];

            DatagramPacket packet = new DatagramPacket( tmp, tmp.length );
            socket.receive( packet );
            remoteInetAddress = packet.getAddress();
            remotePort = packet.getPort();

            CatPacket catPacket = CatPacket.fromDatagramPacket(packet);
            if(checkPacket(catPacket)) {
                listener.receiveInputByte(catPacket.getBody());
            }
        }
    }

    /**
     * Pberprüft das Paket und handelt entsprechend
     *
     * @param packet
     * @return True, wenn es das richtige Paket ist
     */
    private boolean checkPacket(CatPacket packet) throws IOException {
        // State 0
        if(state == ReceiverState.WaitForZero) {

            //Checksumme nicht ok oder Seq = 1
            if(!packet.isChecksumOK()
                    || packet.getSeqNumber() == CatSeqNumber.ACK_ONE) {
                udtSend(CatSeqNumber.ACK_ONE);
                return false;
            } else {
                udtSend(CatSeqNumber.ACK_ZERO);
                this.state = ReceiverState.WaitForOne;
                return  true;
            }

        //State 1
        } else if(state == ReceiverState.WaitForOne) {
            //Checksumme nicht ok oder Seq = 0
            if(!packet.isChecksumOK()
                    || packet.getSeqNumber() == CatSeqNumber.ACK_ZERO) {
                udtSend(CatSeqNumber.ACK_ZERO);
                return false;
            } else {
                udtSend(CatSeqNumber.ACK_ONE);
                this.state = ReceiverState.WaitForZero;
                return  true;
            }
        }

        return false;
    }

    private void udtSend(CatSeqNumber seqNumber) throws IOException {
        CatPacket catPacket = new CatPacket(seqNumber, new byte[0]);

        byte[] data = catPacket.toByte();
        DatagramPacket packet = new DatagramPacket( data, data.length, this.remoteInetAddress, this.remotePort);

        try(DatagramSocket toSocket = new DatagramSocket()) {
            toSocket.send(packet);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
