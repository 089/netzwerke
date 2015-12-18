package edu.hm.cs.nwi.stze.libary;

/**
 * Das Interface CatReceiverListener wird benötigt, um in der Klasse {@code CatReceiverSocket},
 * Methode {@code receive} auf den in der Main übergebenen body zugreifen zu können.
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public interface CatReceiverListener {
    void receiveInputByte(byte[] input);
}
