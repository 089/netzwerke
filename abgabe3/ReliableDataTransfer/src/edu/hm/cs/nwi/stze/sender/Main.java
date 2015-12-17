package edu.hm.cs.nwi.stze.sender;

import edu.hm.cs.nwi.stze.libary.CatSenderSocket;

import java.io.*;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        InetAddress ia = InetAddress.getByName( "localhost" );
        int port = 4711;

        System.out.println("Sende Datei ..");

        CatSenderSocket socket = new CatSenderSocket(ia, port);
        try(InputStream in = new FileInputStream("test.txt"))
        {
            socket.send(in);
        }

        System.out.println("Done!");
    }
}
