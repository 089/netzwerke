package edu.hm.cs.nwi.stze.libary;

/**
 * Created by Kevin on 17.12.2015.
 *
 * @author stieglit
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
