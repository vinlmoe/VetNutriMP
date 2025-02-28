package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import application.TextConstant;

public class Consultation implements Serializable{
	static private final long serialVersionUID = 101L;
private Date date;
private Date pdate;
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
private NEC nec=NEC.A5;
private String activite;
private k2 coeffactive=k2.NORMAL;
private float k2value=1;
private float k1value=1;
private String physio;

private k3 coeffage=k3.ADULTE;
private PathoEnum pathoE=PathoEnum.NORM;
private String RefString="";
private float k3value=1;
private String Pathologie;
private String version=TextConstant.VERSION.nameToString();
private float k4=1;
private String autreobserv;
private float RPCobj=60;
private float O3obj=1;
private float O6obj=3;
private float EPAobj=0.25F;
private float Fibrobj=4;
private float Calciumobj=1.5F;
private float CalPhosobj=1.3F;
private int k1d=0;
private int k2d=0;
private int k3d=0;
private int k4d=0;
private int k5d=0;
private int k6d=0;

private float kx=1;
private float previousBE=0;
private Ration previousRation=new Ration();
private float ky=1;
private float newBE=0;
private ArrayList <Ration> newRation=new ArrayList<Ration>();
private float objectif=0;
private GesCoef ges=GesCoef.ER;
private LactCoef Lac=LactCoef.ER;
private int nbPetit=0;

private float pMere=0;

public Consultation (String UUID) {
	this.UUID=UUID;
	
}

public Consultation (){
	UUID=java.util.UUID.randomUUID().toString();
	date=new Date();
	pdate=new Date();

	newRation.add(new Ration());
	newRation.add(new Ration());
	newRation.add(new Ration());
	newRation.add(new Ration());
	newRation.add(new Ration());
	newRation.add(new Ration());
	newRation.add(new Ration());
	version=TextConstant.VERSION.nameToString();
}

public Consultation (Consultation acons){
	UUID=java.util.UUID.randomUUID().toString();
	date=new Date();
	pdate=new Date();
	if (acons.PoidsIdealex){
		this.PoidsIdeal=acons.PoidsIdeal;
		PoidsIdealex=true;
	}
	coeffactive=acons.getCoeffactive();
	coeffage=acons.getCoeffage();
	
		pMere=acons.getpMere();
	
	k2value=acons.getK2value();
	k3value=acons.getK3value();
	k4=acons.getK4();
	kx=acons.getKx();
	ky=acons.getKy();
	
			
	
	
	newRation.add(new Ration());

	
	version=TextConstant.VERSION.nameToString();
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
	if (CRendu!=null) {
	return CRendu;}else {
		return "";
	}
}
public LactCoef getLac() {
	if (Lac==null) {
		Lac=LactCoef.ER;
	}
	return Lac;
}
public int getNbPetit() {
	return nbPetit;
}
public void setLac(LactCoef lac) {
	Lac = lac;
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
 public void setK1d(int k1d) {
	this.k1d = k1d;
}
 public void setK2d(int k2d) {
	this.k2d = k2d;
}
 public void setK3d(int k3d) {
	this.k3d = k3d;
}
public void setK4d(int k4d) {
	this.k4d = k4d;
}
public void setK5d(int k5d) {
	this.k5d = k5d;
}
public int getK1d() {
	return k1d;
}
public int getK2d() {
	return k2d;
}
public int getK3d() {
	return k3d;
}
public int getK4d() {
	return k4d;
}
public int getK5d() {
	return k5d;
}

public void setK1value(float k1value) {
	this.k1value = k1value;
}

public PathoEnum getPathoE() {
	if(pathoE==null){
		this.pathoE=PathoEnum.NORM;
	}
	return pathoE;
}
public void setPathoE(PathoEnum pathoE) {
	this.pathoE = pathoE;
}
public void setPdate(Date pdate) {
	this.pdate = pdate;
}
public Date getPdate() {
	return pdate;
}
public static long getSerialversionuid() {
	return serialVersionUID;
}

public void setPhysio(String physio) {
	this.physio = physio;
}
public GesCoef getGes() {
	return ges;
}
public void setGes(GesCoef ges) {
	this.ges = ges;
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
public NEC getNec() {
	return nec;
}
public void setK2value(float k2value) {
	this.k2value = k2value;
}
public void setK3value(float k3value) {
	this.k3value = k3value;
}
public void setNec(NEC nec) {
	this.nec = nec;
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
public k2 getCoeffactive() {
	return coeffactive;
}
public k3 getCoeffage() {
	return coeffage;
}
public Date getDate() {
	return date;
}
public float getK4() {
	return k4;
}
public float getKx() {
	return kx;
}
public float getK4value() {
	return k4;
}
public float getK5value() {
	return kx;
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
public void setCoeffactive(k2 coeffactive) {
	this.coeffactive = coeffactive;
}
public void setCoeffage(k3 coeffage) {
	this.coeffage = coeffage;
}
public void setDate(Date date) {
	this.date = date;
}
public void setK4(float k4) {
	this.k4 = k4;
}
public void setKx(float kx) {
	this.kx = kx;
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
public void setNewRation(Ration newRationa, int i) {
/*	int a=0;
	for(int i=0;i<newRation.size();i++){
		if (newRation.get(i).getUUID().equals(newRationa.getUUID())){
			a=i;
			
		}
	}
newRation.remove(a);
newRation.add(newRationa);*/
	newRation.set(i,newRationa);

}
public void addNewRation(Ration newRat, int i) {
	this.newRation.add(i, newRat);
}
public void setCalciumobj(float calciumobj) {
	Calciumobj = calciumobj;
}
public void setCalPhosobj(float calPhosobj) {
	CalPhosobj = calPhosobj;
}
public void setEPAobj(float ePAobj) {
	EPAobj = ePAobj;
}
public void setFibrobj(float fibrobj) {
	Fibrobj = fibrobj;
}
public void setO3obj(float o3obj) {
	O3obj = o3obj;
}public void setO6obj(float o6obj) {
	O6obj = o6obj;
}
public void setRPCobj(float rPCobj) {
	RPCobj = rPCobj;
}
public float getCalciumobj() {
	return Calciumobj;
}
public float getCalPhosobj() {
	return CalPhosobj;
}
public float getEPAobj() {
	return EPAobj;
}
public float getFibrobj() {
	return Fibrobj;
}
public float getO3obj() {
	return O3obj;
}public float getO6obj() {
	return O6obj;
}public float getRPCobj() {
	return RPCobj;
}
public void setPreviousRation(Ration previousRation) {
	this.previousRation = previousRation;
}
public ArrayList<Ration> getNewRation() {
	return newRation;
}

public Ration getPreviousRation() {
	return previousRation;
}@Override
protected Object clone() throws CloneNotSupportedException {
	// TODO Auto-generated method stub
	return super.clone();
}
public Ration getRationByUUID(String UUID) {
	int a=0;
	boolean touch=false;
	for(int i=0;i<newRation.size();i++){
		if (newRation.get(i).getUUID().equals(UUID)){
			a=i;
			touch=true;
		}
	}
	if (touch){
 return this.newRation.get(a);
	}else{
		return null;}
}
public Ration removeRationByUUID(String UUID) {
	int a=0;
	boolean touch=false;
	for(int i=0;i<newRation.size();i++){
		if (newRation.get(i).getUUID().equals(UUID)){
			a=i;
			touch=true;
		}
	}
	if (touch){
 return this.newRation.remove(a);
	}else{
		return null;}
}
}
