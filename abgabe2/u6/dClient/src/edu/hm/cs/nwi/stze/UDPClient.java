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

public class UDPClient {

    public static void main(String[] args) throws UnsupportedEncodingException, UnknownHostException {

        // user UDP (true) or TCP (false) for the transmission
        boolean useUDP = true;

        int transmissionTime = 30; // in seconds

        int N = 100_000; // delay step

        long k = 5; // delay in millis

        InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
        //InetAddress serverAddress = InetAddress.getByName("87.106.2.1");
        //InetAddress serverAddress = InetAddress.getByName("192.168.1.120");

        int serverPort = 4711;

        //#####################################################################
        // Do not change the following parameters!
        //#####################################################################

        // String with 1400 Byte of data (UTF-8!)
        String bodyString = "Eigenschaften des UDP\n" +
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
        //System.out.println(dataString.getBytes("UTF-8").length);

        // convert string to byte
        byte[] bodyBytes = bodyString.getBytes();

        long startTime = System.currentTimeMillis();

        List<Long> lostPacketsList = new ArrayList<>();

        long packetID = 0;

        // Transmission
        if(useUDP) { // use UDP
            while (System.currentTimeMillis() - startTime < transmissionTime * 1000) {

                // Header: ID
                packetID++;

                // influence the performance
                if (packetID % N == 0)
                    sleep(k);

                ByteBuffer dataBuffer = createPacket(packetID, bodyBytes);

                DatagramPacket packet = new DatagramPacket(dataBuffer.array(), dataBuffer.array().length, serverAddress, serverPort);

                try {
                    new DatagramSocket().send(packet);
                } catch (FileNotFoundException e) {
                    // TODO
                } catch (IOException e) {
                    if(!lostPacketsList.contains(packetID)) {
                        lostPacketsList.add(packetID);
                        System.out.println("" + packetID);
                        e.printStackTrace();
                    }
                    continue;
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
