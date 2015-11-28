package stze;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;


public class Server {

    private static boolean useUDP = true; // user UDP (true) or TCP (false) for the transmission
    private static int serverPort = 4711;
    private static int packetSize = 1416;
    private static int timeout = 3000; // in millis

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        long duration = 0L;
        double performance = 0d;
        long countPackets = 0L;

        System.out.printf("%s server started", ((useUDP) ? "UDP" : "TCP"));

        if(useUDP) {
            try (DatagramSocket socket = new DatagramSocket(serverPort)) {

                socket.setSoTimeout(timeout);

                while (true) {

                    // Auf Anfrage warten
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

                    try {
                        socket.receive(packet);

                        countPackets++;

                    } catch (SocketTimeoutException e) {
                        // calculate performance
                        duration = (System.currentTimeMillis()-startTime);
                        performance = measurePerformance(countPackets, duration);

                        // Schleife unterbrechen ==> Empfang/Server beenden
                        break;
                    } catch (SocketException e) {
                        // TODO
                        System.err.println("SocketException");
                    }

                }
            } catch (IOException e) {
                System.err.println("IOException");
                //e.printStackTrace();
            }
        } else { // TCP

            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

                serverSocket.setSoTimeout(timeout);
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         DataInputStream fromClient =
                                 new DataInputStream(
                                         socket.getInputStream());) {

                            countPackets++;

                            byte[] bytes = new byte[packetSize];

                            fromClient.read(bytes);

                            /*
                            if(countPackets % 1000 == 0) {
                                System.out.println(ByteBuffer.allocate(8).getLong(0));
                                //System.out.println(ByteBuffer.allocate(8).getLong(7));
                                //System.out.println(ByteBuffer.allocate(1400).array().toString());
                            } //*/

                    } catch (SocketTimeoutException e) {
                        // calculate performance
                        duration = (System.currentTimeMillis()-startTime);
                        performance = measurePerformance(countPackets, duration);

                        // Schleife unterbrechen ==> Empfang/Server beenden
                        break;
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

        System.out.printf("%s server stopped\n\n", ((useUDP) ? "UDP" : "TCP"));
        System.out.printf("Empfangene Pakete: %d\n", countPackets);
        System.out.printf("Empfangszeit: %,.3f s\n", (double)(duration/1000));
        System.out.printf("Timeout [ms]: %d\n", timeout);
        System.out.printf("Empfangsrate: %,.2f kbit/s\n", performance);

        //System.out.printf("Minimum: %,.2f\n", Calculations.min(performanceList));
        //System.out.printf("Maximum: %,.2f\n", Calculations.max(performanceList));
        //System.out.printf("Mittelwert: %,.2f\n", Calculations.average(performanceList));
        //System.out.printf("Standartabweichung: %,.2f\n", Calculations.standardDeviation(performanceList));
    }

    /**
     * Returns the performance in kbit/s
     *
     * @param countPackets amount of received packets.
     * @param duration receiving duration in milliseconds
     * @return performance in kbit/s
     */
    private static double measurePerformance(long countPackets, long duration) {
        long bitsOfAllReceivedPackets = countPackets * packetSize * 8;

        // return performance with 1000 bit = 1 kbit and duration in seconds ==> duration / 1000
        return (duration > 0) ? (bitsOfAllReceivedPackets / (duration / 1000)) / 1000 : 0.0; // avoid by zero division error.
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
