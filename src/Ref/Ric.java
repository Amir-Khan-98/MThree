package Ref;

import java.io.Serializable;

public class Ric implements Serializable{
	public final String ric;
	public Ric(String ric){
		this.ric=ric;
	}
	public String getEx(){
		return ric.split(".")[1];
	}
	public String getCompany(){
		return ric.split(".")[0];
	}
}