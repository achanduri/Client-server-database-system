import java.io.Serializable;


/**Course Number-CSCI 502
 * Anusha Chanduri-Z1840609
 * Sneha Kontham-Z1838982
 * Gopala Sai Uppalapati-Z1840615
 * Assignment 6
 *
 */
//Object that gets send between the client software and server
public class MessageObject implements Serializable {
    private String name;    
    private String ssn; 
    private String address; 
    private String zipCode; 

    private String type;    
    private String status;  

    
    public MessageObject(String name, String ssn, String address, String zipCode, String type) {
        this.name = name;
        this.ssn = ssn;
        this.address = address;
        this.zipCode = zipCode;
        this.type = type;
    }

   
    public MessageObject(String type) {
        this.type = type;
    }


    public MessageObject(String ssn, String type) {
        this.ssn = ssn;
        this.type = type;
    }


 
    public String getName() {
        return name;
    }

    
    public String getSsn() {
        return ssn;
    }

    public String getAddress() {
        return address;
    }

    
    public String getZipCode() {
        return zipCode;
    }

    
    public String getType()
    {
        return type;
    }

    
    public void setStatus(String successful) {
        this.status = successful;
    }

   
    public String getStatus() {
        return status;
    }
}