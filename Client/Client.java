import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Client {
    Socket requestSocket;          	//socket connect to the server
    ObjectOutputStream out;        	//stream write to the socket
    ObjectInputStream in;          	//stream read from the socket
    String message;                	//message send to the server
    String MESSAGE;					//message received from the server
    BufferedReader bufferedReader;
    Object obj;

    public void Client() {}

    public class MyThread extends Thread {

        public void run(){
            try {
                while (true) {
                    System.out.println("Input a sentence: ");
                    //read a sentence from the standard input
                    message = bufferedReader.readLine();
                    //Send the sentence to the server
                    if(message.charAt(10)!='f' && message.charAt(8)!='f')
                    	sendMessage(message);
                    else
                    	sendFile(message);
                }
            }
			catch(NullPointerException nullpointer){
				System.err.println("Disconnected" );
			}
            catch (Exception e)
            {
                System.err.println("Please input command in proper format" );
            }
        }
    }

    void run()
    {
        try{
            //create a socket to connect to the server
            requestSocket = new Socket("localhost", 8000);
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            byte [] buffer = new byte[100];
			
            //get Input from standard input
            bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            MyThread th = new MyThread();
            th.start();

            while(true)
            {
            	obj = in.readObject();
                //Receive the message from the server
                MESSAGE = (String)obj;
                if(MESSAGE.contains("message")){
					//show the message to the user
                	if(MESSAGE.substring(0,MESSAGE.indexOf(' ')).equals("broadcast"))
                		System.out.println("Received message: " + MESSAGE.substring(18));
                	else if(MESSAGE.substring(0,MESSAGE.indexOf(' ')).equals("unicast"))
                		System.out.println("Received message: " + MESSAGE.substring(18));
                	else if(MESSAGE.substring(0,MESSAGE.indexOf(' ')).equals("blockcast"))
                		System.out.println("Received message: " + MESSAGE.substring(20));
                	
                }else{
					FileOutputStream fos = new FileOutputStream(MESSAGE);
                	
                	// Read file to the end.
			        Integer bytesRead = 0;
			        do {
			            obj = in.readObject();
			            
			            if (!(obj instanceof Integer)) {
			                System.out.println("Something is wrong with the file");
			            }
			 
			            bytesRead = (Integer)obj;
			            obj = in.readObject();
			 
			            if (!(obj instanceof byte[])) {
			                System.out.println("Something is wrong with the file");
			            }
			            buffer = (byte[])obj;
			 
			            // Write data to output file.
			            fos.write(buffer, 0, bytesRead);
			        } while (bytesRead == 100);
			        System.out.println("File received");
					fos.close();
                }
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }catch(SocketException socketExcept){
			System.err.println("Server Disconnected" );
		}
        catch(IOException ioException){
			System.err.println("Disconnected" );
        }
        finally{
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException){
				System.err.println("Unable to close connections");
            }
        }
    }
    //send a message to the output stream
    void sendMessage(String msg)
    {
        try{
            //stream write the message
            out.writeObject(msg);
            out.flush();
            System.out.println("Message sent");
        }
        catch(IOException ioException){
			System.err.println("Unable to send message");
        }
    }

	
	
    //main method
    public static void main(String args[])
    {
        Client client = new Client();
        client.run();
    }

}