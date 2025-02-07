package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Vet implements Serializable{
	static private final long serialVersionUID = 101L;
	private String Nom="";
	private String Prenom="";
	private String Adresse="";
	private String CodePost="";
	private String Ordre="";
	private String LNumbre="";
	private String Ville="";
	private String Language="Français";
	private String UUID="";
	private AlimDBList alDBL=new AlimDBList();
	private ArrayList<Advise> adviseList=new ArrayList<Advise>();
	private float scale=13;
	private boolean Etud=true;
	private boolean nouv=true;
	public Vet(){
		UUID=java.util.UUID.randomUUID().toString();
		nouv=true;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public boolean isNouv() {
		return nouv;
	}
	public String getVille() {
		return Ville;
	}
	public void setVille(String ville) {
		Ville = ville;
	}
	public void setNouv(boolean nouv) {
		this.nouv = nouv;}
	
	public void setAdresse(String adresse) {
		Adresse = adresse;
	}
	public void setCodePost(String codePost) {
		CodePost = codePost;
	}
	public void setEtud(boolean etud) {
		Etud = etud;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}
	 public float getScale() {
		return scale;
	}
	public void setLNumbre(String lNumbre) {
		LNumbre = lNumbre;
	}
	public void setNom(String nom) {
		Nom = nom;
	}
	public void setOrdre(String ordre) {
		Ordre = ordre;
	}
	public void setPrenom(String prenom) {
		Prenom = prenom;
	}
	public boolean isEtud() {
		return Etud;
	}
	public String getAdresse() {
		return Adresse;
	}
	public String getCodePost() {
		return CodePost;
	}
	
	public String getLNumbre() {
		return LNumbre;
	}
	public String getNom() {
		return Nom;
	}
	public String getOrdre() {
		return Ordre;
	}
	public String getPrenom() {
		return Prenom;
	}
	 public String getUUID() {
		return UUID;
	}
public void addAdvise(Advise ad) {
	adviseList.add(ad);
}
public void removeAdvise(Advise ad) {
	adviseList.remove(ad);
}
public ArrayList<Advise> getAdviseList() {
	return adviseList;
}
 public void setAlDBL(AlimDBList alDBL) {
	this.alDBL = alDBL;
}
 public void addAlDBL(AlimDBList alDBL) {
		this.alDBL.addAll(alDBL);
	}
 public AlimDBList getAlDBL() {
	return alDBL;
}
public String getLanguage() {
	return Language;
}
public void setLanguage(String language) {
	Language = language;
}
public void setAdviseList(ArrayList<Advise> adviseList) {
	this.adviseList = adviseList;
}
}
