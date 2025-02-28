package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import DataStruct.CoefP;
import DataStruct.SupplementalvariableP;
import Enumerise.VariableKind;
import application.TextConstant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@SuppressWarnings("rawtypes")
public class ConsultationEv implements Serializable, Comparable, Cloneable{
	static private final long serialVersionUID = 101L;
private LocalDate date;
private LocalDate pdate;
private String objet;
private String UUID;
private String observation;
private String CRendu=new String("");
private float Poids=0;
private float PoidsIdeal=0;
private boolean PoidsIdealex=false;
private float Boisson=0;
private float TauxMG=20;
private boolean suivi=false;
private String bcs="";
private String activite;
private int MCS=3;
private float k2value=1;
private float k1value=1;
private String physio;
private String RefString="";
private float k3value=1;
private String Pathologie;
private String version=TextConstant.VERSION.nameToString();
private float k4value=1;
private String autreobserv;

private String k1d="";
private String k2d="";
private String k3d="";
private String k4d="";
private String k5d="";
private int k6d=0;

private float k5value=1;
private float previousBE=0;
private Ration previousRation=new Ration();
private float ky=1;
private float newBE=0;
private ArrayList <Ration> newRation=new ArrayList<Ration>();
private Map<String, Ration> rationList = new HashMap<String, Ration>();
private int newBCS=4;
private float objectif=0;
private int coefIntG;
private int coefIntL;
private int nbPetit=0;
private float pMere=0;
private ArrayList<String>diseaseRef=new ArrayList<String>();

private ArrayList<SupplementalvariableP> svp=new ArrayList<SupplementalvariableP>();
;
public ConsultationEv (String UUID) {
	this.UUID=UUID;
	for (VariableKind s:VariableKind.values()) {
		svp.add(new SupplementalvariableP(s));
	}
}

public ConsultationEv (){
	UUID=java.util.UUID.randomUUID().toString();
	date=LocalDate.now();
	pdate=LocalDate.now();
Ration r=new Ration();
	rationList.put(r.getUUID(), r);
	
	version=TextConstant.VERSION.nameToString();
	for (VariableKind s:VariableKind.values()) {
		svp.add(new SupplementalvariableP(s));
	}
}

public ConsultationEv (ConsultationEv acons){
	UUID=java.util.UUID.randomUUID().toString();
	date=LocalDate.now();
	pdate=LocalDate.now();
	if (acons.PoidsIdealex){
		this.PoidsIdeal=acons.PoidsIdeal;
		PoidsIdealex=true;
	}
	k1d=acons.getK1d();
	k2d=acons.getK2d();
	k3d=acons.getK3d();
	k4d=acons.getK4d();
	k5d=acons.getK5d();
		pMere=acons.getpMere();
	
	k2value=acons.getK2value();
	k3value=acons.getK3value();
	k4value=acons.getK4value();
	k5value=acons.getK5value();
	coefIntG=acons.getCoefIntG(); 
			coefIntL=acons.getCoefIntL();
			nbPetit=acons.getNbPetit();
		svp=acons.getSuppVarp();
		Ration r=new Ration();
		rationList.put(r.getUUID(), r);
	version=TextConstant.VERSION.nameToString();
}

public void convert() {
	if (newRation.size()>0) {
		for(Ration c:newRation) {
		rationList.put(c.getUUID(), c);
		}
	}
	newRation.clear();
}
public void setVersion(String version) {
	this.version = version;
}
public void update(){
	if (!TextConstant.VERSION.nameToString().equals(version)){
		 int j=Integer.parseInt(TextConstant.NBRATION.nameToString());
		if (this.newRation.size()< j){
			for(int i=this.newRation.size(); i<j+1; i++){
				this.newRation.add(new Ration());
			}
		}
	this.version=TextConstant.VERSION.nameToString();}
}
public String getVersion() {
	return version;
}
public float getPoidsIdeal() {
	return PoidsIdeal;
}
public void setPoidsIdeal(float poidsIdeal) {
	PoidsIdeal = poidsIdeal;
}
public boolean isPoidsIdeal() {
	return PoidsIdealex;
}
public void setPoidsIdeal(boolean poidsIdealex) {
	PoidsIdealex = poidsIdealex;
}
public String getCRendu() {
	return CRendu;
}

public int getNbPetit() {
	return nbPetit;
}

public void setNbPetit(int nbPetit) {
	this.nbPetit = nbPetit;
}
public String getRefString() {
	return RefString;
}
public void setRefString(String refString) {
	RefString = refString;
}
public void setCRendu(String cRendu) {
	CRendu = cRendu;
}
public float getK1value() {
	return k1value;
}
 public void setK1d(String k1d) {
	this.k1d = k1d;
}
 public void setK2d(String k2d) {
	this.k2d = k2d;
}
 public void setK3d(String k3d) {
	this.k3d = k3d;
}
public void setK4d(String k4d) {
	this.k4d = k4d;
}
public void setK5d(String k5d) {
	this.k5d = k5d;
}
public String getK1d() {
	return k1d;
}
public String getK2d() {
	return k2d;
}
public String getK3d() {
	return k3d;
}
public String getK4d() {
	return k4d;
}
public String getK5d() {
	return k5d;
}

public void setK1value(float k1value) {
	this.k1value = k1value;
}



public void setPdate(LocalDate pdate) {
	this.pdate = pdate;
}
public LocalDate getPdate() {
	return pdate;
}
public static long getSerialversionuid() {
	return serialVersionUID;
}

public void setPhysio(String physio) {
	this.physio = physio;
}

public float getpMere() {
	return pMere;
}
public void setpMere(float pMere) {
	this.pMere = pMere;
}
public String getPhysio() {
	return physio;
}
public float getK2value() {
	return k2value;
}
public float getK3value() {
	return k3value;
}

public boolean isSuivi() {
	return suivi;
}

public void setK2value(float k2value) {
	this.k2value = k2value;
}
public void setK3value(float k3value) {
	this.k3value = k3value;
}

public void setBoisson(float boisson) {
	Boisson = boisson;
}
public float getBoisson() {
	return Boisson;
}


public String getUUID() {
	return UUID;
}
public void setObjet(String objet) {
	this.objet = objet;
}
public String getObjet() {
	return objet;
}


public String getActivite() {
	return activite;
}
public String getAutreobserv() {
	return autreobserv;
}

public LocalDate getDate() {
	return date;
}

public float getK4value() {
	return k4value;
}
public float getK5value() {
	return k5value;
}
public float getKy() {
	return ky;
}
public float getNewBE() {
	return newBE;
}
public float getObjectif() {
	return objectif;
}
public String getObservation() {
	return observation;
}
public String getPathologie() {
	return Pathologie;
}
public float getPoids() {
	return Poids;
}
public float getPreviousBE() {
	return previousBE;
}
public float getTauxMG() {
	return TauxMG;
}
public void setActivite(String activite) {
	this.activite = activite;
}
public void setAutreobserv(String autreobserv) {
	this.autreobserv = autreobserv;
}

public void setDate(LocalDate date) {
	this.date = date;
}
public void setK4value(float k4) {
	this.k4value = k4;
}
public void setK5value(float kx) {
	this.k5value = kx;
}
public void setKy(float ky) {
	this.ky = ky;
}
public void setNewBE(float newBE) {
	this.newBE = newBE;
}
public void setPreviousBE(float previousBE) {
	this.previousBE = previousBE;
}
public void setObjectif(float objectif) {
	this.objectif = objectif;
}
public void setObservation(String observation) {
	this.observation = observation;
}
public void setPathologie(String pathologie) {
	Pathologie = pathologie;
}
public void setPoids(float poids) {
	Poids = poids;
}
public void setSuivi(boolean suivi) {
	this.suivi = suivi;
}
public void setTauxMG(float tauxMG) {
	TauxMG = tauxMG;
}
public void setNewRation(Ration newRationa) {
/*	int a=0;
	for(int i=0;i<newRation.size();i++){
		if (newRation.get(i).getUUID().equals(newRationa.getUUID())){
			a=i;
			
		}
	}
newRation.remove(a);
newRation.add(newRationa);*/
	if (rationList.containsKey(newRationa.getUUID())) {
	rationList.replace(newRationa.getUUID(),newRationa);}else {
		this.rationList.put(newRationa.getUUID(), newRationa);
	}

}
public void addNewRation(Ration newRat) {
	this.rationList.put(newRat.getUUID(), newRat);
}

public String getBcs() {
	return bcs;
}
public int getCoefIntG() {
	return coefIntG;
}
public int getCoefIntL() {
	return coefIntL;
}
public void setBcs(String bcs) {
	this.bcs = bcs;
}
public void setCoefIntG(int coefIntG) {
	this.coefIntG = coefIntG;
}
public void setCoefIntL(int coefIntL) {
	this.coefIntL = coefIntL;
}
public void setPreviousRation(Ration previousRation) {
	this.previousRation = previousRation;
}
public ArrayList<Ration> getRationList() {
	Comparator<Ration> rationComparator = Comparator.comparing(Ration::isActual).thenComparing(Ration::getNom);

	ArrayList<Ration>r=new ArrayList<Ration>(rationList.values());
	Collections.sort(r, rationComparator);
	return r;
}
public Ration getPreviousRation() {
	return previousRation;
}
public Ration getRationByUUID(String UUID) {
return rationList.get(UUID);
}
public void setRationByUUID(String UUID, Ration r) {
	rationList.replace(UUID, r);
}
public void removeRationByUUID(String UUID) {
	rationList.remove(UUID);
}



public void update (ConsultationEv trancon) {
	this.Poids=trancon.getPoids();
	this.k1value=trancon.getK1value();
	this.k2value=trancon.getK2value();
	this.k3value=trancon.getK3value();
	this.k4value=trancon.getK4value();
	this.k5value=trancon.getK5value();
	this.RefString=trancon.getRefString();
	this.k1d=trancon.getK1d();
	this.k2d=trancon.getK2d();
	this.k3d=trancon.getK3d();
	this.k4d=trancon.getK4d();
	this.k5d=trancon.getK5d();
	this.svp=trancon.getSuppVarp();
	this.diseaseRef=trancon.getDiseaseRef();
	}
public void setK6d(int k6d) {
	this.k6d = k6d;
}public void setSuppVarp(ObservableList<SupplementalvariableP> svpp) {
	for (SupplementalvariableP s:svp) {
		for (SupplementalvariableP sp:svpp) {
			if (s.getVariable().equals(sp.getVariable())) {
				s.setValue(sp.getValue());
			}
		}	
	}
	
	
}
 public ArrayList<String> getDiseaseRef() {
	return diseaseRef;
}
 public void setDiseaseRef(ArrayList<String> diseaseRef) {
	this.diseaseRef = diseaseRef;
}
 public void setDiseaseRef(ObservableList<ReferenceEv> disease) {
	this.diseaseRef.clear();
	for (ReferenceEv r:disease) {
	this.diseaseRef.add(r.getUUID());
	}
}
 public void addDiseaseRef(String s) {
		this.diseaseRef.add(s);
 }
public ArrayList<SupplementalvariableP> getSuppVarp() {
	return svp;
}
@Override
public int compareTo(Object o) {
    /* For Ascending order*/
    return this.date.compareTo(((ConsultationEv)o).getDate());
    /* For Descending order do like this */
    //return compareage-this.studentage;
}
 public int getMCS() {
	return MCS;
}
  public int getK6d() {
	return k6d;
}
  public int getNewBCS() {
	return newBCS;
}
  public void setNewBCS(int newBCS) {
	this.newBCS = newBCS;
}
 public void setMCS(int mCS) {
	MCS = mCS;
}
 public ConsultationEv clone() {
		ConsultationEv o = null;
		try {
			// On r??cup??re l'instance ?? renvoyer par l'appel de la 
			// m??thode super.clone()
			o =(ConsultationEv) super.clone();
			o.UUID=java.util.UUID.randomUUID().toString();
			o.rationList=new HashMap<String, Ration>();
			o.newRation=new ArrayList<Ration>();
			for (Ration r:getRationList()) {
				o.addNewRation(r.clone());
			}
			o.svp=new ArrayList<SupplementalvariableP>();
			for (SupplementalvariableP r:svp) {
				o.svp.add(r.clone());
			}
		} catch(CloneNotSupportedException cnse) {
			// Ne devrait jamais arriver car nous impl??mentons 
			// l'interface Cloneable
			cnse.printStackTrace(System.err);
		}
		// on renvoie le clone
		return o;
	}
}
