package model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataStruct.NutrientP;
import Enumerise.AAEnum;
import Enumerise.ContEnum;
import Enumerise.FoodKind;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;

public class AlimentEv implements Serializable {
	static private final long serialVersionUID = 101L;
	public int consistent=1;
	public String UUID;
	private String nom="";
	private GroupAlim group;
	private FoodKind foodKind;
	private String ingredients=""; 
	private double prix=0;
	private String categoriePrix="i";
private String marque="";
	private ArrayList<String> indication=new ArrayList<String>();
	private int espece;
	private ArrayList<String>Especes=new ArrayList<String>();
private String gamme="";
private String presentation="";
private float quantInt=0;
private ContEnum cont=ContEnum.NO;
public boolean deprecated=false;
public String DataB="6";
	//contenu en pour %
private Map<Nutrient, NutrientQuantity> valMap=new HashMap<Nutrient, NutrientQuantity>();

	//

	public AlimentEv() {
UUID=java.util.UUID.randomUUID().toString();

foodKind=FoodKind.MEN;
group=GroupAlim.AIDE;
addIndicat(AlimIndic.PHYS.getCoef());

	}
	public AlimentEv(String uuid) {
		UUID=uuid;
	
		group=GroupAlim.AIDE;
	

			}
	public AlimentEv(AlimentUnif al) {
		UUID=al.getUUID();
	
		group=al.getGroup();
		foodKind=al.getTypeAliment();
		categoriePrix=al.getCategoriePrix();
	Especes=al.getEspeces();
	presentation =al.getPresentation();
	quantInt=al.getQuantInt();
	cont=al.getCont();
	deprecated=al.isDeprecated();
DataB=al.getDataB();
update(al);
			}
	public String getUUID() {
		return UUID;
	}
	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}
	public String getPresentation() {
		return presentation;
	}
	public void setCategoriePrix(String categoriePrix) {
		this.categoriePrix = categoriePrix;
	}
	public String getCategoriePrix() {
		return categoriePrix;
	}
	public void setPrix(double prix) {
		this.prix = prix;
	}
	public double getPrix() {
		return prix;
	}
	public String getFamillyBrand() {
		String s= new String();
				switch (this.getTypeAliment()){
				case MEN:
					case BARF:
						s=this.getGroup().nameToString();
						break;
					case COMPLET:
						s=this.getMarque();
						break;
					default:
						s=this.getMarque();
					break;
				}
				return s;
			}
	
	public void update(AlimentUnif aliment){
		if (!aliment.getNom().equals("")){
			this.setNom(aliment.getNom());
		}
		if (!aliment.getGamme().equals("")){
			this.setGamme(aliment.getGamme());
		}
		if (!aliment.getGroup().equals("")){
			this.setGroup(aliment.getGroup());
		}
		if (!aliment.getMarque().equals("")){
			this.setMarque(aliment.getMarque());
		}
		if (!aliment.getIngredients().equals("")){
			this.setIngredients(aliment.getIngredients());
		}
		
		if (aliment.getIndicat().size()>1){
			this.setIndicat(aliment.getIndicat());
		}else if(!aliment.getIndicat().get(0).equals(AlimIndic.PHYS.nameToString())){
			this.setIndicat(aliment.getIndicat());
			}
		for(AAEnum elem:AAEnum.values()){
			if (aliment.isNutrientAcideAmine(elem)){
				this.setNutrient(aliment.getNutrientAcideAmine(elem), elem);
			}
		}
		for(NutrientBase elem:NutrientBase.values()){
			if(aliment.isNutrientBase(elem)){
				this.setNutrient(aliment.getNutrientBase(elem), elem);
			}
		}
		for(NutrientMin elem:NutrientMin.values()){
			if(aliment.isNutrientMin(elem)){
				this.setNutrient(aliment.getNutrientMin(elem), elem);
			}
		}
		for(NutrientMacro elem:NutrientMacro.values()){
			if(aliment.isNutrientMacro(elem)){
				this.setNutrient(aliment.getNutrientMacro(elem), elem);
			}
		}
		for(NutrientLipid elem:NutrientLipid.values()){
			if(aliment.isNutrientLipid(elem)){
				this.setNutrient(aliment.getNutrientLipid(elem), elem);
			}
		}
		for(NutrientVitam elem:NutrientVitam.values()){
			if(aliment.isNutrientVitam(elem)){
				this.setNutrient(aliment.getNutrientVitam(elem), elem);
			}
		}
		for(NutrientOther elem:NutrientOther.values()){
			if(aliment.isNutrientOther(elem)){
				this.setNutrient(aliment.getNutrientOther(elem), elem);
			}
		}
	}

	public void setNutrient(float a,Nutrient enu){
	valMap.put(enu, new NutrientQuantity(enu, a));
	}
	public float getNutrient(Nutrient enu) {
	NutrientQuantity v=valMap.get(enu);
		return( v==null?0:v.getValue()  );
	}

public boolean isNutrient(Nutrient enu) {
	return valMap.containsKey(enu);
}
public void removeNutrient(Nutrient enu) {
	valMap.remove(enu);
}
	


	public float getProtEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  3.5F *  this.getNutrient(NutrientBase.PROTEINE) /(  3.5F *  this.getNutrient(NutrientBase.PROTEINE) + 8.5F* this.getNutrient(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrient(NutrientBase.ENA)));
						default:
				return	  4F *  this.getNutrient(NutrientBase.PROTEINE) /(  4F *  this.getNutrient(NutrientBase.PROTEINE) + 9F* this.getNutrient(NutrientBase.LIPIDE)+ 4F*( this.getNutrient(NutrientBase.ENA)));
						}
	}
	public float getENAEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  3.5F *  this.getNutrient(NutrientBase.ENA) /(  3.5F *  this.getNutrient(NutrientBase.PROTEINE) + 8.5F* this.getNutrient(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrient(NutrientBase.ENA)));
						default:
				return	  4F *  this.getNutrient(NutrientBase.ENA) /(  4F *  this.getNutrient(NutrientBase.PROTEINE) + 9F* this.getNutrient(NutrientBase.LIPIDE)+ 4F*( this.getNutrient(NutrientBase.ENA)));
						}
	}
	public float getLipEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  8.5F *  this.getNutrient(NutrientBase.LIPIDE) /(  3.5F *  this.getNutrient(NutrientBase.PROTEINE) + 8.5F* this.getNutrient(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrient(NutrientBase.ENA)));
						default:
				return	  9F *  this.getNutrient(NutrientBase.LIPIDE) /(  4F *  this.getNutrient(NutrientBase.PROTEINE) + 9F* this.getNutrient(NutrientBase.LIPIDE)+ 4F*( this.getNutrient(NutrientBase.ENA)));
						}
	}
	public void setEspece(int espece) {
		this.espece = espece;
	}
	public String getGamme() {
		return gamme;
	}
	public void setGamme(String gamme) {
		this.gamme = gamme;
	}
	public void setGroup(String string) {
		
		this.group =GroupAlim.StringToGroup(string);
	}
	public void setGroup(GroupAlim string) {
		
		this.group =string;
	}
	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public void setTypeAliment(FoodKind typeAliment) {
		this.foodKind = typeAliment;
	}
	public int getEspece() {
		return espece;
	}
	public GroupAlim getGroup() {
		return group;
	}
	public String getIngredients() {
		return ingredients;
	}
	public String getNom() {
		return nom;
	}
	public FoodKind getTypeAliment() {
		if (foodKind==null) {
			foodKind=FoodKind.MEN;
;		}
		return foodKind;
	}
public String getMarque() {
	return marque;
}
public void setMarque(String marque) {
	this.marque = marque;
}

	public ArrayList<String> getIndicat(){
		try{
		 if (indication.isEmpty()){
			 indication.add(AlimIndic.PHYS.nameToString());
		 }}
		catch(NullPointerException e){
			indication=new ArrayList<String>();
			 indication.add(AlimIndic.PHYS.nameToString());
		}
		 return indication;
		
	 }
	public String getOneIndicat(int i){
		try{
		 if (indication.isEmpty()){
			 indication.add(AlimIndic.PHYS.nameToString());
		 }}
		catch(NullPointerException e){
			indication=new ArrayList<String>();
			 indication.add(AlimIndic.PHYS.nameToString());
		}
		 return indication.get(i);
		
	 }
	public void addIndicat(AlimIndic ind){
		indication.add(ind.nameToString());
	}
	public void setIndicat(List<String> ind){
		this.indication=(ArrayList<String>) ind;
	/*	this.indication.removeAll(this.indication);
		for (int i=0; i<ind.size(); i++){
		this.indication.add(ind.get(i));
		}*/
		
	}
	public void addIndicat(int i){
		   AlimIndic ind= AlimIndic.IntToGroup(i);
		indication.add(ind.nameToString());
	}
	public void removeAllIndicat(){
		indication.removeAll(indication);
	}
	public float []resize(float[] obj, int size) {
		float[] export= new float [size];
		for(int i=0; i<obj.length; i++){
			export[i]=obj[i];
		}
		
		return export;
		
	}
	public boolean []resize(boolean[] obj, int size) {
	boolean[] export= new boolean [size];
		for(int i=0; i<obj.length; i++){
			export[i]=obj[i];
		}
		for (int i=obj.length-1; i<size; i++){
			export[i]=false;
		}
		return export;
		
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	
	public void updateByNutrient(NutrientP np, float unitF, float ProtValue) {
	boolean present=false;
	float value=0F;
		if (np.getQuantity()=="") {
			present=false;
		}else {
			
			present=true;
			if (unitF==-1 & np.getMNE().equals(MainNutrientEnum.AMA)) {
				
				value=Float.parseFloat(np.getQuantity())*np.getConverter();
				
				
			}else if (unitF!=-1 & np.getMNE().equals(MainNutrientEnum.AMA)) {
				value=100*Float.parseFloat(np.getQuantity())*np.getConverter()/ProtValue;
			}
			else {
			value=Float.parseFloat(np.getQuantity())*np.getConverter()*unitF;}}
		
		
	
		
	
		if (present) {
		this.setNutrient(value, np.getNutrientEnum());}
		else {
			this.removeNutrient(np.getNutrientEnum());
		}
	
		}
	
	public ArrayList<String> getEspeces() {
		return Especes;
	}
	public void setEspeces(ArrayList<String> especes) {
		Especes = especes;
	}
	public void addEspeces(String Esp) {
		Especes.add(Esp);
	}
	
	public boolean isEspece(String Esp) {
		if (Esp.equals("ALL")) {
			return true;
		}else
		{
			for (String s:Especes) {
				if (s.equals(Esp)) { 
					return true;
				}
			}
		}
	return false;
	}
	public int getConsistent() {
		return consistent;
	}
	public void setConsistent(int consistent) {
		this.consistent = consistent;
	}
	 public ContEnum getCont() {
		 if(cont!=null) {
				return cont;
		 }else {
				return ContEnum.NO;
		 }
	
	}
	 public float getQuantInt() {
		return quantInt;
	}
	 public void setCont(ContEnum cont) {
		this.cont = cont;
	}
	 public void setCont(int cont) {

		this.cont = ContEnum.byId(cont);
	}
	 public void setQuantInt(float quantInt) {
		this.quantInt = quantInt;
	}
	 public boolean isDeprecated() {
		return deprecated;
	}
	 public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
	 public void setDeprecated(int deprecated) {
		this.deprecated = (deprecated==1);
	}
	 public String getDataB() {
		return DataB;
	}
	 public void setDataB(String dataB) {
		DataB = dataB;
	}
	 
	
	 public AlimentEv clone() {
	        AlimentEv o = new AlimentEv();
	    	o.valMap=valMap;
	            o.setNom("(Dup) "+getNom());
	           o.group=this.getGroup();
	           o.foodKind=this.getTypeAliment();
	           o.ingredients=this.getIngredients();
	           o.prix=this.getPrix();
	           o.categoriePrix=this.getCategoriePrix();
	           o.marque=this.getMarque();
	           o.indication=this.indication;
	        		   o.espece=espece;
	        		   o.Especes=Especes;
	        		   o.gamme=gamme;
	        		   o.presentation=presentation;
	        		o.valMap=valMap;
	           o.quantInt=this.quantInt;
	           o.cont=this.cont;
	           o.deprecated=this.deprecated;
	           o.DataB=this.DataB;
	           
	           
	        
	        // on renvoie le clone
	        return o;
	    }
	public float getEner(Espece chien) {
		// TODO Auto-generated method stub
		return (float) (3.5*getNutrient(NutrientBase.ENA)+3.5*getNutrient(NutrientBase.PROTEINE)+8.5*getNutrient(NutrientBase.LIPIDE));
	}
}
