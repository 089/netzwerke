package edu.hm.cs.nwi.stze.libary;

/**
 * Repräsentiert die möglichen Zustände des {@code FileReceiver}s und des {@code FileSender}s.
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public enum ReceiverState {
    WaitForZero,
    WaitForOne
}
