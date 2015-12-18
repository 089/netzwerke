package edu.hm.cs.nwi.stze.sender;

import edu.hm.cs.nwi.stze.libary.CatSenderSocket;

import java.io.*;
import java.net.InetAddress;

/**
 * Versendet eine Datei per UDP an ein Ziel.
 *
 * Standardmäßig lautet die Datei test.txt (alternativ: Parameter 1)
 * und das Ziel localhost (alternativ: Parameter 2).
 *
 * Aufruf
 *
 *
 */
public class Main {

    /**
     *
     * @param args 1. Datei und 2. Ziel
     * @throws IOException Fehler bei Stream
     */
    public static void main(String[] args) throws IOException {

        InetAddress ia;
        String filename;
        int port = 4711;

        if(args.length == 2) {
            filename = args[0];
            ia = InetAddress.getByName(args[1]);
        } else {
            filename = "test.txt";
            ia = InetAddress.getByName( "localhost" );
        }

        System.out.println("Sende Datei ..");

        CatSenderSocket socket = new CatSenderSocket(ia, port);
        try(InputStream in = new FileInputStream(filename))
        {
            socket.send(in, filename);
        }

        System.out.println("Fertig!");
    }
}
