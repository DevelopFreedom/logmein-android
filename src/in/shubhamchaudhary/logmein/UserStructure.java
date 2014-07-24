package in.shubhamchaudhary.logmein;

import java.io.Serializable;

public class UserStructure implements Serializable{
	String username;
	String password;
	
	public void setUsername(String un){
		username = un;
	}//end of setUsername(String)
	
	public void setPassword(String pwd){
		password = pwd;
	}//end of set_password(String)
	
	public String getUsername(){
		return(username);
	}//end of getUsername()
	
	public String getPassword(){
		return(password);
	}//end of getPassword()
}
