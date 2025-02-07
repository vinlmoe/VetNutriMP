package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientBase implements Nutrient{
	HUMIDITE("Humidite", 0,"g", UnitEnum.BUg,"HUM",  "5DFFFA"),
PROTEINE("Prot??ines", 1,"g",UnitEnum.BUg,"PROT", "3358FF"),
LIPIDE("Lipides",2,"g",UnitEnum.BUg,"LIP", "F8FF5D"),
ENA("ENA", 3,"g",UnitEnum.BUg,"ENA","FF5D5D"),
CELLULOSE("Cellulose brute", 4,"g", UnitEnum.BUg,"CEL", "4BE715"),
CENDRE("Cendres", 5,"g", UnitEnum.BUg,"CEN", "820326"),
SUCRE("Sucres",6,"g",UnitEnum.BUg, "SUC", "C3A900"),
AMIDON("Amidon",7,"g", UnitEnum.BUg,"AMID","820326"),
FIBRESOL("Fibre soluble", 8,"g",UnitEnum.BUg, "FIBRSOL", "20C300"),
FIBRETOT("Fibre totale", 9,"g", UnitEnum.BUg,"FIBRTOT", "1A7D07"),
NDF("Fibre soluble", 10,"g",UnitEnum.BUg, "NDF", "20C300"),
ADF("Fibre totale", 11,"g", UnitEnum.BUg,"ADF", "1A7D07")

		;
		


	private String name = "";
	private int coef = 0;
	private String unite="g";
	private UnitEnum ue= UnitEnum.BUg;
	private String label="HUM";
	private String colr="#00000";

	//Constructeur
NutrientBase(String name, int coef, String unite, UnitEnum ue, String label, String Color){
	this.name = name;
	this.coef=coef;
	this.ue=ue;
	this.unite=unite;
	this.label=label;
	this.colr=Color;
	}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.BASE;
}
public static NutrientBase getByCoef(int i){
	
	return (NutrientBase) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (NutrientBase pageType : NutrientBase.values()) {
        map.put(pageType.coef, pageType);
    }
}
public static NutrientBase getByLabel(String label){
	NutrientBase enu=NutrientBase.PROTEINE;
	for (NutrientBase en:NutrientBase.values()){
		if (en.getLabel().equals(label)){
			enu=en;
		}
	}
	return enu;
}
public static boolean isByLabel(String label){
	
	for (NutrientBase en:NutrientBase.values()){
		if (en.getLabel().equals(label)){
			return true;
		}
	}
	return false;
}
	public String nameToString(){
	return name;
	}
	public String getUnite(){
	return unite;
	}
	public String getLabel(){
	return label;
	}
	public int getCoef(){
		  return coef;
		}
	public static int size(){
		  return 12;
		}
	public String getColr() {
		return colr;
	}
	public UnitEnum getUe() {
		return ue;
	} 
	
	}
