package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Aliments.pourcentPart;
import Enumerise.AAEnum;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientEnergy;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import application.TextConstant;

public class Ration implements Serializable, Cloneable{
	static private final long serialVersionUID = 1120L;
	private String UUID;
	private int number;
	private String Nom="";
	private float coef=0.0F;
	private float fibrep=4;
	private float transCoef=0;
	private float rpcp=60;
	private float huilep=5;
	private float indus=100;
	private float protAnim=80;
	private float objectComp;
	private String version= TextConstant.VERSION.nameToString();
	private int objectType;
	private boolean actual;
	private List<AlimentRation> alimentList= new ArrayList<AlimentRation>();
	private float EnerTot=0;
	public Ration(){
		UUID=java.util.UUID.randomUUID().toString();
		version= TextConstant.VERSION.nameToString();
	}
	public Ration(String UUID){
		this.UUID=UUID;
		version= TextConstant.VERSION.nameToString();
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setIndus(float indus) {
		this.indus = indus;
	}
	
	public float getIndus() {
		return indus;
	}
	public String getVersion() {
		return version;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getNumber() {
		return number;
	}
	public int size(){
		return alimentList.size();
	}
	public String getUUID() {
		return UUID;
	}
	public boolean isActual() {
		return actual;
	}
	public void setActual(boolean actual) {
		this.actual = actual;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public List<AlimentRation> getAlimentList() {
		return alimentList;
	}
	public void setFibrep(float fibrep) {
		this.fibrep = fibrep;
	}
	public void setHuilep(float huilep) {
		this.huilep = huilep;
	}
	public void setObjectComp(float objectComp) {
		this.objectComp = objectComp;
	}
	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}
	public void setAlimentList(List<AlimentRation> alimentList) {
		this.alimentList = alimentList;
	}
	public void setProtAnim(float protAnim) {
		this.protAnim = protAnim;
	}
	public void setRpcp(float rpcp) {
		this.rpcp = rpcp;
	}
	public float getFibrep() {
		return fibrep;
	}
	public float getHuilep() {
		return huilep;
	}
	public float getObjectComp() {
		return objectComp;
	}
	public int getObjectType() {
		return objectType;
	}
	public float getProtAnim() {
		return protAnim;
	}
	public float getRpcp() {
		return rpcp;
	}

	public void addAliment (AlimentEv alimentEv){
		alimentList.add(new AlimentRation(alimentEv));
		
	}
	
	public float getCoef() {
		return coef;
	}
	public void setCoef(float coef) {
		this.coef = coef;
	}
	public void addAliment (AlimentRation alim) {
		alimentList.add(((AlimentRation)alim.clone()));
	}
	public void addRecette (Recette recette) {
		for (AlimentRation alr:recette.getAlimentList()) {
		addAliment(alr);
		}
	}
	public String getNom() {
		return Nom;
	}
	public void setNom(String nom) {
		Nom = nom;
	}
	
	public void addAlimentUnif (AlimentRation alim){
		if(alim.getUUIDunif()!="nonINDEX") {
	
		boolean premierPassage=true;
		for (int i=0; i< alimentList.size(); i++){
			if (premierPassage) {
			if (alimentList.get(i).getUUIDunif().equals(alim.getUUIDunif()) ){
			
				alimentList.get(i).setQuantite(alimentList.get(i).getQuantite()+alim.getQuantite());
				premierPassage=false;
				
			}
			}
		}
		if (premierPassage) {
			alimentList.add((AlimentRation)alim.clone());
		}
		}
		else {
			alimentList.add((AlimentRation)alim.clone());}
		
		
	}
	public void removeAliment (String UUIDVal){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				alimentList.remove(i);
			}
		}
	}
	public void removeAliment (targetAdjust targ){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getTarget().equals(targ) ){
				alimentList.remove(i);
			}
		}
	}
	public void setQuantiteOfAlim (String UUIDVal, float quant){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setQuantite(quant);
			}
		}
	}
	public void setWeightOfAlim (String UUIDVal, float quant){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setWeight(quant);
			}
		}
	}
	public void setTargetOfAlim (String UUIDVal, targetAdjust targ ){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setTarget(targ);
			}
		}
	}
	public void removeAllAlim(){
		alimentList.removeAll(alimentList);
	}
	public void transfertRation (Ration rat){
		for (AlimentRation alim:rat.getAlimentList()){
			this.addAliment((AlimentRation)alim.clone());
		}
	}
	public float getDensity(){
		float ener=0;
		float qtot=0;
		float rap;
		for(AlimentRation alim:alimentList){
			ener+=alim.getDE()*alim.getQuantite();
			qtot+=alim.getQuantite();
		}
		rap=ener/qtot;
		return rap;
	}
	public float getEnerT(){
		float ener=0;
		float qtot=0;
		float rap;
		for(AlimentRation alim:alimentList){
			ener+=alim.getDE()*alim.getQuantite()/100F;
		
		}
		rap=ener;
		return rap;
	}
	public float getMasse()
	{float qtot=0;

	for(AlimentRation alim:alimentList){
		
		qtot+=alim.getQuantite();
	}

	return qtot;
	}
	public void add(List<AlimentRation> alimList) {
		for(AlimentRation alimrat: alimList){
			this.addAliment(alimrat);
		} 
	}
	public void addUnif(List<AlimentRation> alimList) {
		for(AlimentRation alimrat: alimList){
			this.addAlimentUnif(alimrat);
		
		} 
	}
	public float getNutrient(Nutrient nut){
		if (nut.getMNE().equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)) {
			return getNutrientAna((NutrientAnalysis) nut);
		}
		float quantite=0;
		for(AlimentRation alim:alimentList){
			quantite+=alim.getNutrient(nut)*alim.getQuantite()/100;
			
		}
		return quantite;
	}
	public float getNutrientAna(NutrientAnalysis en){
		float quantite=0;
	
		switch(en) {
		case o6o3:
			return getNutrient(NutrientLipid.O6)/getNutrient(NutrientLipid.O3);
		case ZnCu:
			return getNutrient(NutrientMin.ZN)/getNutrient(NutrientMin.CU);
		case PCa:
			return getNutrient(NutrientMacro.CAL)/getNutrient(NutrientMacro.PHOS);
		case PhosphProt:
			return getNutrient(NutrientBase.PROTEINE)/getNutrient(NutrientMacro.PHOS);
		case NaK:
			return getNutrient(NutrientMacro.K)/getNutrient(NutrientMacro.NA);
		case nonOsPhos:
			return (float) (100* ( getNutrient(NutrientMacro.PHOS)-  getNutrient(NutrientMacro.CAL)/2)/  getNutrient(NutrientMacro.PHOS));
		case nonOsProt:
			return   100*(getNutrient(NutrientBase.PROTEINE)-  3*getNutrient(NutrientMacro.CAL))/  getNutrient(NutrientBase.PROTEINE);
		case nonOsPP:
			return getNutrient(NutrientBase.PROTEINE)* getNutrient(NutrientAnalysis.nonOsProt)/(getNutrient(NutrientAnalysis.nonOsPhos)*getNutrient(NutrientMacro.PHOS));
			default:
				return 0.0F;
		}

		
	}

	public pourcentPart[] getNutrientPart(Nutrient nut){
		float quantite=this.getNutrient(nut);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getNutrient(nut)*alim.getQuantite()/quantite);
		
			index++;
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	public pourcentPart[] getNutrientPart(NutrientLipid en){
		float quantite=this.getNutrient(en);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getQuantite()*alim.getNutrient(en)/quantite);
		
			index++;
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	public pourcentPart[] getNutrientPart(NutrientMacro en){
		float quantite=this.getNutrient(en);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getQuantite()*alim.getNutrient(en)/quantite);
			index++;
			
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	
	public pourcentPart[] getNutrientPartEner(){
		float quantite=this.getEnerT();
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), (float) (alim.getQuantite()*alim.getDE()/quantite));
			index++;
			
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	
	public pourcentPart[] getNutrientPart(NutrientMin en){
		float quantite=this.getNutrient(en);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getQuantite()*alim.getNutrient(en)/quantite);
			index++;
			
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	public pourcentPart[] getNutrientPart(NutrientVitam en){
		float quantite=this.getNutrient(en);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getQuantite()*alim.getNutrient(en)/quantite);
			index++;
			
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	public pourcentPart[] getNutrientPart(NutrientOther en){
		float quantite=this.getNutrient(en);
		pourcentPart[] tab;
		if(alimentList.size()==0){
			tab=new pourcentPart[1];
			tab[0]=new pourcentPart("", 0);
		}else{
		tab= new pourcentPart[alimentList.size()];}
		int index=0;
		for(AlimentRation alim:alimentList){
			
			tab[index]=new pourcentPart(alim.getNom(), alim.getQuantite()*alim.getNutrient(en)/quantite);
		
			index++;
		}
		tab=new pourcentPart().tri(tab);
		return tab;
	}
	
	public float getPoids(){
		float quantite=0;
		for(AlimentRation alim:alimentList){
			quantite+=alim.getQuantite();
		}
		return quantite;
	}
	public float getNutrient(AAEnum en){
		float quantite=0;
		for(AlimentRation alim:alimentList){
			if (alim.getNutrient(NutrientBase.PROTEINE)!=0){
			quantite+=alim.getNutrient(en)*alim.getQuantite()/100;
			}
		}
		
	
		return quantite;
	}

	public boolean isNutrient(Nutrient en){
		boolean pres=true;
		for(AlimentRation alim:alimentList){
			pres=alim.isNutrient(en)& pres;
			
		}
		return pres;
	}
	public AlimentRation getAlimentByUUID(String UUIDalim) {
		
		int a=0;
		boolean touch=false;
	
		for(int i=0;i<alimentList.size();i++){
			if (alimentList.get(i).getUUID().equals(UUIDalim)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 return this.alimentList.get(a);
		}else{
			return null;}
	}
	
	public void reInitialise() {
		for (AlimentRation al:alimentList) {
			if (!al.getTarget().equals(targetAdjust.NO)) {
				al.setQuantite(0);
			}
			}
		}
	public void adjust(float target, targetAdjust tar, float pas) {
		
	
		switch(tar) {
		case CALCIUM:
		case CALCIUMPHOS:
			adjust( target,  tar,  pas, NutrientMacro.CAL);
		
			
			break;
	
		case EPA:
			adjust( target,  tar,  pas, NutrientLipid.EPADHA);
			break;
		case FIBER:
			adjust( target,  tar,  pas, NutrientBase.FIBRETOT);
			break;
		case LIP:
			adjust( target,  tar,  pas, NutrientBase.LIPIDE);
			break;
		case MG:
			adjust( target,  tar,  pas, NutrientMacro.MG);
			break;
		case NA:
			adjust( target,  tar,  pas, NutrientMacro.NA);
			break;
		case NO:
			break;
		case O3:
			adjust( target,  tar,  pas, NutrientLipid.O3);
			break;
		case O6:
			adjust( target,  tar,  pas, NutrientLipid.O6);
			break;
		case PROT:
			adjust( target,  tar,  pas, NutrientBase.PROTEINE);
			break;
		case VITA:
			adjust( target,  tar,  pas, NutrientVitam.VITA);
			break;
		case VITD:
			adjust( target,  tar,  pas, NutrientVitam.VITD);
			break;
		case VITE:
			adjust( target,  tar,  pas, NutrientVitam.VITE);
			break;
		default:
			break;
		
		}
		
		
		
		
	}
	private float getSumWeight(targetAdjust tar ) {
	
	 transCoef=0;
		for (AlimentRation al:alimentList) {
			if (al.getTarget().equals(tar)) {
				if (tar.equals(targetAdjust.CALCIUMPHOS)) {
					if ((al.getNutrient(NutrientMacro.CAL)/al.getNutrient(NutrientMacro.PHOS)>1)){
				transCoef+=al.getWeight();}
			}else {
				transCoef+=al.getWeight();}
				
		
	}
		}
		
	

		return transCoef;}
	
public void setEnerTot(float enerTot) {
	EnerTot = enerTot;
}
public float getEnerTot() {
	return EnerTot;
}

public void updateQuantite(List<AlimentRation>li) {
	for (AlimentRation ai:li) {
		for (AlimentRation a:alimentList) {
			if (a.getUUID().equals(ai.getUUID())) {
				a.setQuantite(ai.getQuantite());
			}
		}
	}
}

private void adjust(float target, targetAdjust tar, float pas, NutrientMacro enu) {
	float initial=0;
	float dif =0;
	float coef=0;
	float quotient =1;
	initial=this.getNutrient(enu);
	dif= target-initial;
	if (dif>0) {
		float r=getSumWeight(tar);

	if (r>0) {

	for(AlimentRation al:alimentList) {
	if (al.getWeight()>0 & al.getNutrient(enu)>0 &al.getTarget().equals(tar)) {
		float q=100*dif/((r/al.getWeight())*al.getNutrient(enu));
		q=(float) (pas*(1+Math.floor(q/pas)));
		al.setQuantite(q);}
	}
	}
	}
}


public void adjustCal(float target, float pas, float objectifPCa) {
	
	float initial=0;
	float initialP=0;
	float dif =0;
	float coef=0;
	float quotient =1;
	initial=this.getNutrient(NutrientMacro.CAL);
	initialP=this.getNutrient(NutrientMacro.PHOS);
	dif= target-initial;
	if (dif>0) {
		float r=getSumWeight(targetAdjust.CALCIUM)+getSumWeight(targetAdjust.CALCIUMPHOS);

	if (r>0) {

	for(AlimentRation al:alimentList) {
	if (al.getWeight()>0 & al.getNutrient(NutrientMacro.CAL)>0 &(al.getTarget().equals(targetAdjust.CALCIUM)|
			al.getTarget().equals(targetAdjust.CALCIUMPHOS)	)) {
		float q=100*dif/((r/al.getWeight())*al.getNutrient(NutrientMacro.CAL));
		q=(float) (pas*(1+Math.floor(q/pas)));
		al.setQuantite(q);}
	}
	initial=this.getNutrient(NutrientMacro.CAL);
	initialP=this.getNutrient(NutrientMacro.PHOS);
	
	 r=getSumWeight(targetAdjust.CALCIUMPHOS);
		if (r>0) {
	int outer=0;
	

	while ((objectifPCa*1.1)>(initial/initialP)& outer<1000) {
		
				for(AlimentRation al:alimentList) {
					if (al.getWeight()>0 & (al.getNutrient(NutrientMacro.CAL)/al.getNutrient(NutrientMacro.PHOS)>1 &
							al.getTarget().equals(targetAdjust.CALCIUMPHOS)	)) {
						float q=100*dif/((r/al.getWeight())*al.getNutrient(NutrientMacro.CAL));
						q=(float) (pas+ al.getQuantite());
						al.setQuantite(q);}
					}
				initial=this.getNutrient(NutrientMacro.CAL);
				initialP=this.getNutrient(NutrientMacro.PHOS);
				outer++;
			}
	}
	
	}
	}
}

	private void adjust(float target, targetAdjust tar, float pas, NutrientBase enu) {
		float initial=0;
		float dif =0;
		float coef=0;
		float quotient =1;
	
		initial=this.getNutrient(enu);
	
		dif= target-initial;
		
		if (dif>0) {
			float r=getSumWeight(tar);

		if (r>0) {

		for(AlimentRation al:alimentList) {
		if (al.getWeight()>0 & al.getNutrient(enu)>0 &al.getTarget().equals(tar)) {
			float q=100*dif/((r/al.getWeight())*al.getNutrient(enu));
			q=(float) (pas*(1+Math.floor(q/pas)));
			al.setQuantite(q)
			;
			}
		}
		}
		}
		
	}
	private void adjust(float target, targetAdjust tar, float pas, NutrientLipid enu) {
		float initial=0;
		float dif =0;
		float coef=0;
		float quotient =1;
		initial=this.getNutrient(enu);
		dif= target-initial;
		if (dif>0) {
			float r=getSumWeight(tar);

		if (r>0) {

		for(AlimentRation al:alimentList) {
		if (al.getWeight()>0 & al.getNutrient(enu)>0 &al.getTarget().equals(tar)) {
			float q=100*dif/((r/al.getWeight())*al.getNutrient(enu));
			q=(float) (pas*(1+Math.floor(q/pas)));
			al.setQuantite(q);}
		}
		}
		}
}
	private void adjust(float target, targetAdjust tar, float pas, NutrientVitam enu) {
		float initial=0;
		float dif =0;
		float coef=0;
		float quotient =1;
		initial=this.getNutrient(enu);
		dif= target-initial;
		if (dif>0) {
			float r=getSumWeight(tar);

		if (r>0) {

		for(AlimentRation al:alimentList) {
		if (al.getWeight()>0 & al.getNutrient(enu)>0 &al.getTarget().equals(tar)) {
			float q=100*dif/((r/al.getWeight())*al.getNutrient(enu));
			q=(float) (pas*(1+Math.floor(q/pas)));
			al.setQuantite(q);}
		}
		}
		}
}
	public void adjustEner(float target, targetAdjust tar, float pas) {
		float initial=0;
		float dif =0;
		float coef=0;
		float quotient =1;
		initial=this.getEnerT();
		dif= target-initial;
		if (dif>0) {
			float r=getSumWeight(tar);

		if (r>0) {

		for(AlimentRation al:alimentList) {
		if (al.getWeight()>0 & al.getDE()>0 &al.getTarget().equals(tar)) {
			float q=100*dif/(float)((r/al.getWeight())*al.getDE());
			q=pas*Math.round(q/pas);
			al.setQuantite(q+al.getQuantite());}
		}
		}
		}
}
	public Ration clone() {
		Ration o = null;
		try {
			// On r??cup??re l'instance ?? renvoyer par l'appel de la 
			// m??thode super.clone()
			o =(Ration) super.clone();
			o.UUID=java.util.UUID.randomUUID().toString();
			o.setAlimentList(new ArrayList<AlimentRation>());
			for (AlimentRation r:alimentList) {
				o.addAliment(r.clone());
			}
		} catch(CloneNotSupportedException cnse) {
			// Ne devrait jamais arriver car nous impl??mentons 
			// l'interface Cloneable
			cnse.printStackTrace(System.err);
		}
		// on renvoie le clone
		return o;
	}
	@Override
	public String toString() {
		return getNom();
	}
}