package edu.hm.cs.nwi.stze.sender;

import edu.hm.cs.nwi.stze.libary.CatSenderSocket;

import java.io.*;
import java.net.InetAddress;

public class Main {

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
            socket.send(in);
        }

        System.out.println("Fertig!");
    }
}
