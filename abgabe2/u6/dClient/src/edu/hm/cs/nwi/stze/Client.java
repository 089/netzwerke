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
 * @author: Martin Zell
 * @version: 0.9
 */

public class Client {

    private static boolean useUDP = true; // user UDP (true) or TCP (false) for the transmission
    private static long N = 100_000_000; // delay step
    private static long k = 0; // delay in millis

    private static int serverPort = 4711;
    private static String host = "127.0.0.1";
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


    public static void main(String[] args) throws UnsupportedEncodingException, UnknownHostException {

        InetAddress serverAddress = InetAddress.getByName(host);

        System.out.printf("%s server started", ((useUDP) ? "UDP" : "TCP"));

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
                    sleep(k);

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
                try(Socket s=new Socket(serverAddress, serverPort);
                    DataOutputStream toServer =
                            new DataOutputStream(
                                    s.getOutputStream());) {

                        // Header: ID
                        packetID++;

                        // influence the performance
                        if (packetID % N == 0)
                            sleep(k);

                        ByteBuffer dataBuffer = createPacket(packetID, bodyBytes);

                        byte[] data = dataBuffer.array();

                        toServer.write(data);

                        toServer.flush();



                } catch (IOException e) {
                    if(!lostPacketsList.contains(packetID)) {
                        lostPacketsList.add(packetID);
                        System.out.println("" + packetID);
                        e.printStackTrace();
                    }
                    // continue;
                }
            }
        }


        long endTime = System.currentTimeMillis();
        long durationInSeconds = (endTime-startTime)/1000;

        int packetSizeInBit = (bodyBytes.length + 16) * 8;

        System.out.printf("%s client stopped\n", ((useUDP) ? "UDP" : "TCP"));
        System.out.printf("Es wurden %d Pakete versendet.\n", packetID);
        System.out.printf("%d Pakete wurden evtl. nicht versendet.\n", lostPacketsList.size());
        System.out.printf("Übertragungszeit SOLL/IST [s]: %d/%d\n", transmissionTime, durationInSeconds);
        System.out.printf("Verzögerung Zeit k: %d\n", k);
        System.out.printf("Verzögerung nach Anzahl Pakete N: %d\n", N);
        System.out.printf("Theoretische Senderate:  %,.2f\n", ((N * packetSizeInBit)/(double) (k/1000)));
        System.out.printf("Tatsächliche Senderate:  %,.2f\n", ((packetID * packetSizeInBit)/durationInSeconds));

        //System.out.printf("Minimum: %,.2f\n", Calculations.min(performanceList));
        //System.out.printf("Maximum: %,.2f\n", Calculations.max(performanceList));
        //System.out.printf("Mittelwert: %,.2f\n", Calculations.average(performanceList));
        //System.out.printf("Standartabweichung: %,.2f\n", Calculations.standardDeviation(performanceList));

      System.out.printf("Von %d Paketen wurden nur %d Pakete - mit einer theoretischen Datenrate von %d kbit/s - versendet. %d Pakete konnten nicht gesendet werden."
              , packetID
              , (packetID - lostPacketsList.size())
              , ((((packetID - lostPacketsList.size()) * (bodyBytes.length + 16) * 8)/(transmissionTime))/1024)
              , lostPacketsList.size()
              );

    }

    private static void sleep(long k) {
        try {
            System.out.println("begin sleep");
            Thread.sleep(k);
            System.out.println("end sleep");
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
