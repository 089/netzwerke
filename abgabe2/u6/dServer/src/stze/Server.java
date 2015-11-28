package stze;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Server {

    private static boolean useUDP = true; // user UDP (true) or TCP (false) for the transmission
    private static int serverPort = 4711;
    private static int packetSize = 1416;
    private static int timeoutForPerformanceMeasurement = 200; // in millis
    private static int maxTimouts = 100; // if there are maxTimeouts then the connection will be closed
    private static boolean ignoreMaxTimeouts = false;


    // DO NOT CHANGE THESE VALUES
    private static long countPackets = 0;
    private static long countTotalPackets = 0;
    private static int countTimeouts = 0;
    private static long performanceTimer = System.currentTimeMillis();
    private static ArrayList<Double> performanceList = new ArrayList<>();


    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        System.out.printf("%s server started", ((useUDP) ? "UDP" : "TCP"));
        System.out.println("kbit/s;\t\tPakete;\ttotal;\tZeit [s];");
        System.out.println("---------------------------------");

        if(useUDP) {
            try (DatagramSocket socket = new DatagramSocket(serverPort)) {

                socket.setSoTimeout(timeoutForPerformanceMeasurement);
                while (true) {

                    // Auf Anfrage warten
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

                    try {
                        socket.receive(packet);

                        countTimeouts = 0;
                        countPackets++;
                        countTotalPackets++;

                    } catch (SocketTimeoutException e) {
                        //e.printStackTrace();

                        measurePerformance();

                        if (countTimeouts > 0)
                            System.out.printf("%2.1fs until timeout\n", (maxTimouts - countTimeouts) * (timeoutForPerformanceMeasurement / 1000.0));

                        countTimeouts++;
                        if (!ignoreMaxTimeouts && countTimeouts > maxTimouts) {
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
        } else { // TCP

            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

                serverSocket.setSoTimeout(timeoutForPerformanceMeasurement);

                while (true) {
                    try (Socket socket = serverSocket.accept();
                         DataInputStream fromClient =
                                 new DataInputStream(
                                         socket.getInputStream());) {

                        countTimeouts = 0;
                        countPackets++;
                        countTotalPackets++;

                        byte[] bytes = new byte[packetSize];

                        fromClient.read(bytes);

                        if(countPackets % 1000 == 0) {
                            System.out.println(ByteBuffer.allocate(8).getLong(0));
                            //System.out.println(ByteBuffer.allocate(8).getLong(7));
                            //System.out.println(ByteBuffer.allocate(1400).array().toString());
                        }


                    } catch (SocketTimeoutException e) {
                        //e.printStackTrace();

                        measurePerformance();

                        if (countTimeouts > 0)
                            System.out.printf("%2.1fs until timeout\n", (maxTimouts - countTimeouts) * (timeoutForPerformanceMeasurement / 1000.0));

                        countTimeouts++;
                        if (!ignoreMaxTimeouts && countTimeouts > maxTimouts) {
                            System.out.println("Max timeouts achieved.");
                            break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long durationInSeconds = (endTime-startTime)/1000;

        System.out.printf("%s server stopped\n", ((useUDP) ? "UDP" : "TCP"));
        System.out.printf("Timeout [ms]: %d\n", timeoutForPerformanceMeasurement);
        System.out.printf("Es wurden %d Pakete empfangen.\n", countTotalPackets);
        System.out.printf("Minimum: %,.2f\n", Calculations.min(performanceList));
        System.out.printf("Maximum: %,.2f\n", Calculations.max(performanceList));
        System.out.printf("Mittelwert: %,.2f\n", Calculations.average(performanceList));
        System.out.printf("Standartabweichung: %,.2f\n", Calculations.standardDeviation(performanceList));

    }

    private static void measurePerformance() {
        long bitsOfAllReceivedPackets = countPackets * packetSize * 8;
        double timeDiffInSeconds = (System.currentTimeMillis() - performanceTimer) / 1000.0;

        double performance = (timeDiffInSeconds > 0) ? (bitsOfAllReceivedPackets / timeDiffInSeconds) / 1024 : 0; // we must avoid by zero division error.

        // Datenrate anzeigen
        if (countPackets > 0) {
            //System.out.printf("Letzte Datenrate: %,.2f kbit/s.\nPakete: %d (total: %d)\nZeit: %2.3f s\n------------------------------------\n"
            System.out.printf("%,10.2f;\t%5d;\t%5d;\t%2.3f;\n"
                    , performance
                    , countPackets
                    , countTotalPackets
                    , timeDiffInSeconds
            );

            performanceList.add(performance);
        }

        performanceTimer = System.currentTimeMillis();
        countPackets = 0;

    }

    /*
    private static void extractData() {
        //Empfänger auslesen
        // InetAddress address = packet.getAddress();
        // int port = packet.getPort();
        // int len = packet.getLength();
        // byte[] data = packet.getData();

        // TODO long packetID = ByteBuffer.wrap(data, 0, 8).getLong();
        // TODO long sendingTime = ByteBuffer.wrap(data, 8, 8).getLong();

        // byte [] subArray = Arrays.copyOfRange(bytes, 4, 6);
    }
    */
}
