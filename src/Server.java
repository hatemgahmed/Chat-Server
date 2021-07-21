import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	private HashMap<String, DataOutputStream> clients;
	private HashSet<String> otherServersClients;
	private volatile Socket serverSocket;
	private volatile boolean sending=false;
	private DataOutputStream outToOtherServer;
	private BufferedReader inFromServer;
	private ServerSocket welcomeSocket;
//	private static int clients=0;
	
	private class clientHandler implements Runnable{
		
		Socket connectionSocket;
		String clientName;
		DataOutputStream outToClient;
		BufferedReader inFromClient;
		String[] clientSentence;
		int TTL;
		
		@Override
		public void run() {
			try {
//				String capitalizedSentence;
				inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				outToClient =new DataOutputStream(connectionSocket.getOutputStream());
				clientName=inFromClient.readLine();
				System.out.println(clientName+" is connected");
				clients.put(clientName, outToClient);
				
				
				EventQueue.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						notifyOtherServer();
					}
				});
				
				talkWithClient();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		void clientRemoved() throws IOException {
			clients.remove(clientName);
			connectionSocket.close();
			notifyOtherServer();
		}
		
		protected clientHandler(Socket s) {
			connectionSocket=s;
		}
		
		void notifyClientOfDroppedMessage() throws IOException {
			outToClient.writeBytes("client:ttlError"+"\n");
		}
		
		void Route(String message,String destination) throws IOException {
			if(clients.containsKey(destination)) {
				if(TTL>1) {
					//Required
					DataOutputStream outToDestination;
					outToDestination=clients.get(destination);
					outToDestination.writeBytes("From: "+clientName+"-> "+message+"\n");
				}
				else
					notifyClientOfDroppedMessage();
			}
			else
				if(otherServersClients.contains(destination)){	
					if(TTL>3) {
						//Reuired
						outToOtherServer.writeBytes(clientName+':'+destination+':'+message+"\n");
						System.out.println("Forwarding");
					}
					else
						notifyClientOfDroppedMessage();
				}
				else
					outToClient.writeBytes("client:Name not found"+"\n");
		}
		
		void talkWithClient() throws IOException {
			while(true) {
				if(!connectionSocket.isConnected()) {
					clientRemoved();
					break;
				}
				
				
				clientSentence = inFromClient.readLine().split(":");
				if(clientSentence==null||
						(clientSentence[0].equals("Server")&&clientSentence[1].equals("Quit"))) {
					clientRemoved();
					break;
				}
				if(clientSentence[0].equals("Server")&&clientSentence[1].equals("getMembers")) {
					outToClient.writeBytes(MemberListResponse());
					continue;
				}
//				System.out.println(Arrays.toString(clientSentence));
				TTL=Integer.parseInt(clientSentence[2]);
				Route(clientSentence[1],clientSentence[0]);
				
			}
		}
	}
	
	@SuppressWarnings("resource")
	public Server() throws IOException {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
//		ServerSocket welcomeSocket = new ServerSocket(6789);
		System.out.print("This server's socket:");
		welcomeSocket = new ServerSocket(Integer.parseInt(br.readLine()));
		System.out.print("The other server's socket:");
		clients=new HashMap<String,DataOutputStream>();
		
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					otherServerHandler(br.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
//		ThreadPoolExecutor pool=new ThreadPoolExecutor(2, 20, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
//		clients.keySet().toArray();
		
	}

	
	void joinResponce(String name) {
		
	}
	
	String MemberListResponse() {
		Set<String> serverHere=clients.keySet();
		String s=(serverHere.size()+otherServersClients.size())+"\n";
		for(String i:serverHere)
			s+=i+"\n";
		for(String i:otherServersClients)
			s+=i+"\n";
		return s;
	}
	
	void notifyOtherServer() {
		Thread notifyOtherServer =new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(sending);
				sending=true;
				try {
				Object[] clientArray=clients.keySet().toArray();
					outToOtherServer.writeBytes(clientArray.length+"\n");
					for(int i=0;i<clientArray.length;i++)
						outToOtherServer.writeBytes((String)clientArray[i]+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					sending=false;
				}
			}
		});
		notifyOtherServer.start();
		
	}
	
	void otherServerHandler(String otherServerSocketNumber) {
		Thread t=new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					serverSocket = new Socket("HEXADECIMAL", Integer.parseInt(otherServerSocketNumber));
					outToOtherServer = new DataOutputStream(serverSocket.getOutputStream());
					inFromServer =new BufferedReader(new InputStreamReader(welcomeSocket.accept().getInputStream()));
					System.out.println("Connected to:"+otherServerSocketNumber);
					
					//now I can recieve clients
					EventQueue.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							recieveClients();
						}
					});
					
					
					while(true) {
						String s=inFromServer.readLine();
						System.out.println(s);
						try {
							int number=Integer.parseInt(s);
							otherServersClients=new HashSet<String>();
							for(int i=0;i<number;i++) {
								s=inFromServer.readLine();
								otherServersClients.add(s);
							}
							continue;
						}catch(NumberFormatException e) {
							String[] header=s.split(":");
							DataOutputStream toSelectedClient=clients.get(header[1]);
//							if(Integer.parseInt(header[3])-1>0)
								//Message Drop
							toSelectedClient.writeBytes("From: "+header[0]+"-> "+header[2]+"\n");
						}
					}
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	

	
	
	void recieveClients(){
		Thread t=new Thread(new Runnable() {
			
			@Override
			public void run() {
				ThreadPoolExecutor pool=(ThreadPoolExecutor) Executors.newFixedThreadPool(20);
				while(true) {
					Socket connectionSocket;
					try {
						connectionSocket = welcomeSocket.accept();
						pool.execute(new clientHandler(connectionSocket));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	public static void main(String[] args) throws IOException{
		new Server();
	}
}
