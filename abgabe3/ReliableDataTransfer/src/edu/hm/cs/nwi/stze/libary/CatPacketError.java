package edu.hm.cs.nwi.stze.libary;

/**
 * Es können wie in der Aufgabenstellung (vgl. 7.1.e) beschrieben, verschiedene Fehler passieren.
 * Diese werden durch dieses Enum repräsentiert.
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public enum CatPacketError {
    BIT,        // "a. zufällig mit einer konfigurierbaren Wahrscheinlichkeit einen Bitfehler im Paket verursacht"
    DISCARD,    // "b. zufällig mit einer konfigurierbaren Wahrscheinlichkeit ein Paket verwirft"
    DUPLICATE   // "c. zufällig mit einer konfigurierbaren Wahrscheinlichkeit ein Paket dupliziert"
}
