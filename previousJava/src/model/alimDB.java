package model;

import java.io.Serializable;

public class alimDB implements Serializable{
	private static final long serialVersionUID = 1L;
	private String UUID="";
	private String sNom="";
	private String compNom="";
	private int number=0;
	
	public alimDB() {
		UUID=java.util.UUID.randomUUID().toString();
		// TODO Auto-generated constructor stub
	}
	public alimDB(String uuid, String sname, String compname) {
		UUID=uuid;
		sNom=sname;
		compNom=compname;
	}
	public alimDB(String sname, String compname) {
		UUID=java.util.UUID.randomUUID().toString();;
		sNom=sname;
		compNom=compname;
	}
	public 
	String getUUID() {
		return UUID;
	}
	public String getCompNom() {
		return compNom;
	}
	public void setCompNom(String compNom) {
		this.compNom = compNom;
	}
	public String getsNom() {
		return sNom;
	}
	public void setsNom(String sNom) {
		this.sNom = sNom;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	@Override
	public String toString() {
		return (sNom+ " (n="+(number)+") " );
	}
}