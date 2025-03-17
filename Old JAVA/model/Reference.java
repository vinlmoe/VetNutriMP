package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Enumerise.AAEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;

public class Reference implements Serializable{
	static private final long serialVersionUID = 1L;
	private float NutrientAcideAmineVar [][]=new float[22][4];
	private float NutrientBaseVar [][]=new float[100][4];
	private float NutrientLipidVar [][]=new float[100][4];
	private float NutrientMacroVar [][]=new float[100][4];
	private float NutrientMinVar [][]=new float[100][4];
	private float NutrientVitamVar [][]=new float[100][4];
	private float NutrientOtherVar [][]=new float[100][4];
	private float NutrientAnalysisVar [][]=new float[100][4];
	private String NutrientAcideAmineBib [][]=new String[22][4];
	private String NutrientBaseBib [][]=new String[100][4];
	private String NutrientLipidBib [][]=new String[100][4];
	private String NutrientMacroBib [][]=new String[100][4];
	private String NutrientMinBib [][]=new String[100][4];
	private String NutrientVitamBib [][]=new String[100][4];
	private String NutrientOtherBib [][]=new String[100][4];
	private String NutrientAnalysisBib [][]=new String[100][4];
	
	private boolean NutrientAcideAminePresence [][]=new boolean[22][4];
	private boolean NutrientBasePresence [][]=new boolean[100][4];
	private boolean NutrientLipidPresence [][]=new boolean[100][4];
	private boolean NutrientMacroPresence [][]=new boolean[100][4];
	private boolean NutrientMinPresence [][]=new boolean[100][4];
	private boolean NutrientVitamPresence [][]=new boolean[100][4];
	private boolean NutrientOtherPresence [][]=new boolean[100][4];
	private boolean NutrientAnalysisPresence [][]=new boolean[100][4];
	public String UUID;
	private String nom="";
	private String descriprion="";
	private Espece espece=Espece.CHIEN;
	private StadePhysio sPhysio= StadePhysio.ADULTE;
	
	public Reference() {

		//

		
	UUID=java.util.UUID.randomUUID().toString();

	for (float[] element:NutrientAcideAmineVar){
		
		element[0]=0;
		element[1]=0;
		element[2]=0;
		element[3]=0;
	}
	for (float[] element:NutrientBaseVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float []element:NutrientLipidVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float []element:NutrientMacroVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float[] element:NutrientMinVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float []element:NutrientVitamVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float[] element:NutrientOtherVar){
			element[0]=0;
			element[1]=0;
			element[2]=0;
			element[3]=0;
	}
	for (float[] element:NutrientAnalysisVar){
		element[0]=0;
		element[1]=0;
		element[2]=0;
		element[3]=0;
}for (String[] element:NutrientAcideAmineBib){
	
	element[0]="";
	element[1]="";
	element[2]="";
	element[3]="";
}
for (String[] element:NutrientBaseBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String []element:NutrientLipidBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String []element:NutrientMacroBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String[] element:NutrientMinBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String []element:NutrientVitamBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String[] element:NutrientOtherBib){
		element[0]="";
		element[1]="";
		element[2]="";
		element[3]="";
}
for (String[] element:NutrientAnalysisBib){
	element[0]="";
	element[1]="";
	element[2]="";
	element[3]="";
}
	for (boolean []element:NutrientAcideAminePresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean[] element:NutrientBasePresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean[] element:NutrientLipidPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean []element:NutrientMacroPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean []element:NutrientMinPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean[] element:NutrientVitamPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean[] element:NutrientOtherPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}
	for (boolean[] element:NutrientAnalysisPresence){
		element[0]=false;
		element[1]=false;
		element[2]=false;
		element[3]=false;
	}

		// TODO Auto-generated constructor stub
	}
	public String getUUID() {
		return UUID;
	}

	//mise ?? jour des valeurs
	public void setNutrient(float a,AAEnum enu, Reflevel ref){
		this.NutrientAcideAmineVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientAcideAminePresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setsPhysio(StadePhysio sPhysio) {
		this.sPhysio = sPhysio;
	}
	public StadePhysio getsPhysio() {
		return sPhysio;
	}
	public void setNutrient(float a,NutrientBase enu, Reflevel ref){
		this.NutrientBaseVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientBasePresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientLipid enu, Reflevel ref){
		this.NutrientLipidVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientLipidPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientMacro enu, Reflevel ref){
		this.NutrientMacroVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientMacroPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientMin enu, Reflevel ref){
		this.NutrientMinVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientMinPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientVitam enu, Reflevel ref){
		this.NutrientVitamVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientVitamPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientOther enu, Reflevel ref){
		this.NutrientOtherVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientOtherPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrient(float a,NutrientAnalysis enu, Reflevel ref){
		this.NutrientAnalysisVar[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientAnalysisPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	//orise des valeurs
	public float getNutrient(AAEnum enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientAcideAmineVar[enu.getCoef()][ref.getCoef()];}
		else return -1;
	
	}
	public float getNutrient(NutrientBase enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientBaseVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public float getNutrient(NutrientLipid enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
	
		return this.NutrientLipidVar[enu.getCoef()][ref.getCoef()];
	}else return-1;
	}
	public float getNutrient(NutrientMacro enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientMacroVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public float getNutrient(NutrientMin enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientMinVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public float getNutrient(NutrientVitam enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientVitamVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public float getNutrient(NutrientOther enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientOtherVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public float getNutrient(NutrientAnalysis enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientAnalysisVar[enu.getCoef()][ref.getCoef()];
		}else return-1;
	}
	public boolean isNutrient(AAEnum enu, Reflevel ref){
		return this.NutrientAcideAminePresence[enu.getCoef()][ref.getCoef()];
	
	}
	public boolean isNutrient(NutrientBase enu, Reflevel ref){
		return this.NutrientBasePresence[enu.getCoef()][ref.getCoef()];

	}
	public boolean isNutrient(NutrientLipid enu, Reflevel ref){
		return this.NutrientLipidPresence[enu.getCoef()][ref.getCoef()];
		
	}
	public boolean isNutrient(NutrientMacro enu, Reflevel ref){
		return this.NutrientMacroPresence[enu.getCoef()][ref.getCoef()];
	
	}
	public boolean isNutrient(NutrientMin enu, Reflevel ref){
		return this.NutrientMinPresence[enu.getCoef()][ref.getCoef()];
		
	}
	public boolean isNutrient(NutrientVitam enu, Reflevel ref){
		return this.NutrientVitamPresence[enu.getCoef()][ref.getCoef()];
	
	}
	public boolean isNutrient(NutrientOther enu, Reflevel ref){
		return this.NutrientOtherPresence[enu.getCoef()][ref.getCoef()];
		
	}
	public boolean isNutrient(NutrientAnalysis enu, Reflevel ref){
		return this.NutrientAnalysisPresence[enu.getCoef()][ref.getCoef()];
		
	}
	public void removeNutrient(AAEnum enu, Reflevel ref){
		this.NutrientAcideAmineVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientAcideAminePresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientBase enu, Reflevel ref){
		this.NutrientBaseVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientBasePresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientLipid enu, Reflevel ref){
		this.NutrientLipidVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientLipidPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientMacro enu, Reflevel ref){
		this.NutrientMacroVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientMacroPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientMin enu, Reflevel ref){
		this.NutrientMinVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientMinPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientVitam enu, Reflevel ref){
		this.NutrientVitamVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientVitamPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientOther enu, Reflevel ref){
		this.NutrientOtherVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientOtherPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	public void removeNutrient(NutrientAnalysis enu, Reflevel ref){
		this.NutrientAnalysisVar[enu.getCoef()][ref.getCoef()]=0;
		this.NutrientAnalysisPresence[enu.getCoef()][ref.getCoef()]= false;
	}
	
	//Gestion des R??f??rence Bibliographiques
	public void setNutrientBib(String a,AAEnum enu, Reflevel ref){
		this.NutrientAcideAmineBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientAcideAminePresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientBase enu, Reflevel ref){
		this.NutrientBaseBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientBasePresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientLipid enu, Reflevel ref){
		this.NutrientLipidBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientLipidPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientMacro enu, Reflevel ref){
		this.NutrientMacroBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientMacroPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientMin enu, Reflevel ref){
		this.NutrientMinBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientMinPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientVitam enu, Reflevel ref){
		this.NutrientVitamBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientVitamPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientOther enu, Reflevel ref){
		this.NutrientOtherBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientOtherPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public void setNutrientBib(String a,NutrientAnalysis enu, Reflevel ref){
		this.NutrientAnalysisBib[enu.getCoef()][ref.getCoef()]=a;
		this.NutrientAnalysisPresence[enu.getCoef()][ref.getCoef()]= true;
	}
	public String getNutrientBib(AAEnum enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientAcideAmineBib[enu.getCoef()][ref.getCoef()];
	} else {
		 String re="";		
		return re;
	}
	}
	public String getNutrientBib(NutrientBase enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientBaseBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientLipid enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
	
		return this.NutrientLipidBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientMacro enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientMacroBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientMin enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientMinBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientVitam enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientVitamBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientOther enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientOtherBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getNutrientBib(NutrientAnalysis enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.NutrientAnalysisBib[enu.getCoef()][ref.getCoef()];
		} else {
			 String re="";		
			return re;
		}
	}
	public String getEspece() {
		return (""+espece.getCategorie());
	}
	public void setEspece(Espece espece) {
		this.espece = espece;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public void setDescriprion(String descriprion) {
		this.descriprion = descriprion;
	}public String getDescriprion() {
		return descriprion;
	}public String getNom() {
		return nom;
	}
}
