package model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

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

public class AlimentRation implements Serializable, Cloneable {
	static private final long serialVersionUID = 1001L;
	private float quantite;
	private float prop;
	private AlimentUnif alim;
	private AlimentEv alime;
	private String UUID;
	private String UUIDunif;
	private targetAdjust targ;
private float weight=1;
	private int categ =0;
	private double density=0;
	DecimalFormat twoDForm = new DecimalFormat("#.#"); 
public AlimentRation(AlimentEv al){
	this.alime=al;
	UUID=java.util.UUID.randomUUID().toString();
	UUIDunif= al.getUUID();
	categ=1;
}

public AlimentRation(AlimentEv al, float Quantite, String UUID){
	this.alime=al;
	quantite =Quantite;
	this.UUID=UUID;
	UUIDunif= al.getUUID();
	categ=1;
}
public void update() {
	if (alim!=null) {
		alime=new AlimentEv(alim);
		alim=null;
	}
}
public String getPresentation () {
	return alime.getPresentation();
}
public String getUUIDunif() {
	if(UUIDunif!=null) {
	return UUIDunif;}
	else {
		return "nonINDEX";
	}
}
public String getUUID() {
	return UUID;
}
public void UpUUID(String ratUUID) {
	UUID=this.UUID+ratUUID;
}
public String getNom(){
	String nom= new String();

		nom=alime.getNom();
	
	return nom;
}
public AlimentEv getAlim() {
	if (alime==null) {
		alime=new AlimentEv(alim);
		return alime;
	}
	return alime;
}
public String getMarque(){
	String nom= new String();
	if (alime.getTypeAliment().equals(FoodKind.COMPLET)||alime.getTypeAliment().equals(FoodKind.COMPLEMENTAIRE)){
		
		nom=alime.getMarque();}
		else{
		nom="";
		}
	
	return nom;
}

public String getGamme(){
	String nom= new String();
	if (alime.getTypeAliment().equals(FoodKind.COMPLET)||alime.getTypeAliment().equals(FoodKind.COMPLEMENTAIRE)){
		
		nom=alime.getGamme();}
		else{
		nom="";
		}
	
	return nom;
	
}

public float getQuantite() {
	return quantite;
}

public float getProp() {
	return prop;
}public void setProp(float prop) {
	this.prop = prop;
}
public void setQuantite(float quantite) {
	this.quantite = quantite;
}


public FoodKind getType(){

	return alime.getTypeAliment();

		
}
public String getGroup(){
String fl=new String("");
	

	fl=alime.getGroup().nameToString();
	return fl;
		
}

public String getDecription(){
String fl="";
	
		fl=alime.getIngredients();
	
	
	return fl;
}
public AlimentRation clone() {
	AlimentRation o = null;
	try {
		// On r??cup??re l'instance ?? renvoyer par l'appel de la 
		// m??thode super.clone()
		o =(AlimentRation) super.clone();
		o.renewUUID();
	} catch(CloneNotSupportedException cnse) {
		// Ne devrait jamais arriver car nous impl??mentons 
		// l'interface Cloneable
		cnse.printStackTrace(System.err);
	}
	// on renvoie le clone
	return o;
}

public float getNutrient (Nutrient enu){
	
	if (enu==NutrientBase.FIBRETOT & !alime.isNutrient(enu) &alime.isNutrient(NutrientBase.CELLULOSE)){
	
		return(alime.getNutrient(NutrientBase.CELLULOSE) );
	}
	else if(enu==NutrientBase.ENA & !alime.isNutrient(enu) & 
			alime.isNutrient(NutrientBase.LIPIDE)&
alime.isNutrient(NutrientBase.PROTEINE)&
alime.isNutrient(NutrientBase.HUMIDITE)&
alime.isNutrient(NutrientBase.CELLULOSE)&
alime.isNutrient(NutrientBase.CENDRE)){
		return (100-alime.getNutrient(NutrientBase.LIPIDE)
				-alime.getNutrient(NutrientBase.PROTEINE)
				-alime.getNutrient(NutrientBase.HUMIDITE)
				-alime.getNutrient(NutrientBase.CELLULOSE)
				-alime.getNutrient(NutrientBase.CENDRE)>0?
						100-alime.getNutrient(NutrientBase.LIPIDE)
						-alime.getNutrient(NutrientBase.PROTEINE)
						-alime.getNutrient(NutrientBase.HUMIDITE)
						-alime.getNutrient(NutrientBase.CELLULOSE)
						-alime.getNutrient(NutrientBase.CENDRE):0
				);
				
				
	}
	else if(enu==NutrientBase.ENA & !alime.isNutrient(enu) & 
			alime.isNutrient(NutrientBase.LIPIDE)&
alime.isNutrient(NutrientBase.PROTEINE)&
alime.isNutrient(NutrientBase.HUMIDITE)&
alime.isNutrient(NutrientBase.FIBRETOT)&
alime.isNutrient(NutrientBase.CENDRE)){
		return (100-alime.getNutrient(NutrientBase.LIPIDE)
				-alime.getNutrient(NutrientBase.PROTEINE)
				-alime.getNutrient(NutrientBase.HUMIDITE)
				-alime.getNutrient(NutrientBase.FIBRETOT)
				-alime.getNutrient(NutrientBase.CENDRE)>0?
						100-alime.getNutrient(NutrientBase.LIPIDE)
						-alime.getNutrient(NutrientBase.PROTEINE)
						-alime.getNutrient(NutrientBase.HUMIDITE)
						-alime.getNutrient(NutrientBase.FIBRETOT)
						-alime.getNutrient(NutrientBase.CENDRE):0
				);
				
				
	}	else if(enu==NutrientBase.ENA & !alime.isNutrient(enu) & (
			alime.isNutrient(NutrientBase.AMIDON)|
			alime.isNutrient(NutrientBase.SUCRE))
			){
				return alime.getNutrient(NutrientBase.AMIDON)+alime.getNutrient(NutrientBase.SUCRE);
			}
	else if(enu==NutrientLipid.EPADHA & !alime.isNutrient(enu) & (
			alime.isNutrient(NutrientLipid.AG205)|
			alime.isNutrient(NutrientLipid.AG226))
			) {
		return alime.getNutrient(NutrientLipid.AG205)+alime.getNutrient(NutrientLipid.AG226);
	}else if (enu.getMNE().equals(MainNutrientEnum.AMA)) {
	
		return alime.getNutrient(enu)*alime.getNutrient(NutrientBase.PROTEINE)/100  ;
	}
	else if (enu==NutrientAnalysis.MethCys) {
		return (getNutrient(AAEnum.METHIONINE)+getNutrient(AAEnum.CYSTEINE));
	}
	else if (enu==NutrientAnalysis.PhenTyr) {
		return (getNutrient(AAEnum.PHENYLALANINE)+getNutrient(AAEnum.TYROSINE));
	}
	else if (enu==NutrientAnalysis.nonOsPhos) {
		return (100* ( alime.getNutrient(NutrientMacro.PHOS)-  alime.getNutrient(NutrientMacro.CAL)/2)/  alime.getNutrient(NutrientMacro.PHOS));
	}
	else if (enu==NutrientAnalysis.nonOsPP) {
		return (((alime.getNutrient(NutrientBase.PROTEINE)-  3*alime.getNutrient(NutrientMacro.CAL))/ alime.getNutrient(NutrientMacro.PHOS)-  alime.getNutrient(NutrientMacro.CAL)/2));
	}
	else if (enu==NutrientAnalysis.nonOsProt) {
		return 100*(alime.getNutrient(NutrientBase.PROTEINE)-  3*alime.getNutrient(NutrientMacro.CAL))/  alime.getNutrient(NutrientBase.PROTEINE);
	}
	else	{
	return alime.getNutrient(enu);
	}
	
	
}

public targetAdjust getTarget() {
	if(targ==null) {
		switch (alime.getTypeAliment()){


			case COMPLET:
				targ=targetAdjust.COMP; 
				break;
			default:
				if ((100F*this.getNutrient(NutrientBase.CENDRE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>10) & this.isNutrient(NutrientMacro.CAL)) {
					targ=targetAdjust.CALCIUMPHOS; 
				}else if((100F*this.getNutrient(NutrientBase.PROTEINE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>30)) {
					targ=targetAdjust.PROT; 
				}	else if((100F*this.getNutrient(NutrientBase.LIPIDE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>30)& (100F*this.getNutrient(NutrientLipid.O6)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>15)) {
					targ=targetAdjust.O6; 
				}else if((100F*this.getNutrient(NutrientBase.LIPIDE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>30)& this.getNutrient(NutrientLipid.EPADHA)>0) {
					targ=targetAdjust.EPA; 
				}
				else if((100F*this.getNutrient(NutrientBase.LIPIDE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>40)) {
					targ=targetAdjust.LIP; 
				}	else if((100F*this.getNutrient(NutrientBase.CELLULOSE)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>20)) {
					targ=targetAdjust.FIBER; 
				}else if((100F*this.getNutrient(NutrientBase.ENA)/(100F-this.getNutrient(NutrientBase.HUMIDITE))>50)) {
					targ=targetAdjust.ENERGIE; 
				}
				
				else {
					targ=targetAdjust.NO; 
				}
				break;
	
		}
	}
	return targ;
}
public String getFamillyBrand() {
String s= new String();
System.out.println(alime.getTypeAliment());
		switch (alime.getTypeAliment()){
		case MEN:
			case BARF:
				s=this.getGroup();
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

public void setTarget(targetAdjust tag) {
	this.targ=tag; 
}

public boolean isNutrient (Nutrient enu){
	return alime.isNutrient(enu);
}

private String presence(boolean is){
	String texte="";
	if (!is){
		texte=" /!\\" ;
	}
	return texte;
}
public float getWeight() {
	return weight;
}
public void setWeight(float w) {
	this.weight = w;
}
public String Resume(ResourceBundle bun){
	String texte= new String();
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	
	texte+=("<html>" +this.getNom());
	texte+="<br>"+(this.getAlim().getCont().equals(ContEnum.NO)?"":(this.getAlim().getQuantInt()+" g/"+bun.getString(this.getAlim().getCont().getName())));
	texte+=("<br>Pourcentage d'humidité : " +twoDForm.format(this.getNutrient(NutrientBase.HUMIDITE))+" %"+ presence(this.isNutrient(NutrientBase.HUMIDITE)));

	texte+="<br>"+("Valeur pour 100g de Matière sèche : ");
	for (int i=1;  i < NutrientBase.size(); i++){
		texte+="<br>"+(bun.getString(NutrientBase.getByCoef(i).getLabel())+" : "+twoDForm.format(100F*this.getNutrient(NutrientBase.getByCoef(i))/(100F-this.getNutrient(NutrientBase.HUMIDITE)))+" "+NutrientBase.getByCoef(i).getUnite()+ presence(this.isNutrient(NutrientBase.getByCoef(i))));
		
	}

		texte+="<br>"+(bun.getString(NutrientMacro.CAL.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientMacro.CAL)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientMacro.CAL.getUnite()+ presence(this.isNutrient(NutrientMacro.CAL)));
		texte+="<br>"+(bun.getString(NutrientMacro.PHOS.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientMacro.PHOS)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+' '+NutrientMacro.PHOS.getUnite()+ presence(this.isNutrient(NutrientMacro.PHOS)));
		texte+="<br>"+(bun.getString(NutrientLipid.O3.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientLipid.O3)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientLipid.O3.getUnite()+ presence(this.isNutrient(NutrientLipid.O3)));
		texte+="<br>"+(bun.getString(NutrientLipid.O6.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientLipid.O6)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientLipid.O6.getUnite()+ presence(this.isNutrient(NutrientLipid.O6)));
	
	
	
	return texte;
}
public String Resume(ResourceBundle bun, float quant){
	String texte= new String();
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	
	texte+=("<html>" +this.getNom());
	texte+="<br>"+(this.getAlim().getCont().equals(ContEnum.NO)?"":(this.getAlim().getQuantInt()+" g/"+bun.getString(this.getAlim().getCont().getName())+ " ("+twoDForm.format( quant/this.getAlim().getQuantInt()) +")"));
	texte+=("<br>Pourcentage d'humidité : " +twoDForm.format(this.getNutrient(NutrientBase.HUMIDITE))+" %"+ presence(this.isNutrient(NutrientBase.HUMIDITE)));

	texte+="<br>"+("Valeur pour 100g de Matière sèche : ");
	for (int i=1;  i < NutrientBase.size(); i++){
		texte+="<br>"+(bun.getString(NutrientBase.getByCoef(i).getLabel())+" : "+twoDForm.format(100F*this.getNutrient(NutrientBase.getByCoef(i))/(100F-this.getNutrient(NutrientBase.HUMIDITE)))+" "+NutrientBase.getByCoef(i).getUnite()+ presence(this.isNutrient(NutrientBase.getByCoef(i))));
		
	}

		texte+="<br>"+(bun.getString(NutrientMacro.CAL.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientMacro.CAL)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientMacro.CAL.getUnite()+ presence(this.isNutrient(NutrientMacro.CAL)));
		texte+="<br>"+(bun.getString(NutrientMacro.PHOS.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientMacro.PHOS)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+' '+NutrientMacro.PHOS.getUnite()+ presence(this.isNutrient(NutrientMacro.PHOS)));
		texte+="<br>"+(bun.getString(NutrientLipid.O3.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientLipid.O3)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientLipid.O3.getUnite()+ presence(this.isNutrient(NutrientLipid.O3)));
		texte+="<br>"+(bun.getString(NutrientLipid.O6.getLabel())+" : "+
				twoDForm.format(100F*this.getNutrient(NutrientLipid.O6)/(100F-this.getNutrient(NutrientBase.HUMIDITE)))
		+" "+NutrientLipid.O6.getUnite()+ presence(this.isNutrient(NutrientLipid.O6)));
	
	
	
	return texte;
}
public void setDE(double density) {
	this.density = density;
}
public double getDE() {
	return density;
}
private void renewUUID() {
	UUID=java.util.UUID.randomUUID().toString();
}
}
