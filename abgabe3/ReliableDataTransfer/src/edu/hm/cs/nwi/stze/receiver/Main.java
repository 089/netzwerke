package edu.hm.cs.nwi.stze.receiver;

import edu.hm.cs.nwi.stze.libary.CatException;
import edu.hm.cs.nwi.stze.libary.CatReceiverSocket;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Empfängt eine Datei.
 */
public class Main {

    private static String tempFilename = "tmp.dat";

    public static void main(String[] args) throws IOException, CatException {
        System.out.println("Start Receiver ..");

        try(CatReceiverSocket server = new CatReceiverSocket(4711);
            FileOutputStream fos = new FileOutputStream(tempFilename);
        ) {
            server.receive(body -> {
                // Datei
                fos.write(body);
            });

            String filename = server.getFilename();

            if(filename != null && !filename.isEmpty()) {
                System.out.printf("Datei %s empfangen.\n", filename);
                File oldFile = new File(tempFilename);
                File newFile = new File(server.getFilename());
                oldFile.renameTo(newFile);
            } else {
                filename = tempFilename;
                System.out.printf("Datei %s empfangen.\n", filename);
            }
        }

        //System.out.println("Finish!");
    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }
}