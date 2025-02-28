package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import application.TextConstant;

public class Animal implements Serializable{
	static private final long serialVersionUID = 101L;
	private listConsult list=new listConsult();
	private String version ="22.1";
	public String UUID;
	public String nom; 
	private boolean dead;
	private String id;
	private Sex sex;
	private Espece espece=Espece.CHIEN;
	private String nomProprio="";
	private Date dateNaiss;
	private RaceChien race=RaceChien.Autre;
	private RaceChat racecat=RaceChat.Europeen;
private String resume;

	public Animal(){
		UUID=java.util.UUID.randomUUID().toString();
		version=TextConstant.VERSION.nameToString();
	}
	public Animal(String UUID){
		this.UUID=UUID;
		version=TextConstant.VERSION.nameToString();
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getVersion() {
		return version;
	}
	
	public void setNomProprio(String nomProprio) {
		this.nomProprio = nomProprio;
	}
	public String getNomProprio() {
		return nomProprio;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getResume() {
		return resume;
	}
	public void setResume(String resume) {
		this.resume = resume;
	}
	
	public String getUUID() {
		return UUID;
	}
	public void setEspece(Espece espece) {
		this.espece = espece;
	}
	public Espece getEspece() {
		return espece;
	}
	
public listConsult getList() {
	return list;
}
public void setDead(boolean dead) {
	this.dead = dead;
}
public void setId(String id) {
	this.id = id;
}
public void setNom(String nom) {
	this.nom = nom;
}
public void setSex(Sex sex) {
	this.sex = sex;
}
public void setDateNaiss(Date dateNaiss) {
	this.dateNaiss = dateNaiss;
}
public void setRace(RaceChien race) {
	this.race = race;
}
public void setRace(RaceChat race) {
	this.racecat = race;
}

 public Date getDateNaiss() {
	return dateNaiss;
}
 public String getId() {
	return id;
}
 public String getNom() {
	return nom;
}
 public Object getRace() {
	 if (this.espece.equals(Espece.CHIEN)||this.espece==null){
			return race; 
	 }else{
		 return racecat;
	 }

}
 public String getRaceName() {
	 
	 if (this.espece.equals(Espece.CHIEN)){
			return race.nameToString(); 
	 }else{
		 return racecat.nameToString();
	 }

}
 public Sex getSex() {
	return sex;
}
 public boolean isDead(){
	 return this.dead;
 }
public void addConsult(Consultation cons){
	this.list.addConsult(cons);
}
public void removeConsult(String UUID){
	this.list.removeConsult(UUID);
}
}
