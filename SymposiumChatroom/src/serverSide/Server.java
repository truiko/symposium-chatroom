package serverSide;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame {
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private BufferedWriter writer;
	private String serverIP;

	public Server() {
		super("Symposium Server");
		userText = new JTextField();
		userText.setEditable(false);
		try {
			serverIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());//server       client
						File check = new File("" + "127.0.0.1" + "+" + serverIP+ ".txt");
						if(check.isFile()){
							try {//for windows
//								writer = new BufferedWriter(new FileWriter("C:/Users/" + System.getProperty("user.name") + "/git/symposium-clientside/"
//										+ "SymposiumClientSide/"+"127.0.0.1" + "+" 
//										+ serverIP+ ".txt", true));
								//for mac
								writer = new BufferedWriter(new FileWriter("/Users/" + System.getProperty("user.name") + "/git/symposium-clientside/"
										+ "SymposiumClientSide/"+"127.0.0.1" + "+" 
										+ serverIP+ ".txt", true));
							} catch (IOException e) {
								e.printStackTrace();
							}
							
						}else{
							try{
								File texting = new File("" + "127.0.0.1" + "+" + serverIP+ ".txt");
								writer = new BufferedWriter(new FileWriter(texting, true));
							}catch(IOException e){
								e.printStackTrace();
							}
						}
						try {
							if (!event.getActionCommand().equals("END")){
								writer.write("Server: " + event.getActionCommand() + "\r\n");
								System.out.println(event.getActionCommand());
							}
							else{
								writer.write("Conversation ended: " + LocalDateTime.now() + "\r\n");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						userText.setText("");
					}
				}
			);
		add(userText , BorderLayout.NORTH);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
	}
//
	//set up and run the server
	public void startRunning(){
		try{
			server = new ServerSocket(6789, 100);
			while(true){
				try{
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eofException){
					showMessage("\n Server ended the connection! ");
				}finally{
					closeAll();
				}
			}
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	//wait for connection, then display connection information
	private void waitForConnection() throws IOException{
		showMessage("Waiting for someone to connect... \n");
		connection = server.accept();
		showMessage("Now connected to "+ connection.getInetAddress().getHostAddress());
	}
	
	//get stream to send and receive data
	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup! \n");
		
	}
	
	//during the chat conversation
	private void whileChatting() throws IOException{
		String message = " You are now connected! ";
		sendMessage(message);
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
				//showMessage("hi");
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n Can't understand what that user sent!");
			}
		}while(!message.equals("CLIENT - END"));
	}
	
	//close streams and sockets after you are done chatting
	private void closeAll(){
		showMessage("\n Closing connections... \n");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send a message to client
	private void sendMessage(String message){
		try{
			output.writeObject("SERVER - "+ message);
			output.flush();
			showMessage("\nSERVER - " + message);
		}catch(IOException ioException){
			chatWindow.append("\n ERROR: Message can't be sent");
		}
	}
	
	//updates chatWindow
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(text);
				}
			}
		);
	}
	
	//let the user type stuff into their box
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
}
	