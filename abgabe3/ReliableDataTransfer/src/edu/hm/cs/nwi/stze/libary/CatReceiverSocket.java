package edu.hm.cs.nwi.stze.libary;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

/**
 * CatReceiverSocket empfängt die in viele kleinere Pakete unterteilte Datei mittels
 * UDP. Es sendet die entsprechenden ACK um dem Sender den Empfang von Paketen zu bestätigen.
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public class CatReceiverSocket implements AutoCloseable {

    DatagramSocket socket;
    ReceiverState state;

    InetAddress remoteInetAddress;
    int remotePort;

    String filename;

    /**
     * Konstruktor
     *
     * @param port Port auf dem gehört wird
     * @throws SocketException Fehler mit dem Socket
     */
    public CatReceiverSocket(int port) throws SocketException {
        this.socket = new DatagramSocket( port );
        this.state = ReceiverState.WaitForZero;
    }

    /**
     * Emprängt die einzelnen Pakete und reicht sie nach oben durch.
     *
     * @param listener Listener welche die empfangenen Bits ausgibt
     * @param errors gewünschte Fehler die absichtlich eingebaut werden um die Funktionalität des Protokolls zu testen
     * @throws IOException Fehler mit Streams
     */
    public void receive(CatReceiverListener listener, Map<CatPacketError, Double> errors) throws IOException {

        int resendPacket; // Wie oft soll das selbe Packet empfangen werden?

        receiving:
        while (true) {
            // Auf Anfrage warten

            byte[] tmp = new byte[CatPacket.MAX_PACKET_SIZE];
            CatPacket catPacket = null;

            DatagramPacket packet = new DatagramPacket( tmp, tmp.length );
            socket.receive( packet );
            remoteInetAddress = packet.getAddress();
            remotePort = packet.getPort();

            // Absichtlich einen Fehler eingebauen mit der Eintrittswahrscheinlichkeit:
            double probabilityOfOccurrence = Math.random();

            // Wir produzieren einen (einzelnen) Bitfehler
            if(errors.containsKey(CatPacketError.BIT) && probabilityOfOccurrence <= errors.get(CatPacketError.BIT)) {
                byte[] data = packet.getData();

                // Wir invertieren das letzte Bit im ersten Byte des Bodys mittels XOR
                data[CatPacket.HEADER_SIZE] = (byte) (data[CatPacket.HEADER_SIZE] ^ 0b00000001);

                packet.setData(data);
            }

            // ABSICHTLICHER FEHLER: Paket wird verworfen, weil der Rest der Schleife nicht mehr abgearbeitet wird.
            if(errors.containsKey(CatPacketError.DISCARD) && probabilityOfOccurrence <= errors.get(CatPacketError.DISCARD)) {
                continue;
            }

            // ABSICHTLICHER FEHLER: Paket wird verworfen, weil der Rest der Schleife nicht mehr abgearbeitet wird.
            if(errors.containsKey(CatPacketError.DISCARD) && probabilityOfOccurrence <= errors.get(CatPacketError.DISCARD)) {
                continue;
            }

            // ABSICHTLICHER FEHLER: Paket wird dupliziert
            if(errors.containsKey(CatPacketError.DUPLICATE) && probabilityOfOccurrence <= errors.get(CatPacketError.DUPLICATE)) {
                resendPacket = 2;
            } else {
                resendPacket = 1;
            }

            // evtl. bei Fehler mehrfach "empfangen"
            for(int i = 0; i < resendPacket; i++) {
                //Wenn das Paket nicht richtig gelesen werden konnte => Auf neues warten
                try {
                    catPacket = CatPacket.fromDatagramPacket(packet);
                } catch (CatException e) {
                    continue;
                }

                if (checkPacket(catPacket)) {

                    //Letztes Paket?
                    if (isLastPackage(catPacket)) {
                        break receiving; // wir müssen die while Schleife verlassen, nicht das for!
                    }

                    listener.receiveInputByte(catPacket.getBody());
                }
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
        if(packet.getLength() < packet.MAX_BODY_SIZE) {
            byte[] tmp = packet.getBody();

            // Im letzten Paket steht der Dateiname umgeben von jeweils zwei 0xf
            if(tmp[0] == 0xf
                    && tmp[1] == 0xf
                    && tmp[tmp.length-2] == 0xf
                    && tmp[tmp.length-1] == 0xf
                    ) {

                // extract filename
                this.filename = new String(tmp, 2, tmp.length-4);

                System.out.println(this.filename);

                return true;
            }
        }
        return false;
    }

    public String getFilename() {
        return filename;
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
