package edu.hm.cs.nwi.stze;

import com.sun.deploy.util.ArrayUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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

        int transmissionTime = 30; // in seconds

        int N = 100; // delay step

        long k = 1000; // delay in millis

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

        byte[] headerID = null;

        byte[] headerSendingTime = null;

        // Transmission
        while(System.currentTimeMillis() - startTime < transmissionTime * 1000) {

            // Header: ID
            packetID++;
            headerID = ByteBuffer.allocate(8).putLong(packetID).array();

            // Heder: sending time
            headerSendingTime = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();

            // concat header and body data
            byte[] data = new byte[bodyBytes.length + 2 * 8];
            ByteBuffer dataBuffer = ByteBuffer.wrap(data);
            dataBuffer.put(headerID);
            dataBuffer.put(headerSendingTime);
            dataBuffer.put(bodyBytes);

            DatagramPacket packet = new DatagramPacket(dataBuffer.array(), dataBuffer.array().length, serverAddress, serverPort );

            try {
                new DatagramSocket().send(packet);
            } catch(FileNotFoundException e) {
                // TODO
            } catch (IOException e){
                //e.printStackTrace();
                System.out.println("" + packetID);
                lostPacketsList.add(packetID);
                continue;
            }


        // influence the performance
            if(packetID % N == 0) {
                try {
                    System.out.println("begin sleep");
                    Thread.sleep(k);
                    System.out.println("end sleep");
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

}
