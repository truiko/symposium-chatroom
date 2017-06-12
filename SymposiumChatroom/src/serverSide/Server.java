package serverSide;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	
	private JButton attachment;

	public Server() {
		super("Symposium Server");
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						//sendMessage(event.getActionCommand());
						sendMessage((new Message(event.getActionCommand())));
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
		
		attachment = new JButton("Attachment");
		attachment.setSize(100,100);
		add(attachment, BorderLayout.EAST);
		attachment.setVisible(true);
		
		attachment.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					sendImage();
					//receiveImage();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
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
					receiveImage();
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
		showMessage("\n Streans are now setup! \n");
		
	}
	
	//during the chat conversation
	private void whileChatting() throws IOException{
		Message message = (new Message(" You are now connected! "));
		sendMessage(message);
		ableToType(true);
		//do{
			try{
				//receiveImage();
				message = (new Message(input.readObject()));
				//receiveImage();
				if(message.getData() instanceof String)
				showMessage("\n" + message.getData());
				//receiveImage();
				//showMessage("hi");
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n Can't understand what that user sent!");
			}
		//}while( 
			//	!message.getData().equals("CLIENT - END"));
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
	private void sendMessage(Message message){
		try{
//			output.writeObject("SERVER - "+ message);
//			output.flush();
//			showMessage("\nSERVER - " + message);
			if(message.getData() instanceof String){
				output.writeObject("SERVER - "+ message.getData());
				showMessage("\nSERVER - " + message.getData());
			}
			output.flush();
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
	private void sendImage() throws IOException{
		BufferedImage img = null;
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(fc);
		String filePath = null;
		if(returnVal == JFileChooser.APPROVE_OPTION){
			filePath = fc.getSelectedFile().getAbsolutePath();
		}else{
			System.out.println("User clicked CANCEL");
			//System.exit(1);
		}
		try{
			img = ImageIO.read(new File(filePath));
		}catch(Exception e){
			e.printStackTrace();
		}
        ImageIO.write(img, "jpg", connection.getOutputStream());
        //output.writeObject(null);
        System.out.println("sent");
	}
	
	private void receiveImage() throws IOException{
		boolean running = true;
//		System.out.println("initiating receival of image");
//		BufferedImage image = ImageIO.read(input);
//	      System.out.println("got image");
//	      JLabel label = new JLabel(new ImageIcon(image));
//	      JFrame f = new JFrame("Image sent from client");
//	      f.getContentPane().add(label);
//	      f.pack();
//	      f.setVisible(true);
//	      System.out.println("image is displayed");
		
	      
	      do{
				System.out.println("initiating receival of image");
				BufferedImage image = ImageIO.read(input);
				  System.out.println("got image");
				  JLabel label = new JLabel(new ImageIcon(image));
				  JFrame f = new JFrame("Server: Image sent from client");
				  f.getContentPane().add(label);
				  f.pack();
				  f.setVisible(true);
				  System.out.println("image is displayed");
				  running = false;
			}while(running);//
	      
//	      try{
//	    	  System.out.println("initiating receival of image");
//	    	  BufferedImage image = ImageIO.read(input);
//			  System.out.println("got image");
//			  JLabel label = new JLabel(new ImageIcon(image));
//			  JFrame f = new JFrame("Image sent from client");
//			  f.getContentPane().add(label);
//			  f.pack();
//			  f.setVisible(true);
//			  System.out.println("image is displayed");
//			  running = false;
//	      }catch(IOException i){
//	    	  i.printStackTrace();
//	      }
	}
}
	