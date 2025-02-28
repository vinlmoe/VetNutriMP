package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import application.TextConstant;

public class AnimalEv implements Serializable{
	static private final long serialVersionUID = 101L;
	private listConsultEv list=new listConsultEv();
	private String version ="22.1";
	public String UUID;
	public String nom=""; 
	private boolean dead=false;
	private String id;
	private int sex=0;
	private String espece=new String("1");
	private String nomProprio="";
	private LocalDate dateNaiss= LocalDate.now();
	private String race="";
private String resume="";
private ArrayList<WeightDate> listWeight=new ArrayList<WeightDate>();

	public AnimalEv(){
		UUID=java.util.UUID.randomUUID().toString();
		version=TextConstant.VERSION.nameToString();
		list.addConsult(new ConsultationEv());
		
	}
	public AnimalEv(String UUID){
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
 public void setEspece(String espece) {
	this.espece = espece;
}
 public String getEspece() {
	return espece;
}
 
public listConsultEv getList() {
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


public void setDateNaiss(LocalDate dateNaiss) {
	System.out.println("Date set "+dateNaiss);
	this.dateNaiss = dateNaiss;
}

 public LocalDate getDateNaiss() {
	return dateNaiss;
}
 public String getId() {
	return id;
}
 public String getNom() {
	return nom;
}
 public String getRace() {
	return race;
}
 public int getSex() {
	return sex;
}
 public void setRace(String race) {
	this.race = race;
}
 public void setSex(int sex) {
	this.sex = sex;
}
 public boolean isDead(){
	 return this.dead;
 }
 public void addWeight(WeightDate w) {
	 listWeight.add(w);
	 }
  public ArrayList<WeightDate> getListWeight() {
	return listWeight;
}
 public void updateWeight (String uuid, LocalDate d, float v) {
	for (WeightDate s:listWeight) {
		if (s.getUUID().equals(uuid)) {
			s.setDate(d);
			s.setValue(v);
			return ;
		}
	}
 }
 
 public void removeWeight(String UUIDwd) {
	 
	 for (WeightDate s:listWeight) {
			if (s.getUUID().equals(UUIDwd)) {
				listWeight.remove(s);
				return ;
			}
		}
 }
public void addConsult(ConsultationEv cons){
	this.list.addConsult(cons);
}
public void removeConsult(String UUID){
	this.list.removeConsult(UUID);
}

public void describe() {
	System.out.println(this.nom);
	for (ConsultationEv c:list.getListConsult()) {
		for (Ration r :c.getRationList()) {
			for(AlimentRation ar:r.getAlimentList())
				System.out.println(ar.getNom());
		}
	}
}
}
