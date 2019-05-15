import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**Course Number-CSCI 502
 * Anusha Chanduri-Z1840609
 * Sneha Kontham-Z1838982
 * Gopala Sai Uppalapati-Z1840615
 * Assignment 6
 *
 */
public class CustomerServer extends Thread {
    private ServerSocket listenSocket;

    public static void main(String args[]) {
        new CustomerServer();
    }

    
    private CustomerServer() {
        // Replace 97xx with your port number
        int port = 9706;   
        try {
            listenSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Exception creating server socket: " + e);
            System.exit(1);
        }

        System.out.println("LOG: Server listening on port " + port);
        this.start();   //Make the rest of the server run on a different thread
    }

    /**
     * run()
     * The body of the server thread. Loops forever, listening for and
     * accepting connections from clients. For each connection, create a
     * new Conversation object to handle the communication through the
     * new Socket.
     */

    public void run() {
        try {
            while (true) {
                Socket clientSocket = listenSocket.accept();

                System.out.println("LOG: Client connected");

                // Create a Conversation object to handle this client and pass
                // it the Socket to use.  If needed, we could save the Conversation
                // object reference in an ArrayList. In this way we could later iterate
                // through this list looking for "dead" connections and reclaim
                // any resources.
                new Conversation(clientSocket); 
            }
        } catch (IOException e) {
            System.err.println("Exception listening for connections: " + e);
        }
    }
}

/**
 * The Conversation class handles all communication with a client.
 */
class Conversation extends Thread {

    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Where JavaCustXX is your database name
    private static final String URL = "jdbc:mysql://courses:3306/JavaCust06";

    private Statement getAllStatement = null;   
    private PreparedStatement addStatement = null;
    private PreparedStatement deleteStatement = null;
    private PreparedStatement updateStatement = null;

    /**
     * Constructor
     *
     * Initialize the streams and start the thread.
     */
    Conversation(Socket socket) {
        clientSocket = socket;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("LOG: Streams opened");
        } catch (IOException e) {
            try {
                clientSocket.close();
            } catch (IOException e2) {
                System.err.println("Exception closing client socket: " + e2);
            }

            System.err.println("Exception getting socket streams: " + e);
            return;
        }

        try {
            System.out.println("LOG: Trying to create database connection");
            Connection connection = DriverManager.getConnection(URL);

            // Create your Statements and PreparedStatements here
            getAllStatement = connection.createStatement(); 
            getAllStatement.addBatch("SELECT * FROM customer");
            addStatement = connection.prepareStatement("INSERT INTO customer VALUES (?, ?, ?, ?)"); 
            deleteStatement = connection.prepareStatement("DELETE FROM customer WHERE ssn = ?");   
            updateStatement = connection.prepareStatement("UPDATE customer SET address = ?, zipCode = ? WHERE ssn = ?");   

            System.out.println("LOG: Connected to database");

        } catch (SQLException e) {
            System.err.println("Exception connecting to database manager: " + e);
            return;
        }

        // Start the run loop.
        System.out.println("LOG: Connection achieved, starting run loop");
        this.start();
    }

    /**
     * run()
     *
     * Reads and processes input from the client until the client disconnects.
     */
    public void run() {
        System.out.println("LOG: Thread running");

        try {
            while (true) {
                // Read and process input from the client.
                MessageObject msg = (MessageObject) in.readObject();

                //Check the object and get the action from with in
                if (msg.getType().equals("GETALL"))   
                	handleGetAll();
                else if (msg.getType().equals("ADD"))  
                    handleAdd(msg);
                else if(msg.getType().equals("DELETE"))    
                    handleDelete(msg);
                else if(msg.getType().equals("UPDATE"))    
                    handleUpdate(msg);

            }
        } catch (IOException e) {
            System.err.println("IOException: " + e);
            System.out.println("LOG: Client disconnected");
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException: " + e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Exception closing client socket: " + e);
            }
        }
    }

    /**
	 * handles the GET ALL commads
	 * 
	 */
    private void handleGetAll() {
        ArrayList<MessageObject> queryList = new ArrayList<>(); 
        int i = 0;  

        try {
            ResultSet resultSet = getAllStatement.executeQuery("SELECT * FROM customer");   
            while (resultSet.next()){   
                queryList.add(new MessageObject(resultSet.getString("name"),    
                        resultSet.getString("ssn"),
                        resultSet.getString("address"),
                        resultSet.getString("zipCode"), "RETURN"));
                           i++;    
            }

            if(queryList.size() != 0)   
                queryList.get(0).setStatus(i + " Rows Returned");

            else {  
                queryList.add(new MessageObject(null)); 
                queryList.get(0).setStatus("0 Rows Returned");   
            }

            out.writeObject(queryList); //send it back to the client
            out.flush();
            out.reset();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * handles the ADD commads
	 * @param clientMsg
	 */
    private void handleAdd(MessageObject clientMsg) {
        int returnCode = 0; 

        try {
            addStatement.setString(1, clientMsg.getName()); //Fill in the blanks in the SQL statement
            addStatement.setString(2, clientMsg.getSsn());
            addStatement.setString(3, clientMsg.getAddress());
            addStatement.setString(4, clientMsg.getZipCode());
            returnCode =  addStatement.executeUpdate(); 
        }catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (returnCode >= 1)    //Check to see it was added to the data base
                clientMsg.setStatus("Record added to the Database");
            else    //Else there was an error
                clientMsg.setStatus("Unable to add record into Database");

            out.writeObject(clientMsg); //Send the result back to the client
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
          
        
    }

    /**
	 * handles the DELETE commads
	 * @param clientMsg
	 */
    private void handleDelete(MessageObject clientMsg) {
        int returnCode = 0; 

        try {
            deleteStatement.setString(1, clientMsg.getSsn());  
            returnCode = deleteStatement.executeUpdate(); 
        }catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (returnCode >= 1)    //If it worked
                    clientMsg.setStatus("Record Deleted");    //set the status
                else
                    clientMsg.setStatus("Unable to Delete the record"); //else set status to bad

                out.writeObject(clientMsg); //send it back
                out.flush();
                out.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }     
    }

    /**
	 * handles the UPDATE commads
	 * @param clientMsg
	 */
    private void handleUpdate(MessageObject clientMsg) {
        int returnCode = 0; //Return code from the command

        try {
            updateStatement.setString(1, clientMsg.getAddress());   
            updateStatement.setString(2, clientMsg.getZipCode());
            updateStatement.setString(3, clientMsg.getSsn());
            returnCode = updateStatement.executeUpdate();  
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            if (returnCode >= 1)    
                clientMsg.setStatus("Record Updated");
            else
                clientMsg.setStatus("Unable to Update the record"); 

            out.writeObject(clientMsg); 
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}