package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import application.TextConstant;

public class MenuRation implements Serializable{
	static private final long serialVersionUID = 1120L;
	private String UUID;


	private String version= TextConstant.VERSION.nameToString();
	private int objectType;
	private boolean actual;
	private List<Ration> rationList= new ArrayList<Ration>();
	public MenuRation(){
		UUID=java.util.UUID.randomUUID().toString();
		version= TextConstant.VERSION.nameToString();
	}
 public int getObjectType() {
	return objectType;
}public List<Ration> getRationList() {
	return rationList;
}
public String getUUID() {
	return UUID;
}
public String getVersion() {
	return version;
}
public static long getSerialversionuid() {
	return serialVersionUID;
}
public void setActual(boolean actual) {
	this.actual = actual;
}
public void setObjectType(int objectType) {
	this.objectType = objectType;
}
public void setRationList(List<Ration> rationList) {
	this.rationList = rationList;
}
public void setUUID(String uUID) {
	UUID = uUID;
}
public void setVersion(String version) {
	this.version = version;
}
public void addRation (Ration ration){
	rationList.add((ration));
	
}

}
