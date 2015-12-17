package edu.hm.cs.nwi.stze.libary;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * Die Klasse Packet stellt Hilfsmethoden für den Umgang mit Paketen bereit.
 * Kann nur in der jeweiligen Package aufgerufen werden
 */
final class CatPacket {
    /**
     * Max Package Size für DatagramPacket
     */
    public static final int MAX_PACKAGE_SIZE = 1024;

    /**
     * Size for protocol header
     */
    public static final int HEADER_SIZE = 1 + 4 + 8;

    /**
     * Max Size for Body
     */
    public static final int MAX_BODY_SIZE = MAX_PACKAGE_SIZE - HEADER_SIZE;

    private CatSeqNumber seqNumber;
    private long checksum;
    private int length;
    private byte[] body;

    /**
     * Generiert ein CatPacket
     *
     * @param seqNumber Sequenznummer, üblicherweise 0 oder 1
     * @param body Inhalt des Pakets
     */
    public CatPacket(CatSeqNumber seqNumber, byte[] body) {
        this.seqNumber = seqNumber;
        this.length = body.length;
        this.body = body;
        this.checksum = generateChecksum(body);
    }

    /**
     * Generiert ein CatPacket mit eigener Checksum
     *
     * @param seqNumber Sequenznummer, üblicherweise 0 oder 1
     * @param checksum Checksumme des body
     * @param body Inhalt des Pakets
     */
    private CatPacket(CatSeqNumber seqNumber, long checksum, byte[] body) {
        this.seqNumber = seqNumber;
        this.length = body.length;
        this.body = body;
        this.checksum = checksum;
    }

    /**
     * Generiert eine CRC32 Checksum über die Daten
     *
     * @param body Daten über die die CHK generiert wird
     * @return Checksumme
     */
    private static long generateChecksum(byte[] body) {
        CRC32 checksum = new CRC32();
        checksum.update(body);

        return checksum.getValue();
    }

    /**
     * Überprüft die Checksumme eines Pakets mit der übergebenen Checksumme.
     *
     * @return Liefert true, wenn die Checksummen (Paket und Parameter) übereinstimmen
     */
    public boolean isChecksumOK() {
        return generateChecksum(this.body) == checksum;
    }

    /**
     * Erstellt ein Paket bestehend aus Header (Sequenznummer, Länge des Bodys, Checksumme über dem Body) und Body.
     *
     * @return Paket in definiertem Paketformat
     */
    public byte[] toByte() {
        /*
         * Header
         *
         * allocate(x); byt: x=1, int: x=4, long: x=8
         */

        byte[] sequenceNumber = ByteBuffer.allocate(1).put(this.seqNumber.getSeqNumber()).array();
        byte[] bodyLength = ByteBuffer.allocate(4).putInt(this.length).array();
        byte[] bodyChecksum = ByteBuffer.allocate(8).putLong(this.checksum).array();

        /*
         * Body haben wir schon ==> Header und Body zusammenfügen
         */

        byte[] data = new byte[body.length + HEADER_SIZE]; // Die additiven Zahlen müssen dem allokierten Speicher (siehe Header) entsprechen

        //Erstellt das Packet
        ByteBuffer packet = ByteBuffer.wrap(data);
        packet.put(sequenceNumber);
        packet.put(bodyLength);
        packet.put(bodyChecksum);
        packet.put(this.body);

        return packet.array();
    }

    /**
     * Gibt den Body zurück
     *
     * @return
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Gibt die Länge des Body zurück
     * @return
     */
    public int getLength() {
        return length;
    }

    /**
     * Gibt die Sequenznummer zurück
     * @return
     */
    public CatSeqNumber getSeqNumber() {
        return seqNumber;
    }

    /**
     * Generiert ein CatPacket von einem Datagram aus
     *
     * @throws CatException Bei Paketfehler
     * @param packet übergebens Paket
     * @return Neues CatPacket
     */
    public static CatPacket fromDatagramPacket(DatagramPacket packet) throws CatException {
        //Packet Size überprüfen
        if(packet.getLength() < HEADER_SIZE)
            throw new CatException("Packet size is to small");

        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());

        //Get Seq
        byte seq = buffer.get(0);
        CatSeqNumber seqNumber = CatSeqNumber.values()[seq];

        //Get Length
        int length = buffer.getInt(1);

        //Get Checksum
        long checksum = buffer.getLong(5);
        byte [] body;

        try {
            body = Arrays.copyOfRange(buffer.array(), HEADER_SIZE, HEADER_SIZE + length);
        } catch (IndexOutOfBoundsException e) {
            throw new CatException("Packet-Length is invalid");
        }

        return new CatPacket(seqNumber, checksum, body);
    }
}
