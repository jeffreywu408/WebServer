import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    private static ServerSocket serverSocket;
    private static int port = 8080; //Default port
    
    //Desired working directory (where server files are located)
    public static boolean setDirectory(String directoryName) {
        File directory = new File(directoryName).getAbsoluteFile();
        
        if (directory.exists() || directory.mkdirs()) {
            if (System.setProperty("user.dir", directory.getAbsolutePath()) != null) {
                return true;
            }
        }

        return false;
    }
    
    //This implementation only takes in "-document_root" or "-port" inputs
    //Further command line inputs are ignored
    public static void setOptions(String[] inputs) {
    	for (int i = 0; i < inputs.length; i+=2) {
    		if (inputs[i].equalsIgnoreCase("-document_root")) {
    			//Set desired working directory
                if (setDirectory(inputs[i+1].replaceAll("\"", ""))) {
                    //The directory was set correctly
                    System.out.println("Directory Successfully Set: \"" + System.getProperty("user.dir").replace('\\', '/') + "/\"");
                } else {
                    //The directory was not set correctly
                    System.out.println("Error in Setting Directory");
                    System.out.println("Using Directory \"" + System.getProperty("user.dir").replace('\\', '/') + "/\"");
                }
                
            } else if (inputs[i].equalsIgnoreCase("-port")) {
                //Set port to be used
                try {
                    port = Integer.parseInt(inputs[i+1]);
                    System.out.println("Port number successfully set to " + port);
                } catch (NumberFormatException ex) {
                    System.out.println("Error parsing port number. Using port number 8080 instead.");
                }
            }
    	}
    }
    
    public static void main(String[] args) {
    	//Set command line options
    	setOptions(args);
        
        try {
            //Start listening on port
            serverSocket = new ServerSocket(port);
            System.out.println("\nWeb Server running on port " + serverSocket.getLocalPort());
            System.out.println("Working Directory: \"" + System.getProperty("user.dir").replace('\\', '/') + "/\"");

            //Server infinite loop and wait for client(s) to connect
            while (true) {
                //accept client connection
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
                
                //Create a new thread and handle the client request
                RequestHandler handler = new RequestHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}