package edu.hm.cs.nwi.stze.libary;

/**
 * Repräsentiert die alternierende Sequenznummer der Pakete.
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
enum CatSeqNumber {
    ACK_ZERO(0x00),
    ACK_ONE(0x01);

    private final byte seqNumber;

    CatSeqNumber(int seqNumber) {
        this.seqNumber = (byte) seqNumber;
    }

    public byte getSeqNumber() {
        return this.seqNumber;
    }
}
