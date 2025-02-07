package model;

import java.util.ArrayList;
import javax.swing.DefaultListModel;


public enum AlimIndic{
	ALL("All", 999),
	PED("Pédiatrique",0),
	NEUT("Stérilisé",1),
	PHYS("Physiologique",2),
	SEN("Sénior",3),
	CALM("Stress félin",4),
	OBES("Obésité",5),
	GESTATION("Gestation",6),
	SONDE("Sonde",7),
	LACT("Lactation",8),
	CROISSANCE("Croissance",9),
	
	DENT("Hygiéne Buccodentaire",11),
	DIAB("Diabète",12),
	
	INSHEP("Insuffisance Hépatique",25),
	HYPO("Hypoallergénique",26),
	ART("Soutien Articulaire",27),
	MRC("Soutien de la fonction rénale",28),

	CONV("Convalescence",30),

	MBAUF("MBAUF",32),
	URO("Urolithiase",33),
	DERM("Affections cutanées",34),
	GI("Affections gastro-intestinales",35),
	CAR("Affections cardiaques",36),
	END("Affections endocriniennes",37),
	IPE("Insufisance pancréatique",38),
	DISTRU("Dissolution struvites",39),
	REDSTRU("Réduction struvites",40),
	REDURA("Réduction urates",41),
	REDOXA("Réduction oxalates",42),
	REDCYST("Réduction cystines",43),
	ACT("Sport",45),
	AUTRE("", 44)
	//ANOR("Anorexie",10),
	//CONS("Constipation",13),
		//MEG("Mégacolon",14),
		//COL("Colite",15),
		//DIAR("Diarrhée",16),
		//MICI("MICI",17),
		//MALD("Malabsorption, Maldigestion",18),
		
		//GAST("Gastrite",19),
		//PANC("Pancreatite",20),
		//ENTEX("Enteropathie exudative",21),
		//CHOLANG("Cholangite, Cholestase",22),
		//SHUNT("Shunt Porto-Systémique",23),
		//ENCEPHEP("Encéphalose Hépatique",24),
	//LIP("Lipidose hépatique",31),
	//CANC("Cancer",29),
		;
		


	private String name = "";
	private int coef = 1;

	//Constructeur
	AlimIndic(String name, int coef){
	this.name = name;
	this.coef=coef;
	}
	public static AlimIndic StringToGroup(String sz){
		AlimIndic r=AlimIndic.AUTRE;
		for (AlimIndic ga: AlimIndic.values()){
			if (ga.nameToString().equals(sz)){
				r=ga;
			}
			
		}
		return r;
	}
	public static AlimIndic IntToGroup(int sz){
		AlimIndic r=AlimIndic.PHYS;
		for (AlimIndic ga: AlimIndic.values()){
			if (ga.getCoef()==sz){
				r=ga;
			}
			
		}
		return r;
	}
	
	public static DefaultListModel<String>  getAllIndic(){
		  DefaultListModel<String> list = new DefaultListModel<>();
		for (AlimIndic elem:AlimIndic.values()){
			list.addElement(elem.nameToString());
		}
		return list;
	}
	public static ArrayList<AlimIndic> valuesExcept() {
		
		ArrayList<AlimIndic>es=new ArrayList<AlimIndic>();
		for (AlimIndic e:AlimIndic.values()) {
			if (e!=AlimIndic.ALL) {
				es.add(e);
			}
				
		}
		return es;
	}
	
	
	public String nameToString(){
	return name;
	}

	public int getCoef(){
		  return coef;
		}
	public String getCoefStr(){
		if (coef<10){
			  return "0"+coef;
		}else{
		  return ""+coef;}
		}
	}



