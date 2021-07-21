import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame{
	String name;
	volatile boolean socketClosed;
//	BufferedReader inFromUser;
	Socket clientSocket ;
	DataOutputStream outToServer ;
	BufferedReader inFromServer ;
	private Thread receiver;
	private JPanel contentPane;
	private JTextField destination;
	private JTextField messageField;
	private JTextField timeToLive;
	private JTextArea textArea;
	
	public Client() throws IOException, InterruptedException{
		socketClosed=false;
//		inFromUser=new BufferedReader(new InputStreamReader(System.in));
		this.name=JOptionPane.showInputDialog("Please enter your name");
		setTitle(name);
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				makeWindow();
			}
		});
		Join(name);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new Client();
	}
	
	public void Join(String name) throws IOException, InterruptedException {
//		System.out.println("Which server to connect to? 6789 or 9999");
		int server = Integer.parseInt(JOptionPane.showInputDialog("Which server to connect to? 6789 or 9999"));
		clientSocket = new Socket("HEXADECIMAL", server);
		outToServer =new DataOutputStream(clientSocket.getOutputStream());
		inFromServer =new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		outToServer.writeBytes(name + "\n");
		System.out.println(name+" is initiated");
		
		receiver=new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					try {
						if(socketClosed) {
							notConnectedError();
							return;
						}
						try {
							String s=inFromServer.readLine();
							if(s==null) {
								notConnectedError();
								return;
							}
							try {
								int num=Integer.parseInt(s);
								String members="";
								for(int i=0;i<num;i++)
									members=members+inFromServer.readLine()+"\n";
								new MemberList(members);
								continue;
							}catch(NumberFormatException e) {
								
							}
//						System.out.println(s);
							if(s.equals("client:ttlError")) {
								ttlError();
								continue;
							}
							if(s.equals("client:Name not found")) {
								JOptionPane.showMessageDialog(null, "Name not found", "Error", JOptionPane.ERROR_MESSAGE);
								continue;
							}
							textArea.setText(textArea.getText()+"\n"+s);
						}catch(SocketException e) {
							return;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				receiver.start();
			}
		});
		
		
			
		
	}
	
	void makeWindow() {
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					System.out.println("closing");
					outToServer.writeBytes("Server:Quit"+"\n");
					clientSocket.close();
					socketClosed=true;
					dispose();
					System.exit(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		setBounds(100, 100, 624, 486);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnGetMembers = new JButton("Get Members");
		btnGetMembers.setBounds(22, 363, 214, 37);
		btnGetMembers.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GetMemberList();
			}
		});
		contentPane.add(btnGetMembers);
		
		JButton btnSend = new JButton("Send");
		btnSend.setBounds(365, 363, 214, 37);
		btnSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Chat(name, destination.getText(), Integer.parseInt( timeToLive.getText() ) , messageField.getText());
			}
		});
		contentPane.add(btnSend);
		
		destination = new JTextField();
		destination.setBounds(22, 56, 163, 35);
		contentPane.add(destination);
		destination.setColumns(10);
		
		JLabel lblDestination = new JLabel("Destination");
		lblDestination.setBounds(33, 13, 152, 29);
		contentPane.add(lblDestination);
		
		JLabel lblMessage = new JLabel("Message to Send");
		lblMessage.setBounds(172, 99, 268, 29);
		contentPane.add(lblMessage);
		
		messageField = new JTextField();
		messageField.setColumns(10);
		messageField.setBounds(22, 135, 557, 35);
		contentPane.add(messageField);
		
		JLabel lblTimeToLive = new JLabel("Time To Live");
		lblTimeToLive.setBounds(206, 13, 152, 29);
		contentPane.add(lblTimeToLive);
		
		timeToLive = new JTextField();
		timeToLive.setColumns(10);
		timeToLive.setBounds(195, 56, 163, 35);
		contentPane.add(timeToLive);
		
		JLabel lblMessagesRecieved = new JLabel("Messages Recieved");
		lblMessagesRecieved.setBounds(172, 178, 268, 29);
		contentPane.add(lblMessagesRecieved);
		
		textArea = new JTextArea();
//		textArea.setBounds(22, 213, 557, 140);
		textArea.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(22, 213, 557, 140);
		scrollPane.setViewportView(textArea);
		contentPane.add(scrollPane);
		
	}
	
	void ttlError() {
		JOptionPane.showMessageDialog(null, "Message didn't arrive, please increase time to live", "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	void notConnectedError() {
		JOptionPane.showMessageDialog(null, "You are no longer connected to the server, please restart the application", "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	void Chat(String source,String destination,int TTL,String message){
		String clientMessage=message;
		if(TTL<=0) {
			ttlError();
			return;
		}
		try {
			if(!clientSocket.isConnected())
				notConnectedError();
			outToServer.writeBytes(destination +":"+ clientMessage+":"+ TTL + "\n");
			
//			System.out.print(">");
//			clientMessage=inFromUser.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	
	void GetMemberList(){
		try {
			outToServer.writeBytes("Server:getMembers"+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

