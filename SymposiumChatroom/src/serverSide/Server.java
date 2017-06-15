package serverSide;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
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
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.vdurmont.emoji.EmojiParser;

import clientSide.Message;

public class Server extends JFrame {
	
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton micButton;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private MicThread st;
	private ArrayList<AudioChannel> channels = new ArrayList<AudioChannel>();
	private JButton attachment;
	private BufferedWriter writer;
	private String serverIP;

	public Server() {
		super("Symposium Server");
		userText = new JTextField();
		micButton = new JButton("Voice");
		micButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				startMic();	
			}
			
		});
		userText.setEditable(false);
		try {
			serverIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						Message reply = new Message(event.getActionCommand());
						sendMessage(reply);;//server       client
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
		add(micButton, BorderLayout.SOUTH);
		setSize(500,500);
		setVisible(true);
//		try {
//			File fontFile = new File("EmojiLibrary/OpenSansEmoji.ttf");
//			Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
//			Font baseFont=font.deriveFont(16f);
//			//Component.setBaseFont(baseFont); LOOK at dragonLand github for help
//		} catch (FontFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

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

	private void playbackSound(Message sound) {
		try {
			if(connection.getInputStream().available() > 0){
				AudioChannel sendTo = null;
				for(AudioChannel channel: channels){
					if(channel.getChId() == sound.getChId()){
						sendTo = channel;	
					}
				}
				if(sendTo != null){
					sendTo.addToQueue(sound);
				}else{
					AudioChannel channel = new AudioChannel(sound.getChId());
					channel.addToQueue(sound);
					channel.start();
					channels.add(channel);
				}
			}else{
				ArrayList<AudioChannel> killMe=new ArrayList<AudioChannel>();
                for(AudioChannel c:channels) if(c.canKill()) killMe.add(c);
                for(AudioChannel c:killMe){c.closeAndKill(); channels.remove(c);}
                Utils.sleep(1); //avoid busy wait
			}
					
		}catch (IOException e) {
			e.printStackTrace();
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
		do{
			try{
				// figure out how to send more than just String
				message = (new Message(input.readObject()));
				if(message.getData() instanceof String){
					message.setData(convertToEmoji((String) message.getData()));
					showMessage("\n" + message.getData());
				}else{ 
					if(message.getData() instanceof byte[]){
						playbackSound(message);
					}
				}
				//showMessage("hi");
			}catch(Exception e){
				showMessage("\n Can't understand what that user sent!");
				e.printStackTrace();
				System.exit(1);
			}
		}while(!message.getData().equals("CLIENT - END"));
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
			if(message.getData() instanceof String){
				output.writeObject("SERVER - "+ message.getData());
				System.out.println("sent");
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
					chatWindow.append(convertToEmoji(text));
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
	
	private void startMic() {
        try {
        	System.out.println("on");
            Utils.sleep(100); //wait for the GUI microphone test to release the microphone
            st = new MicThread(output);  //creates a MicThread that sends microphone data to the server
            st.start(); //starts the MicThread
        } catch (LineUnavailableException e) { //error acquiring microphone. causes: no microphone or microphone busy
            showMessage("mic unavailable " + e);
        }
	}
	
	//checks through the input to see if there are any characters that correspond with an emoji and changes it if found
	private static String convertToEmoji(String message){
		// this method only used for the type-able Emojis
		String newString =message;
		String[] emojis = {":smiley:", ":wink:", ":slightly_frowning:",
						":upside_down, flipped_face:", ":expressionless:", ":heart:"};
		String[] emojiSymbols = {":)", ";)", ":(", "(:", ":|", "<3"};
		if(EmojiParser.parseToUnicode(message)!=message){
			newString = EmojiParser.parseToUnicode(message);
		}
		for(int i = 1; i < message.length(); i++){
			for(int j = 0; j < emojis.length; j++){
				if(message.substring(i-1, i+1).equals(emojiSymbols[j])){
					newString = message.replace(message.substring(i-1,i+1), EmojiParser.parseToUnicode(emojis[j]));
				}
			}
		}
		return newString;
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
	