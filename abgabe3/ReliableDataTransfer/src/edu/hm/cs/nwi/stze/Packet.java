package edu.hm.cs.nwi.stze;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Die Klasse Packet stellt Hilfsmethoden für den Umgang mit Paketen bereit.
 */
public final class Packet {

    // Niemand kann ein Objekt von dieser Klasse ableiten.
    private Packet() {}


    public static boolean isCorrupt(byte[] packet) {
        // TODO

        return true;
    }

    private static int extractSequenceNumber(byte[] packet) {
        // TODO

        return 0;
    }

    private static int extractLength(byte[] packet) {
        // TODO

        return 0;
    }

    private static byte[] extractBody(byte[] packet) {
        // TODO

        return null;
    }

    private static int extractChecksum(byte[] packet) {
        // TODO

        return 0;
    }

    /**
     * Überprüft die Checksumme eines Pakets mit der übergebenen Checksumme.
     *
     * @param packet Paket, das eine Checksumme beinhaltet
     * @param checksum Checksumme, die überprüft wird.
     *
     * @return Liefert true, wenn die Checksummen (Paket und Parameter) übereinstimmen
     */
    private static boolean isChecksumOK(byte[] packet, int checksum) {

        return extractChecksum(packet) == checksum;
    }

    /**
     * Erstellt ein Paket bestehend aus Header (Sequenznummer, Länge des Bodys, Checksumme über dem Body) und Body.
     *
     * @param seq Sequenznummer, üblicherweise 0 oder 1
     * @param body Inhalt des Pakets
     *
     * @return Paket in definiertem Paketformat
     */
    private static ByteBuffer createPacket(byte seq, byte[] body) {

        /*
         * Header
         *
         * allocate(x); byt: x=1, int: x=4, long: x=8
         */

        byte[] sequenceNumber = ByteBuffer.allocate(1).putInt(seq).array();

        byte[] bodyLength = ByteBuffer.allocate(4).putInt(body.length).array();

        CRC32 checksum = new CRC32();
        checksum.update(body);
        byte[] bodyChecksum = ByteBuffer.allocate(8).putLong(checksum.getValue()).array();

        /*
         * Body haben wir schon ==> Header und Body zusammenfügen
         */

        byte[] data = new byte[body.length + 1 + 4 + 8]; // Die additiven Zahlen müssen dem allokierten Speicher (siehe Header) entsprechen
        ByteBuffer packet = ByteBuffer.wrap(data);
        packet.put(sequenceNumber);
        packet.put(bodyLength);
        packet.put(bodyChecksum);
        packet.put(body);

        return packet;
    }

}
