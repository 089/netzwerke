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

    /**
     *
     * @param listener Listener welche die empfangenen Bits ausgibt
     * @throws IOException
     * @throws CatException
     */
    public void receive(CatReceiverListener listener) throws IOException {
        while (true) {
            // Auf Anfrage warten

            byte[] tmp = new byte[CatPacket.MAX_PACKAGE_SIZE];
            CatPacket catPacket = null;

            DatagramPacket packet = new DatagramPacket( tmp, tmp.length );
            socket.receive( packet );
            remoteInetAddress = packet.getAddress();
            remotePort = packet.getPort();

            //Wenn das Paket nicht richtig gelesen werden konnte => Auf neues warten
            try {
                catPacket = CatPacket.fromDatagramPacket(packet);
            } catch (CatException e) {
                continue;
            }

            if(checkPacket(catPacket)) {

                //Letztes Paket?
                if(isLastPackage(catPacket))
                    break;

                listener.receiveInputByte(catPacket.getBody());
            }
        }
    }

    /**
     * Überprüft ob es sich um das letzte Paket handelt
     *
     * @param packet
     * @return True wenn letztes Paket
     */
    private boolean isLastPackage(CatPacket packet) {
        if(packet.getLength() == 2) {
            byte[] tmp = packet.getBody();
            if(tmp[0] == 0xf
                    && tmp[1] == 0xf)
                return true;
        }
        return false;
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
                return true;
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
                return true;
            }
        }

        return false;
    }

    /**
     * Sendet ein ACK an den Client
     *
     * @param seqNumber
     * @throws IOException
     */
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