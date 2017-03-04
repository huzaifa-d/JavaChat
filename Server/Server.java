import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

	//The server will be listening on this port number
	private static final int sPort = 8000;
	public static final int BUFFER_SIZE = 100;
	public static final ObjectOutputStream[] out = new ObjectOutputStream[10];
	public static int clientNum = 1;

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running on port - 8000");
		// creating a new server socket
		ServerSocket listener = new ServerSocket(sPort);
		try {
			while(true) {
				new Handler(listener.accept(),clientNum).start();
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
			}
		} finally {
			listener.close();
		}

	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {
		private String message;    		//message received from the client
		private String MESSAGE;
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private int no;					//The index number of the client
		public Object o;

		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
		}

		public void run() {
			try{
				//initialize Input and Output streams
				out[no] = new ObjectOutputStream(connection.getOutputStream());
				out[no].flush();
				in = new ObjectInputStream(connection.getInputStream());
				byte [] buffer = new byte[BUFFER_SIZE];
				try{
					while(true)
					{
						o = in.readObject();
						//receive the message sent from the client
						message = (String)o;
						
						if(message.contains("broadcast message") || message.contains("unicast message") ||
							message.contains("blockcast message") || message.contains("broadcast file") ||
							message.contains("unicast file") || message.contains("blockcast file")){
							if(message.substring(message.indexOf(' ')+1, message.indexOf(' ')+8).equals("message") || message.substring(message.indexOf(' ')+1, message.indexOf(' ')+5).equals("file")){
								//show the message to the user
								System.out.println("Received message: " + message + " from client " + no);
							}
							if(message.substring(message.indexOf(' ')+1, message.indexOf(' ')+5).equals("file")){
								FileOutputStream fos = null;
								if(message.charAt(message.indexOf('e')+2)>'0' && message.charAt(message.indexOf('e')+2)<='9')
									fos = new FileOutputStream(message.substring(message.indexOf('e')+4));
								else
									fos = new FileOutputStream(message.substring(message.indexOf('e')+2));
								
								// Read file to the end.
						        Integer bytesRead = 0;
						        do {
						            o = in.readObject();
						            
						            if (!(o instanceof Integer)) {
						                System.out.println("Something is wrong with the input file");
						            }
						 
						            bytesRead = (Integer)o;
						            o = in.readObject();
						 
						            if (!(o instanceof byte[])) {
						                System.out.println("Something is wrong with the input file");
						            }
						            buffer = (byte[])o;
						 
						            // Write data to output file.
						            fos.write(buffer, 0, bytesRead);
						        } while (bytesRead == BUFFER_SIZE);
								fos.close();
						        System.out.println("File transfer success");						
					        }
							//send message back to the client
							sendMessage(message);
						}
					}
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}catch(EOFException eofException) {
					System.err.println("Disconnected with Client " + no);
				}catch(SocketException socketException){
					System.err.println("Disconnected with Client " + no);
				}catch (Exception e) {
					System.err.println("Disconnected");
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
			finally{
				//Close connections
				try{
					in.close();
					out[no].close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{
				// check if broadcast/unicast/blockcast is for message or file
				if(msg.substring(0, msg.indexOf(' ')).equalsIgnoreCase("broadcast")){
					if(msg.substring(msg.indexOf(' ')+1, msg.indexOf(' ')+5).equals("file")){
						
						for (int i = 1; i<clientNum; i++)
						{
							if(i==no){}
							else if(i<clientNum){
								File file = new File(msg.substring(msg.indexOf(' ')+6));
								out[i].writeObject(file.getName());	 
								FileInputStream fis = new FileInputStream(file);
								byte [] buffer = new byte[100];
								Integer bytesRead = 0;
						 
								while ((bytesRead = fis.read(buffer)) > 0) {
									out[i].writeObject(bytesRead);
									out[i].writeObject(Arrays.copyOf(buffer, buffer.length));
								}
								fis.close();
							}
						}
					}else{
						for (int i = 1; i<clientNum; i++)
						{
							if(i==no){}
							else if(i<clientNum){
								out[i].writeObject(msg);
								out[i].flush();
							}
						}
					}
				}else if(msg.substring(0, msg.indexOf(' ')).equalsIgnoreCase("unicast")){
					if(msg.substring(msg.indexOf(' ')+1, msg.indexOf(' ')+5).equals("file")){
						char toSend = msg.charAt(13);
						int index = Character.getNumericValue(toSend);
						File file = new File(msg.substring(msg.indexOf(' ')+8));
						out[index].writeObject(file.getName());	 
						FileInputStream fis = new FileInputStream(file);
						byte [] buffer = new byte[100];
						Integer bytesRead = 0;
				 
						while ((bytesRead = fis.read(buffer)) > 0) {
							out[index].writeObject(bytesRead);
							out[index].writeObject(Arrays.copyOf(buffer, buffer.length));
						}
						fis.close();
						
					}else{
						char toSend = msg.charAt(16);
						int index = Character.getNumericValue(toSend);
						out[index].writeObject(msg);
						out[index].flush();
					}
				}else if(msg.substring(0, msg.indexOf(' ')).equalsIgnoreCase("blockcast")){
					if(msg.substring(msg.indexOf(' ')+1, msg.indexOf(' ')+5).equals("file")){
						char toBlock = msg.charAt(15);
						int index = Character.getNumericValue(toBlock);
						for (int i = 1; i<clientNum; i++)
						{
							if(i==no || i==index){}
							else if(i<clientNum){
								File file = new File(msg.substring(msg.indexOf(' ')+8));
								out[i].writeObject(file.getName());	 
								FileInputStream fis = new FileInputStream(file);
								byte [] buffer = new byte[100];
								Integer bytesRead = 0;
						 
								while ((bytesRead = fis.read(buffer)) > 0) {
									out[i].writeObject(bytesRead);
									out[i].writeObject(Arrays.copyOf(buffer, buffer.length));
								}
								fis.close();
							}
						}
						
					}else{
						char toBlock = msg.charAt(18);
						int index = Character.getNumericValue(toBlock);
						for (int i = 1; i<clientNum; i++)
						{
							if(i==no || i==index){}
							else if(i<clientNum){
								System.out.println("Thread no " + i);
								out[i].writeObject(msg);
								out[i].flush();
								System.out.println("Success on Thread no " + i);
							}
						}
					}
				}
			}catch(SocketException eSock){
				System.err.println("Cannot send message/file because some Clients have been disconnected");
			}
			catch(IOException ioException){
				System.err.println("Clients doesn't exist");
			}
		}

	}

}
