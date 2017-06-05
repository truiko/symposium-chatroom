package serverSide;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame {
	
	private JTextField userText;
	private JTextArea chatWindow;
	private JButton micButton;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	private MicThread st;
	private ArrayList<AudioChannel> chs = new ArrayList<AudioChannel>();

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
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage((new Message(event.getActionCommand())));
						userText.setText("");
					}
				}
		);
		add(userText , BorderLayout.NORTH);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow));
		add(micButton, BorderLayout.SOUTH);
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

	private void listenForVoice() {
		while(true){
			try {
				if(connection.getInputStream().available() > 0){
					Message sound = (Message)(input.readObject());
					AudioChannel sendTo = null;
					for (AudioChannel ch : chs) {
                        if (ch.getChId() == sound.getChId()) {
                            sendTo = ch;
                        }
                    }
                    if (sendTo != null) {
                        sendTo.addToQueue(sound);
                    } else { //new AudioChannel is needed
                        AudioChannel ch = new AudioChannel(sound.getChId());
                        ch.addToQueue(sound);
                        ch.start();
                        chs.add(ch);
                    }
                }else{ //see if some channels need to be killed and kill them
                    ArrayList<AudioChannel> killMe=new ArrayList<AudioChannel>();
                    for(AudioChannel c:chs) if(c.canKill()) killMe.add(c);
                    for(AudioChannel c:killMe){c.closeAndKill(); chs.remove(c);}
                    Utils.sleep(1); //avoid busy wait
                }
					
			}catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
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
					showMessage("\n" + message.getData());
				}else{ 
					listenForVoice();
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
}
	