package edu.hm.cs.nwi.stze.receiver;

import edu.hm.cs.nwi.stze.libary.CatException;
import edu.hm.cs.nwi.stze.libary.CatPacketError;
import edu.hm.cs.nwi.stze.libary.CatReceiverSocket;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Empfängt eine Datei.
 */
public class Main {

    private static String tempFilename = "tmp.dat";

    public static void main(String[] args) throws IOException, CatException {
        System.out.println("Start Receiver ..");

        /*
         * Wenn Fehler gewünscht werden, wie in der Aufgabe 7.1.e gefordert, kann das
         * mittels folgender Map eingestellt werden.
         */
        Map<CatPacketError, Double> errorsForSimulation = new HashMap<>();
        if(args.length == 3) {
            errorsForSimulation.put(CatPacketError.BIT, Double.valueOf(args[0]));
            errorsForSimulation.put(CatPacketError.DISCARD, Double.valueOf(args[1]));
            errorsForSimulation.put(CatPacketError.DUPLICATE, Double.valueOf(args[2]));
        }

        try(CatReceiverSocket server = new CatReceiverSocket(4711);
            FileOutputStream fos = new FileOutputStream(tempFilename);
        ) {
            // Datei schreiben und ggf. gewünschte Fehler einbauen
            server.receive(body -> { fos.write(body); }, errorsForSimulation);

            System.out.printf("Endzeit [ms]: %d\n", System.currentTimeMillis());

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

    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }
}