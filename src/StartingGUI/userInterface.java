package StartingGUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

public class userInterface extends JFrame {

	private JPanel contentPane;
	private JPanel contactsPanel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					userInterface frame = new userInterface();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public userInterface() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setBounds(contentPane.getBounds());
		
		JScrollPane contacts=new JScrollPane();
		split.setLeftComponent(contacts);
		
		contactsPanel=new JPanel();
		contacts.setViewportView(contactsPanel);
		initContacts();
	}
	private void initContacts() {
		JList<String> contactsList=new JList<String>();
//		contactsList.
	}
}
