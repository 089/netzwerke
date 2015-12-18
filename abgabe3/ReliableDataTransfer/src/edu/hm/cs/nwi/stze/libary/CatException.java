package edu.hm.cs.nwi.stze.libary;

/**
 * Created by Kevin on 17.12.2015.
 *
 * @author stieglit
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
