import java.io.*;
import java.net.*;

public class WebServer {
    private static ServerSocket serverSocket;
    private static int port = 8080; //Default port
    
    public static boolean setDirectory(String directoryName) {
        //Desired working directory (where server files are located)
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
        if (inputs.length >= 2) {
            if (inputs[0].equalsIgnoreCase("-document_root")) {
                //The first command line option is "-document_root"
                if (setDirectory(inputs[1].replaceAll("\"", ""))) {
                    //The directory was set correctly
                    System.out.println("Using Directory: \"" +
                        System.getProperty("user.dir").replace('\\', '/') + "\"");
                    
                } else {
                    //The directory was not set correctly
                    System.out.println("Error in Setting Directory");
                    
                    System.out.println("Using Directory \"" +
                        System.getProperty("user.dir").replace('\\', '/') + "\"");
                }
                
            } else if (inputs[0].equalsIgnoreCase("-port")) {
                //The first command line option is "-port"
                //Attempt to parse the input into an int variable
                //Otherwise, use the default port set above
                try {
                    port = Integer.parseInt(inputs[1]);
                    System.out.println("Port number set to " + port);
                    
                } catch (Exception e) {
                    System.out.println("Error parsing port number");
                    System.out.println("Using port number 8080");
                }
            }
        }
        
        //Check if second command line option exist
        if (inputs.length >= 4) {
            if (inputs[2].equalsIgnoreCase("-document_root")) {
                //The second command line option is "-document_root"
                if (setDirectory(inputs[3].replaceAll("\"", ""))) {
                    //The directory was set correctly
                    System.out.println("Using Directory: \"" +
                        System.getProperty("user.dir").replace('\\', '/') + "\"");

                } else {
                    //The directory was not set correctly
                    System.out.println("Error in Setting Directory");

                    System.out.println("Using Directory \"" +
                        System.getProperty("user.dir").replace('\\', '/'));
                }

            } else if (inputs[2].equalsIgnoreCase("-port")) {
                //The second command line option is "-port"
                //Attempt to parse the input into an int variable
                //Otherwise, use the default port set above
                try {
                    port = Integer.parseInt(inputs[3]);
                    System.out.println("Port number set to " + port);
                } catch (Exception e) {
                    System.out.println("Error parsing port number");
                    System.out.println("Using port number " + port + " instead");
                }
            }
        }
    }
    
    public static void main(String[] args) {
        setOptions(args); //Set command line options
        
        try {
            //Start listening on port
            serverSocket = new ServerSocket(port);
            System.out.println("\nWeb Server running on port " + serverSocket.getLocalPort());
            System.out.println("Working Directory: \""
                + System.getProperty("user.dir").replace('\\', '/') + "\"");

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