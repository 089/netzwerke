package stze;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

/**
 * @auhor Kevin Stieglitz, Martin Zell
 */
public class Server {

    private static int serverPort = 4711;
    private static int packetSize = 1416;
    private static int timeout = 10_000; // in millis

    public static void main(String[] args) {

        // user UDP (true) or TCP (false) for the transmission
        boolean useUDP = args.length == 0 || !args[0].equals("tcp");
        //useUDP = false;

        long startTime = System.currentTimeMillis();
        long duration = 0L;
        double performance = 0d;
        long countPackets = 0L;

        System.out.printf("%s server started\n", ((useUDP) ? "UDP" : "TCP"));

        if(useUDP) {
            try (DatagramSocket socket = new DatagramSocket(serverPort)) {

                socket.setSoTimeout(timeout);

                while (true) {

                    // Auf Anfrage warten
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

                    try {
                        socket.receive(packet);

                        countPackets++;

                        if(countPackets == 1)
                            startTime = System.currentTimeMillis();

                    } catch (SocketTimeoutException e) {
                        // calculate performance
                        duration = (System.currentTimeMillis()-startTime-timeout);
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

                        if(countPackets == 1)
                            startTime = System.currentTimeMillis();

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
                        duration = (System.currentTimeMillis()-startTime-timeout);
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

        double goodput = ((duration > 0) ? (double) (duration/1000) : timeout);
        System.out.printf("%s server stopped\n\n", ((useUDP) ? "UDP" : "TCP"));
        System.out.printf("Empfangene Pakete: %d\n", countPackets);
        System.out.printf("Empfangszeit: %,.2f s\n", goodput);
        System.out.printf("Timeout [ms]: %d\n", timeout);
        System.out.printf("Empfangsrate: %,.2f kbit/s\n\n", performance);

        System.out.printf("%d; %f; %d, %f\n\n",countPackets,goodput,timeout,performance);
    }

    /**
     * Returns the performance in kbit/s
     *
     * @param countPackets amount of received packets.
     * @param duration receiving duration in milliseconds
     * @return performance in kbit/s
     */
    private static double measurePerformance(long countPackets, long duration) {
        // 1000 bit = 1 kbit
        double kBitsOfAllReceivedPackets = (double) (countPackets * packetSize * 8) / 1000;
        // we need seconds
        double duration2 = (double) duration / 1000;

        // return performance with 1000 bit = 1 kbit and duration in seconds ==> duration / 1000
        return (duration > 0) ? (kBitsOfAllReceivedPackets / duration2) : 0.0; // avoid by zero division error.
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
