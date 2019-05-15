import java.io.Serializable;

public class Customer implements Serializable {

	private String command;
    private String name;
    private String address;
    private String ssn;
    private String zipcode;

    Customer(){ }

    public void setCommand(String command){
        this.command = command;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public void setSSN(String ssn){
        this.ssn = ssn;
    }
    public void setZipCode(String zipcode){
        this.zipcode = zipcode;
    }

    public String getCommand(){

        return name+"\t"+address+"\t"+ssn+"\t"+zipcode;
    }
    
    
    public String getName(){

        return name;
    }
    public String getAddress(){

        return address;
    }
    public String getSsn(){

        return ssn;
    }
    public String getZipcode(){

        return zipcode;
    }


}