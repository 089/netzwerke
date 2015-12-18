package edu.hm.cs.nwi.stze.libary;

/**
 *
 *
 * Praktikum Netzwerke I, Gruppe 02
 *
 * @author <a href="Stieglit@hm.edu">Kevin Stieglitz</a>, <a href="Zell@hm.edu">Martin Zell</a>
 * @version 1.0
 */
public class CatException extends Exception {
    public CatException(String message) {
        super(message);
    }

    public CatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
