import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.internal.NonNull;
import notifications.NotificationService;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Main {

	static final Logger LOGGER = Logger.getLogger(Thread.currentThread().getName());

	public static void main(String[] args) {
		LOGGER.info("\n\n\n");
		LOGGER.info("Start server application");

		if (args.length < 2) {
			System.err.println("Missing required arguments:\n" +
					"\t1) Path to service key\n" +
					"\t2) URL of Firabase Realtime Database");
			LOGGER.error("Not enouth arguments, shutting down");
			System.exit(1);
		}

		final String pathToServiceKey = args[0];
		final String databaseURL = args[1];
		LOGGER.info("Path to service key: [" + pathToServiceKey + ']');
		LOGGER.info("URL of Database: [" + databaseURL + ']');

//		System.out.close();
//		System.err.close();

		initializeSDK(pathToServiceKey, databaseURL);

		NotificationService.start();

	}

	public static void initializeSDK(@NonNull final String pathToServiceKey,
	                                 @NonNull final String databaseURL) {

		LOGGER.debug("Start to initialize Firebase SDK");
		try(FileInputStream serviceAccount = new FileInputStream(pathToServiceKey)) {
			final FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl(databaseURL)
					.build();

			FirebaseApp.initializeApp(options);
			LOGGER.debug("Firebase SDK successfully initialized");

		} catch (FileNotFoundException e) {
			LOGGER.error("Service key is not found", e);
			System.exit(1);

		} catch (IOException e) {
			LOGGER.error("Error closing resource", e);
			System.exit(1);
		}
	}
}
