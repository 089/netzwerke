package cat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Cat Server
 * @author Reiser und Marianus
 */
public class CatServer {

	public final static String CAT_URL = "http://cosmetic-candy.com/wp-content/uploads/cat-sleeping.jpg";

	public void startServer(String th) {
		try (ServerSocket server = new ServerSocket(8082)) {

			System.out.println("Waiting for connection....");
			Socket s = server.accept();

			// read HTTP request and create new one
			URL url = new URL(th);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			System.out.println("Received request!\n");
			try (BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
					PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
				for (String line = read.readLine(); line != null && !line.isEmpty(); line = read.readLine()) {

					if (line.startsWith("POST")) {
						System.err.println("POST REQUEST");
						return;
					}
					if (line.startsWith("GET")) {
						conn.setRequestMethod("GET");
						System.out.println("Request header:\n---------------------------\n" + line);
					} else {
						String[] parts = line.split(":");
						conn.setRequestProperty(parts[0], parts[1]);
						System.out.println(line);
					}

				}

				int responseCode = conn.getResponseCode();
				System.out.println("\nConnected with responsecode " + responseCode);

				// Read response
				// Can only read GZIP compressed websites
				BufferedReader in = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(conn.getInputStream())));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// replace img and "you"
				String myResponse = response.toString().replaceAll(" you",
						" you, admirer of cats and all things feline :-)");
				myResponse = myResponse.replaceAll("You", "You, admirer of cats and all things feline :-)");
				myResponse = myResponse.replaceAll("<img(.*?)src=\"(.*?)\"(.*?)>",
						"<img $1 src=\"" + CAT_URL + "\"$3>");
				
				// Add response header
				String str = "HTTP/1.1 200 OK\r\n";
				str += "Content-Lenght: " + myResponse.getBytes().length + "\r\n";
				str += "\r\n";
				System.out.println("\nResponse header:\n---------------------------\n" + str);
				
				//send html response
				pw.write(str);
				pw.write(myResponse);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
