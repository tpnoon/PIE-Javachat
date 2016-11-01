package Chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;

import Microgear.Microgear;
import Microgear.MicrogearEventListener;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import javax.swing.JTextField;
import java.awt.Font;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import javax.swing.BoxLayout;

import javax.swing.ScrollPaneConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Label;
import java.awt.Toolkit;

public class DemoChat extends JFrame implements MicrogearEventListener{ 

	/**
	 * Demo Java Chat version 0.0.1
	 * by noon Facebook: Thapanee Chayakul
	 * NETPIE 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JPanel panelMember;
	private JTextField txtName;
	private JTextField textFieldChatBox;
	private JTextArea textArea;
	private JScrollPane scrollPaneRightTop;

	private JButton btnDisconnect;
	private JButton btnConnect;
	private JButton btnSend;

	private Microgear microgear;
	private ArrayList<String> memberList;

	//.......Configuration...................
	
	final static String appID = "appID"; 	//appID
	final static String Key = "Key";		//Key
	final static String Secret = "Secret";	//Secret
	final static String Topic = "/chat";	//Topic
	
	//...........................
	private JPanel panelTop;
	private JLabel lblLogo;
	private Label label;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DemoChat frame = new DemoChat();
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
	
	public DemoChat() {
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Images/gear.png")));
		setTitle("Chat By NETPIE");
		this.microgear = new Microgear();
		this.microgear.setCallback(this);
		
		createFrame();
	}
	
	private void createFrame(){
		memberList = new ArrayList<String>();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 500);
		contentPane = new JPanel();

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(300);
		contentPane.add(splitPane);

		JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(null);

		lblLogo = new JLabel("");
		lblLogo.setBounds(0, 0, 299, 78);
		
		ImageIcon logoImage = new ImageIcon(getClass().getResource("/Images/netpie_logo_app.png"));
		
		lblLogo.setIcon(logoImage);
		panelLeft.add(lblLogo);
		
		JLabel lblName = new JLabel("Name :");
		lblName.setHorizontalAlignment(SwingConstants.LEFT);
		lblName.setFont(new Font("Dialog", Font.PLAIN, 18));
		lblName.setBounds(23, 114, 63, 26);
		panelLeft.add(lblName);

		txtName = new JTextField();
		txtName.setFont(new Font("TH Sarabun New", Font.PLAIN, 18));
		txtName.setBounds(96, 114, 172, 26);
		panelLeft.add(txtName);
		txtName.setColumns(10);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(txtName.getText().equals("") || txtName.getText() == null){
					JOptionPane.showMessageDialog(contentPane, "Error: name must be A-Z,a-z,0-9,_,& and must not spaces.", "Error", JOptionPane.ERROR_MESSAGE);
				}else{
					newMicrogear();
					microgear.connect(appID, Key, Secret);
					microgear.Subscribe("/chat");
					microgear.Publish("/chat", "I'm new#" + txtName.getText());
					microgear.Publish(Topic, "Hi#" + txtName.getText());
					
					microgear.Setalias(txtName.getText());
					txtName.setEditable(false);
				}
				
				

			}
		});
		btnConnect.setFont(new Font("TH Sarabun New", Font.PLAIN, 18));
		btnConnect.setBounds(92, 151, 115, 29);
		panelLeft.add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				microgear.Publish(Topic, "bye#" + txtName.getText());
				microgear.Disconnect();
				txtName.setEditable(true);
			}
		});

		btnDisconnect.setVisible(false);
		btnDisconnect.setFont(new Font("TH Sarabun New", Font.PLAIN, 18));
		btnDisconnect.setBounds(79, 151, 141, 29);
		panelLeft.add(btnDisconnect);

		JLabel lblWhosOnline = new JLabel("Who's Online!");
		lblWhosOnline.setHorizontalAlignment(SwingConstants.CENTER);
		lblWhosOnline.setFont(new Font("TH Sarabun New", Font.PLAIN, 18));
		lblWhosOnline.setBounds(32, 200, 115, 26);
		panelLeft.add(lblWhosOnline);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 241, 279, 197);
		panelLeft.add(scrollPane);

		panelMember = new JPanel();
		panelMember.setLayout(new BoxLayout(panelMember, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(panelMember);

		JSplitPane splitPaneRight = new JSplitPane();
		splitPaneRight.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneRight.setDividerLocation(350);
		splitPane.setRightComponent(splitPaneRight);

		JPanel panelRightButton = new JPanel();
		splitPaneRight.setRightComponent(panelRightButton);
		panelRightButton.setLayout(new BorderLayout(0, 0));

		textFieldChatBox = new JTextField();
		textFieldChatBox.setEnabled(false);
		textFieldChatBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					textArea.append("Me: " + textFieldChatBox.getText() + "\n");
					microgear.Publish(Topic, "msg#" + txtName.getText() + "#" + textFieldChatBox.getText());
					textFieldChatBox.setText("");
				}

			}
		});
		textFieldChatBox.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panelRightButton.add(textFieldChatBox, BorderLayout.CENTER);
		textFieldChatBox.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.append("Me: " + textFieldChatBox.getText() + "\n");
				microgear.Publish(Topic, "msg#" + txtName.getText() + "#" + textFieldChatBox.getText());
				textFieldChatBox.setText("");
			}
		});
		btnSend.setFont(new Font("TH Sarabun New", Font.PLAIN, 18));
		panelRightButton.add(btnSend, BorderLayout.EAST);

		scrollPaneRightTop = new JScrollPane();

		splitPaneRight.setLeftComponent(scrollPaneRightTop);

		JScrollBar bar = scrollPane.getVerticalScrollBar();
		bar.setPreferredSize(new Dimension(0, 50));

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPaneRightTop.setViewportView(textArea);

		panelTop = new JPanel();
		panelTop.setPreferredSize(new Dimension(0, 50));
		scrollPaneRightTop.setColumnHeaderView(panelTop);
		panelTop.setLayout(new BorderLayout(0, 0));

		label = new Label("Group Chat");
		label.setBackground(new Color(123, 104, 238));
		label.setFont(new Font("Dialog", Font.PLAIN, 18));
		label.setAlignment(Label.CENTER);
		panelTop.add(label);
	}

	public void addMember(String name) {
		JButton btnMember = new JButton(name);
		btnMember.setForeground(new Color(0, 128, 0));
		btnMember.setSize(100, 20);
		btnMember.setVisible(true);
		btnMember.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panelMember.add(btnMember);
	}

	private void clearMember() {
		panelMember.removeAll();
		panelMember.revalidate();
		panelMember.repaint();
	}

	private void updateMember() {
		clearMember();
		for (String string : memberList) {
			addMember(string);
		}
		panelMember.revalidate();
		panelMember.repaint();
	}

	public void newMicrogear() {
		microgear = new Microgear();
		microgear.setCallback(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {	
				if(microgear == null){
					e.getWindow().dispose();
				}else{
					microgear.Publish(Topic, "bye#" + txtName.getText());
					e.getWindow().dispose();
				}
				
				
			}
		});

	}
	
	@Override
	public void onConnect() {	
			microgear.Publish(Topic, "I'm new#"+txtName.getText());
			microgear.Publish(Topic, "Hi#"+txtName.getText());
			
			btnDisconnect.setVisible(true);
			btnConnect.setVisible(false);
			textFieldChatBox.setEnabled(true);
			btnSend.setEnabled(true);
			
			System.out.println("Microgear is Connected.");
	}

	@Override
	public void onMessage(String topic, String message) {
		String[] msgi = message.split("#");

		if (msgi[0].equals("I'm new") && !msgi[1].equals(txtName.getText())) {
			memberList.add(msgi[1]);
		} else if (msgi[0].equals("Hi") && !msgi[1].equals(txtName.getText())) {
			microgear.Publish(Topic, "Hey#" + txtName.getText());
		} else if (msgi[0].equals("Hey") && !msgi[1].equals(txtName.getText())) {
			if (!memberList.contains(msgi[1])) {
				memberList.add(msgi[1]);
			}
		} else if (msgi[0].equals("msg") && !msgi[1].equals(txtName.getText())) {
			if (msgi[2].trim().equals("NETPIE")) {
				textArea.append(msgi[1] + ": " + msgi[3] + "\n");
			} else {
				textArea.append(msgi[1] + ": " + msgi[2] + "\n");
			}

		} else if (msgi[0].equals("bye") && !msgi[1].equals(txtName.getText())) {
			memberList.remove(memberList.indexOf(msgi[1]));
		}
		updateMember();
		System.out.println(topic+" "+message);
	}

	@Override
	public void onPresent(String token) {
		System.out.println("Present " + token);
	}

	@Override
	public void onAbsent(String token) {
		System.out.println("Absent :" + token);
	}

	@Override
	public void onDisconnect() {		
		microgear = null;
		btnDisconnect.setVisible(false);
		btnConnect.setVisible(true);
		textFieldChatBox.setEnabled(false);
		btnSend.setEnabled(false);
		System.out.println("Microgear is Disconnect.");
	}

	@Override
	public void onError(String error) {
		System.out.println("Error : "+error);
		
	}
}
