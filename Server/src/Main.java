import Classes.Request;
import Classes.Response;
import Configs.ServerConfig;
import Constants.RequestTypes;
import Constants.StatusCodes;
import Database.DBSetup;
import Routes.Login;
import Routes.Logout;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Steven Balagtas
 *
 * This is the main class, the server will be initialised here.
 */

public class Main {
    public static void main(String[] args) {
        // check if database is setup
        try {
            DBSetup.setupTables();
        } catch (Exception e) {
            System.err.println(e);
            System.out.println("FAILURE! Could not connect to database");
            System.out.println("Terminating server now...");
            System.exit(0);
        }

        // instantiate a new session HashMap
        HashMap<String, ArrayList<String>> sessions = new HashMap<>();

        // initialise and run the server
        try {
            ServerSocket serverSocket = new ServerSocket(ServerConfig.getPort());
            System.out.println("Server is now running and listening on port: " + ServerConfig.getPort());

            // infinite loop to keep listening for server requests
            for (;;) {
                // listen for a client connection
                Socket socket = serverSocket.accept();

                // instantiate objects for input and output
                InputStream inputStream = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                Request request = (Request) ois.readObject();
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);

                // basic logging
                System.out.println("\nClient connected: " + socket.getInetAddress());
                System.out.println("Client requested: " + request.getRequestType() + " with parameters: " + request.getRequestParameters().toString());

                // only for the TestClient --REMOVE LATER--
                oos.writeObject("Request for: '" + request.getRequestType() + "' received");
                oos.flush();

                // handle requests here
                if (request.getRequestType().equals(RequestTypes.LOGIN)) {
                    Login.login(request.getRequestParameters(), sessions, oos);
                } else if (request.getRequestType().equals(RequestTypes.LOGOUT)) {
                    Logout.logout(request.getRequestParameters(), sessions, oos);
                } else {
                    oos.writeObject(new Response(StatusCodes.BAD_REQUEST, "Request Type Invalid"));
                    oos.flush();
                }

                // close the streams
                ois.close();
                oos.close();

                // close the client connection
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
