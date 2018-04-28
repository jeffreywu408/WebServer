import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

class RequestHandler implements Runnable {
    private Socket socket; // Socket accepted from the server
    private String directory; // The working directory

    public RequestHandler(Socket s) {
        this.socket = s;
        this.directory = System.getProperty("user.dir");
    }

    @Override
    public void run() {
        try {
            // Read input from socket and prepare output
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));

            // Read input line "GET /filename.html ..."
            String s = in.readLine();
            if (s == null) {
                // readLine() returns null if the end of the stream has been reached
                out.close();
                System.out.println("Connection closed");
                return;
            }
            System.out.println(s);

            // Start parsing the request
            StringTokenizer token = new StringTokenizer(s);
            String fileName = "";

            try {
                if (token.hasMoreElements() && token.nextToken().equalsIgnoreCase("GET") && token.hasMoreElements()) {
                    fileName = token.nextToken(); // The file to be retrieved

                } else {
                    // HTTP Status Code 400: Bad Request
                    throw new Exception();
                }

                // File names ending with "/" are assumed to be looking for "/index.html"
                if (fileName.endsWith("/")) {
                    fileName += "index.html";
                }

                // If the server is running on Windows, use "\" instead of "/"
                fileName = fileName.replace('/', File.separator.charAt(0));

                // Prevent access to parent directories
                if (fileName.contains("..") || fileName.contains(":")) {
                    throw new FileNotFoundException();
                }

                // Absolute path to the file to be sent to client
                String file = directory + fileName;

                // If the parsed fileName is actually a directory
                if (new File(file).isDirectory()) {
                    // Output HTTP Status Code 301 MOVED PERMANENTLY to Client
                    fileName = fileName.replace('\\', '/'); // For readability

                    out.print("HTTP/1.0 301 Moved Permanently\r\n" + "Content-Type: text/html\r\n\r\n" + "Location: /"
                            + fileName + "/\r\n\r\n");

                    out.close();

                    // Output result
                    System.out.println("HTTP/1.0 301 MOVED PERMANENTLY: " + fileName + "\nConnction closed");
                    return;
                }

                // Check if have read permission
                if (!new File(file).canRead()) {
                    // HTTP Status Code 403: Forbidden
                    throw new SecurityException();
                }

                // May throw FileNotFoundException
                InputStream f = new FileInputStream(file);

                // Determine the content type
                String contentType = "text/plain";

                if (file.endsWith(".html") || file.endsWith(".htm")) {
                    contentType = "text/html";
                } else if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (file.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (file.endsWith(".class")) {
                    contentType = "application/octet-stream";
                }

                // HTTP Status Code 200: OK
                out.print("HTTP/1.0 200 OK\r\n" + "Content-Type: " + contentType + "\r\n");

                int contentLength = (int) (new File(file).length());
                out.print("Content-Length: " + contentLength + "\r\n\r\n");

                // Send file content to client
                byte[] a = new byte[4096];
                int bytes;
                while ((bytes = f.read(a)) > 0) {
                    out.write(a, 0, bytes);
                }

                f.close();
                out.close();
                System.out.println("HTTP/1.0 200 OK: " + fileName.replace('\\', '/') + "\nConnection closed");

            } catch (FileNotFoundException e) {
                // Output HTTP Status Code 404: NOT FOUND to client
                fileName = fileName.replace('\\', '/');

                out.println("HTTP/1.0 404 Not Found\r\n" + "Content-Type: text/html\r\n\r\n"
                        + "<html><head></head><body><b>404 File Not Found:</b><br>\"" + fileName
                        + "\" not found</body></html>\n");

                out.close();

                // Output result to system
                System.out.println("HTTP/1.0 404 NOT FOUND: " + fileName + "\nConnection closed");

            } catch (SecurityException se) {
                // Output HTTP Status Code 403 FORBIDDEN to Client
                fileName = fileName.replace('\\', '/');

                out.println("HTTP/1.0 403 Forbidden\r\n" + "Content-Type: text/html\r\n\r\n"
                        + "<html><head></head><body><b>403 Forbidden</b><br></body></html>\n");

                out.close();

                // Output result
                System.out.println("HTTP/1.0 403 FORBIDDEN: " + fileName + "\nConnection closed");

            } catch (Exception ex) {
                // Output HTTP Status Code 400: Bad Request to client
                fileName = fileName.replace('\\', '/');

                out.println("HTTP/1.0 400 Bad Request\r\n" + "Content-Type: text/html\r\n\r\n"
                        + "<html><head></head><body><b>400 Bad Request</b><br></body></html>\n");

                out.close();

                // Output result
                System.out.println("HTTP/1.0 400 BAD REQUEST: " + fileName + "\nConnection closed");
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}