package StartingGUI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	private static int clients = 0;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ServerSocket welcomeSocket = new ServerSocket(6789);
		ThreadPoolExecutor pool = new ThreadPoolExecutor(0, 20, 1000, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();

			pool.execute(new Runnable() {

				@Override
				public void run() {
					BufferedReader inFromClient;
					try {
						String clientSentence;
						String capitalizedSentence;
						inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						while (true) {
							if (!connectionSocket.isConnected()) {
								connectionSocket.close();
								break;
							}

							clientSentence = inFromClient.readLine();
							if (clientSentence == null) {
								connectionSocket.close();
								break;
							}

							if (clientSentence.equals("quit")) {
								capitalizedSentence = "Process terminated" + "\n";
								outToClient.writeBytes(capitalizedSentence);
							} else {
								capitalizedSentence = clientSentence.toUpperCase() + "\n";
								outToClient.writeBytes(capitalizedSentence);
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			// t.start();
		}
	}
}
