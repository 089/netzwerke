package stze;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UDPServer {

    public static void main(String[] args) {

        int serverPort = 4711;
        int packetSize = 1416;
        int timeoutForPerformanceMeasurement = 500; // in millis
        int maxTimouts = 10; // if there are maxTimeouts then the connection will be closed
        boolean ignoreMaxTimeouts = false;

        //boolean resetPerformanceTimer = true;
        long performanceTimer = System.currentTimeMillis();
        double performance = 0;
        ArrayList<Double> performanceList = new ArrayList<>();

        int countPackets = 0;
        int countTotalPackets = 0;
        int countTimeouts = 0;

        try (DatagramSocket socket = new DatagramSocket(serverPort)) {

            socket.setSoTimeout(timeoutForPerformanceMeasurement);
            System.out.println("UDP server started");

            // System.out.printf("von\t\tID\tTime\tLänge\tRate\n");

            System.out.println("kbit/s;\t\tPakete;\ttotal;\tZeit [s];");
            System.out.println("---------------------------------");
            while (true) {

                // Auf Anfrage warten
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

                try {
                    socket.receive(packet);

                    countTimeouts = 0;
                    countPackets++;
                    countTotalPackets++;

                    //Empfänger auslesen
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    int len = packet.getLength();
                    byte[] data = packet.getData();

                    long packetID = ByteBuffer.wrap(data,0,8).getLong();
                    long sendingTime = ByteBuffer.wrap(data,8,8).getLong();

                    //byte [] subArray = Arrays.copyOfRange(bytes, 4, 6);

                    // System.out.printf("%s:%d\t\t%d\t%d\t%d\t%f\n", address, port, packetID, sendingTime, len, performance);

//                System.out.printf("Anfrage von %s vom Port %d mit der Länge %d:%n%s%n", address, port, len, new String(data, 16, len));

                } catch (SocketTimeoutException e) {
                    //e.printStackTrace();
                    // System.out.printf("%d, %d, %d, %d, %d, %d, %d\n", countPackets, packetSize, (countPackets * packetSize * 8), System.currentTimeMillis(), performanceTimer, (System.currentTimeMillis() - performanceTimer), ((System.currentTimeMillis() - performanceTimer)/1000));

                    long bitsOfAllReceivedPackets = countPackets * packetSize * 8;
                    double timeDiffInSeconds = (System.currentTimeMillis() - performanceTimer)/1000.0;

                    performance = (timeDiffInSeconds > 0) ? (bitsOfAllReceivedPackets/timeDiffInSeconds) / 1024 : 0; // we must avoid by zero division error.
                    performanceList.add(performance);

                    // Datenrate anzeigen
                    if(countPackets > 0)
                        //System.out.printf("Letzte Datenrate: %,.2f kbit/s.\nPakete: %d (total: %d)\nZeit: %2.3f s\n------------------------------------\n"
                        System.out.printf("%,10.2f;\t%5d;\t%5d;\t%2.3f;\n"
                                , performance
                                , countPackets
                                , countTotalPackets
                                , timeDiffInSeconds
                                );

                    performanceTimer = System.currentTimeMillis();
                    countPackets=0;

                    if(countTimeouts > 0)
                        System.out.println(maxTimouts-countTimeouts);

                    countTimeouts++;
                    if(!ignoreMaxTimeouts && countTimeouts > maxTimouts) {
                        System.out.println("Max timeouts achieved.");
                        break;
                    }

                } catch (SocketException e) {
                    // TODO
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UDP server stopped");
        System.out.printf("Es wurden %d Pakete empfangen.\n", countTotalPackets);
        System.out.println("Mittelwert: " + new Calculations().average(performanceList));
        System.out.println("Standartabweichung: " + new Calculations().standardDeviation(performanceList));


    }

}
