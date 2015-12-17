package edu.hm.cs.nwi.stze.receiver;

import edu.hm.cs.nwi.stze.libary.CatException;
import edu.hm.cs.nwi.stze.libary.CatReceiverSocket;

import java.io.IOException;
import java.nio.charset.Charset;

public class Main {

    public static void main(String[] args) throws IOException, CatException {
        System.out.println("Start Receiver ..");

        try(CatReceiverSocket server = new CatReceiverSocket(4711)) {
            server.receive(body -> {
                String out = decodeUTF8(body);
                System.out.println(out);
            });
        }
    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }

    static byte[] encodeUTF8(String string) {
        return string.getBytes(UTF8_CHARSET);
    }
}