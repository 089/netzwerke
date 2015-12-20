package cat;
/**
 * Cat Server
 * @author Reiser und Marianus
 */
public class Main {

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Fehler: Falsche Parameterl�nge: Bitte die Ziel Adresse eingeben");
		} else {
			CatServer server = new CatServer();
			server.startServer(args[0]);

		}

	}

}
