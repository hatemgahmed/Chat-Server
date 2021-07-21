package StartingGUI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Client {
	String name;
	Queue<String[]> messageQueue;
	public static void main(String[] args) throws IOException {
		
	}
	public Client(String name) {
		this.name=name;
		messageQueue=new LinkedList<String[]>();
	}
	protected void getNextMessage() {
		
	}
}
