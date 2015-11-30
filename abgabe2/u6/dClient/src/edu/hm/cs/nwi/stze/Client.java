package edu.hm.cs.nwi.stze;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends UDP packets during {@code transmissionTime} to a server.
 *
 * Packets consists of a header (8 Byte {@code packetID}, 8 Byte {@code sendTime})
 * and a body (around 1400 Bytes of data).
 *
 * @author: Kevin Stieglitz, Martin Zell
 * @version: 0.9
 */

public class Client {

    private static long N = 5_000_000; // delay step
    private static long k = 250; // delay in millis

    private static int serverPort = 4711;
    private static int transmissionTime = 30; // in seconds

    //#####################################################################
    // Do not change the following parameters!
    //#####################################################################

    // String with 1400 Byte of data (UTF-8!)
    private static String bodyString = "Eigenschaften des UDP\n" +
            "UDP ist ein verbindungsloses, nicht-zuverlässiges und ungesichertes wie auch ungeschütztes " +
            "Übertragungsprotokoll. Das bedeutet, es gibt keine Garantie, dass ein einmal gesendetes Paket auch " +
            "ankommt, dass Pakete in der gleichen Reihenfolge ankommen, in der sie gesendet wurden, oder dass ein" +
            " Paket nur einmal beim Empfänger eintrifft. Es gibt auch keine Gewähr dafür, dass die Daten " +
            "unverfälscht oder unzugänglich für Dritte beim Empfänger eintreffen. Eine Anwendung, die UDP nutzt, " +
            "muss daher gegenüber verlorengegangenen und unsortierten Paketen unempfindlich sein oder selbst " +
            "entsprechende Korrekturmaßnahmen und ggfs. auch Sicherungsmaßnahmen vorsehen. Ein Datenschutz ist bei" +
            " dieser offenen Kommunikation nicht möglich.\n\nDa vor Übertragungsbeginn nicht erst eine Verbindung " +
            "aufgebaut werden muss, kann ein Partner oder können beide Partner schneller mit dem Datenaustausch" +
            " beginnen. Das fällt vor allem bei Anwendungen ins Gewicht, bei denen nur kleine Datenmengen " +
            "ausgetauscht werden müssen. Einfache Frage-Antwort-Protokolle wie DNS (das Domain Name System)" +
            " verwenden UDP, um die Netzwerkbelastung gering zu halten und damit den Datendurchsatz zu erhöhen. Ein" +
            " Drei-Wege-Handschlag wie bei TCP (dem Transmission Control Protocol) für den Aufbau der Verbindung [...]" +
            "\n Quelle: Wikipedia (https://de.wikipedia.org/wiki/User_Datagram_Protocol#Eigenschaften)";
    // convert string to byte
    private static byte[] bodyBytes = bodyString.getBytes();


    public static void main(String[] args) throws IOException {

        // user UDP (true) or TCP (false) for the transmission
        boolean useUDP = args.length == 0 || !args[0].equals("tcp");
        //useUDP = false;

        String host = (args.length <= 1) ? "127.0.0.1" : args[1];

        if(args.length == 4) {
            N = Long.valueOf(args[2]);
            k = Long.valueOf(args[3]);
        }


        InetAddress serverAddress = InetAddress.getByName(host);

        System.out.printf("%s client started (%s:%d, N=%d, k=%d) \n"
                , ((useUDP) ? "UDP" : "TCP")
                , host
                , serverPort
                , N
                , k
                );

        //System.out.println(dataString.getBytes("UTF-8").length);

        long startTime = System.currentTimeMillis();

        List<Long> lostPacketsList = new ArrayList<>();

        long packetID = 0;

        // Transmission
        if(useUDP) { // use UDP

            DatagramSocket toServer = null;
            DatagramPacket packet;

            // create DatagramSocket
            try {
                toServer = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }

            while (System.currentTimeMillis() - startTime < transmissionTime * 1000) {

                // Header: ID
                packetID++;

                // influence the performance
                if (packetID % N == 0)
                    delay(k);

                ByteBuffer dataBuffer = createPacket(packetID, bodyBytes);

                packet = new DatagramPacket(dataBuffer.array(), dataBuffer.array().length, serverAddress, serverPort);

                try {
                    toServer.send(packet);
                } catch (FileNotFoundException e) {
                    // TODO
                    System.err.println("FileNotFoundException");
                } catch (IOException e) {
                    if(!lostPacketsList.contains(packetID)) {
                        lostPacketsList.add(packetID);
                        System.err.println("IOException" + packetID);
                        e.printStackTrace();
                    }
                }
            }
        } else { // use TCP

            while (System.currentTimeMillis() - startTime < transmissionTime * 1000) {
                try (Socket s = new Socket(serverAddress,serverPort);
                     DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
                ) {
                    //s.setKeepAlive(false);
                    //s.setReuseAddress(true);
                    // Header: ID
                    packetID++;

                    // influence the performance
                    if (packetID % N == 0)
                        delay(k);

                    toServer.write(createPacket(packetID, bodyBytes).array(), 0, 1416);

                } catch(NoRouteToHostException e){
                    // System.err.println("NoRouteToHostException " + packetID);


                } catch(SocketException e){
                    //System.err.println("SocketException");
                    // System.out.println(">> " + packetID);
                    // e.printStackTrace();
                }catch(IOException e){
                    System.err.println("IOException");
                    if (!lostPacketsList.contains(packetID)) {
                        lostPacketsList.add(packetID);
                        System.out.println("" + packetID);
                        e.printStackTrace();
                    }
                }
            }
        }


        long endTime = System.currentTimeMillis();
        double durationInSeconds = (double)(endTime-startTime)/1000;
        double packetSizeInkBit = (double) (bodyBytes.length + 16) * 8 / 1000;

        double performanceTheoretical = (double)(N * packetSizeInkBit) / ((double) k / 1000);
        double performanceEffective = ((double) (packetID * packetSizeInkBit)/durationInSeconds);

        System.out.printf("%s client stopped\n\n", ((useUDP) ? "UDP" : "TCP"));
        System.out.printf("Es wurden %d Pakete versendet.\n", packetID);
        System.out.printf("Übertragungszeit SOLL/IST [s]: %d/%,.2f\n", transmissionTime, durationInSeconds);
        System.out.printf("Verzögerung Zeit k: %d\n", k);
        System.out.printf("Verzögerung nach Anzahl Pakete N: %d\n", N);
        System.out.printf("Theoretische Senderate:  %,.2f\n", performanceTheoretical);
        System.out.printf("Tatsächliche Senderate:  %,.2f\n\n", performanceEffective);

        System.out.printf("%d; %d; %f; %d; %d; %f; %f;\n\n", packetID, transmissionTime, durationInSeconds, k, N, performanceTheoretical, performanceEffective);
    }

    private static void delay(long k) {
        try {
            // System.out.println(".");
            Thread.sleep(k);
            // System.out.println("..");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ByteBuffer createPacket(long packetID, byte[] bodyBytes) {

        // Header: ID
        byte[] headerID = ByteBuffer.allocate(8).putLong(packetID).array();

        // Heder: sending time
        byte[] headerSendingTime = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();

        // concat header and body data
        byte[] data = new byte[bodyBytes.length + 2 * 8];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        dataBuffer.put(headerID);
        dataBuffer.put(headerSendingTime);
        dataBuffer.put(bodyBytes);

        return dataBuffer;
    }

}
