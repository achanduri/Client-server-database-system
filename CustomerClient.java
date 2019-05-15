import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

/**Course Number-CSCI 502
 * Anusha Chanduri-Z1840609
 * Sneha Kontham-Z1838982
 * Gopala Sai Uppalapati-Z1840615
 * Assignment 6
 *
 */
public class CustomerClient extends JFrame implements ActionListener {

    // GUI components
    private JButton connectButton = new JButton("Connect"); //Create all the buttons
    private JButton getAllButton = new JButton("Get All");
    private JButton addButton = new JButton("Add");
    private JButton deleteButton = new JButton("Delete");
    private JButton updateButton = new JButton("Update Address");

    private JLabel messageLabel = new JLabel("Client Started");
	private JTextArea addressTextField = new JTextArea();
	private JTextArea zipTextField = new JTextArea();
	private JTextArea ssnTextField = new JTextArea();
	private JTextArea nameTextField = new JTextArea();
	private JTextArea resultTextArea = new JTextArea(8,40);
   

    private Socket socket;  
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private static final long serialVersionUID = 1L;

    
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            CustomerClient client = new CustomerClient();
            client.createAndShowGUI();
            client.setVisible(true);
        });
    }

    
    private CustomerClient() {
        super("Customer Database");
    }

   
    private void createAndShowGUI() {
    	
    	JPanel mainPanel=new JPanel(new BorderLayout());
    	
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel maingridLayout=new JPanel(new GridLayout(3, 1, 5, 5));
		JPanel gridLayout=new JPanel(new GridLayout(2, 2, 5, 5));
		
		//Add Input Values
		gridLayout.add(new JLabel("Name:"));
		gridLayout.add(nameTextField);
		gridLayout.add(new JLabel("SSN:"));
		gridLayout.add(ssnTextField);
		gridLayout.add(new JLabel("Address:"));
		gridLayout.add(addressTextField);
		gridLayout.add(new JLabel("Zip Code:"));
		gridLayout.add(zipTextField);
		maingridLayout.add(gridLayout);
		
		JPanel flowLayout=new JPanel(new FlowLayout(FlowLayout.CENTER,10,5));
		
		//Add Buttons
		
		flowLayout.add(connectButton);
		flowLayout.add(getAllButton);
		flowLayout.add(addButton);
		flowLayout.add(deleteButton);
		flowLayout.add(updateButton);
		maingridLayout.add(flowLayout);
		maingridLayout.add(messageLabel);
		
		//Set default values to buttons
		
		connectButton.setEnabled(true);
		getAllButton.setEnabled(false);
		addButton.setEnabled(false);
		updateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		resultTextArea.setEditable(false);
		
		mainPanel.add(maingridLayout,BorderLayout.NORTH);
		mainPanel.add(new JScrollPane(resultTextArea),BorderLayout.CENTER);
		add(mainPanel,BorderLayout.CENTER);
		
		//Adding listeners to buttons
    	connectButton.addActionListener(this);
    	getAllButton.addActionListener(this);
    	addButton.addActionListener(this);
    	updateButton.addActionListener(this);
    	deleteButton.addActionListener(this);

    	setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setVisible(true);
    }
    

    @Override
   
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Connect")) {   
            connect();
        } else if (e.getActionCommand().equals("Disconnect")) { 
            disconnect();
        } else if (e.getSource() == getAllButton) { 
            handleGetAll();
        } else if (e.getSource() == addButton) {    
            handleAdd();
        } else if (e.getSource() == updateButton) { 
            handleUpdate();
        } else if (e.getSource() == deleteButton) { 
            handleDelete();
        }
    }

    /**
	 * Connect to server + connection setup
	 */
    private void connect() {
        try {
            // Replace 97xx with your port number
            socket = new Socket("hopper.cs.niu.edu", 9706); 

            System.out.println("LOG: Socket opened");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("LOG: Streams opened");

            connectButton.setText("Disconnect");    

            //Enable Buttons
            getAllButton.setEnabled(true);  
            addButton.setEnabled(true);
            deleteButton.setEnabled(true);
            updateButton.setEnabled(true);

        } catch (UnknownHostException e) {
            System.err.println("Exception resolving host name: " + e);
        } catch (IOException e) {
            System.err.println("Exception establishing socket connection: " + e);
        }
    }

    /**
	 * Disconnect from server
	 */
    private void disconnect() {
        connectButton.setText("Connect");   

        //Disable buttons
        getAllButton.setEnabled(false); 
        addButton.setEnabled(false);
        deleteButton.setEnabled(false);
        updateButton.setEnabled(false);
        messageLabel.setText("");  
        resultTextArea.setText("");    

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Exception closing socket: " + e);
        }
    }

    // handles GetAll command
    private void handleGetAll() {
        resultTextArea.setText("");    
        MessageObject toSend = new MessageObject("GETALL");

        try {
            out.writeObject(toSend);    //Send Command
            out.flush();    
            out.reset();

            ArrayList<MessageObject> receivedList = (ArrayList<MessageObject>) in.readObject(); //Get the returned ArrayList

            messageLabel.setText(receivedList.get(0).getStatus()); //get the total size that is stored in the first object

            for(int i = 0; i < receivedList.size(); i++)    //Loop through the whole thing and print it out to the text area
            {
                if (receivedList.get(i).getName() != null) {    
                    resultTextArea.append(receivedList.get(i).getName() + " | " +   
                                           receivedList.get(i).getSsn() + " | " +
                                       receivedList.get(i).getAddress() + " | " +
                                       receivedList.get(i).getZipCode() + " | "
                                         + " \n ");
                }
            }

        } catch (Exception e)
        {
            System.out.println(e);
        }
        nameTextField.setText("");  
        ssnTextField.setText("");
        addressTextField.setText("");
        zipTextField.setText("");
    }

    //handles Add Command
    private void handleAdd() {
       //Data Validation
    	if(nameTextField.getText().equals("")) {
    		messageLabel.setText("Name is not defined");
    		return;
    		}
		if(ssnTextField.getText().equals("")) {
			messageLabel.setText("SSN is not defined");
			return;
			}
		if(addressTextField.getText().equals("")) {
			messageLabel.setText("Address is not defined");
			return;
			}
		if(zipTextField.getText().equals("")) {
			messageLabel.setText("Zip Code is not defined");
			return;
			}
		if(!ssnTextField.getText().matches("\\d{3}-\\d{2}-\\d{4}")){
			messageLabel.setText("Enter Valid SSN");
			return;
	     	}
		if(nameTextField.getText().length() > 20) {
			messageLabel.setText("Name is too long");
			return;
			}
		if(addressTextField.getText().length() > 30) {
			messageLabel.setText("Address is too long");
			return;
			}
		if(zipTextField.getText().length() > 5 || zipTextField.getText().length() < 5) {
			messageLabel.setText("Invalid Zip Code");
			return;
			}
		Pattern p = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
		Matcher m = p.matcher(ssnTextField.getText());
		if(!m.matches()){
			messageLabel.setText("SSN is invalid");
			return;
		}
		
            MessageObject msg = new MessageObject(nameTextField.getText(), ssnTextField.getText(), addressTextField.getText(), zipTextField.getText(), "ADD");   //make a new object with data from text fields

            try {
            	
                out.writeObject(msg);    //Send Command
                out.flush();
                out.reset();

                MessageObject received = (MessageObject) in.readObject();  
                messageLabel.setText(received.getStatus());    
            } catch (Exception e) {
                System.out.println(e);
            }
        
        nameTextField.setText("");  
        ssnTextField.setText("");
        addressTextField.setText("");
        zipTextField.setText("");
    }

    //handles Delete Command
    private void handleDelete() {
       
        	//Data Validation
    	if(ssnTextField.getText().equals("")) {
			messageLabel.setText("SSN is not defined");
			return;
			}
    	if(!ssnTextField.getText().matches("\\d{3}-\\d{2}-\\d{4}")){
			messageLabel.setText("Enter Valid SSN");
			return;
	     	}
    	
            Pattern p = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    		Matcher m = p.matcher(ssnTextField.getText());
    		if(!m.matches()){
    			messageLabel.setText("SSN is invalid");
    			return;
    		}   
            

            MessageObject msg = new MessageObject(ssnTextField.getText(), "DELETE"); //Make a new object with the ssn

            try {
            	
                out.writeObject(msg);    //send Command
                out.flush();   
                out.reset();

                MessageObject received = (MessageObject) in.readObject(); 
                messageLabel.setText(received.getStatus());   
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
       
        nameTextField.setText("");  
        ssnTextField.setText("");
        addressTextField.setText("");
        zipTextField.setText("");
    }

   //handles Update Command
    private void handleUpdate() {
        
        	
    	//Data Validation
    	if(ssnTextField.getText().equals("")) {
			messageLabel.setText("SSN is not defined");
			return;
			}
    	if(!ssnTextField.getText().matches("\\d{3}-\\d{2}-\\d{4}")){
			messageLabel.setText("Enter Valid SSN");
			return;
	     	}
    	if(addressTextField.getText().equals("")) {
			messageLabel.setText("Address is not defined");
			return;
			}
    	if(addressTextField.getText().length() > 30) {
			messageLabel.setText("Address is too long");
			return;
			}
    	
            Pattern p = Pattern.compile("^\\d{3}-\\d{2}-\\d{4}$");
    		Matcher m = p.matcher(ssnTextField.getText());
    		if(!m.matches()){
    			messageLabel.setText("SSN is invalid");
    			return;
    		}   
            
            MessageObject msg = new MessageObject(nameTextField.getText(), ssnTextField.getText(), addressTextField.getText(), zipTextField.getText(), "UPDATE");    //Make a new object

            try {
            	
                out.writeObject(msg);    //send Command
                out.flush();
                out.reset();

                MessageObject received = (MessageObject) in.readObject();   
                messageLabel.setText(received.getStatus());  
            } catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        
        nameTextField.setText(""); 
        ssnTextField.setText("");
        addressTextField.setText("");
        zipTextField.setText("");
    }
}