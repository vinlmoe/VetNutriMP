package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientOther implements Nutrient{
	TAURINE("Taurine", 0,"g",UnitEnum.BUg, "TAURINE"),
	CARNITINE("L-Carnitine", 1,"mg",UnitEnum.BUmg,"CARNITINE"),
	FOS("FOS", 2,"g",UnitEnum.BUg,"FOS"),
	MOS("MOS", 3,"g",UnitEnum.BUg,"MOS"),

	SUCR("Saccharose", 5, "g",UnitEnum.BUg,"SACC"),
	FRUCT("Fructose", 6, "g",UnitEnum.BUg,"FRUCT"),
	LACT("Lactose",7,"g",UnitEnum.BUg,"LACTO"),
	MALT("Maltose",8,"g", UnitEnum.BUg,"MALT"),
	AcOx("Acide Oxalique", 9, "mg",UnitEnum.BUmg,"AcOx"),
	GAL("Galactose",10, "g",UnitEnum.BUg, "GAL"),
	GLUCOSE("Glucose", 11, "g",UnitEnum.BUg,"GLUCOSE"),
	DEXTROSE("Dextrose", 12, "g",UnitEnum.BUg,"DEXTROSE")
		;
		


	private String name = "";
	private int coef = 0;
	private UnitEnum ue =UnitEnum.BUmg;
	private String unite="g";
	private String label="TAURINE";

	//Constructeur
NutrientOther(String name, int coef, String unite, UnitEnum ue, String label){
	this.name = name;
	this.coef=coef;
	this.unite=unite;
	this.ue=ue;
	this.label=label;
	}

public static NutrientOther getByCoef(int i){
	
	return (NutrientOther) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (NutrientOther pageType : NutrientOther.values()) {
        map.put(pageType.coef, pageType);
    }
}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.OTHER;
}
public static NutrientOther getByLabel(String label){
	NutrientOther enu=NutrientOther.TAURINE;
	for (NutrientOther en:NutrientOther.values()){
		if (en.getLabel().equals(label)){
			return en;
		}
	}
	return enu;
}
public static boolean isByLabel(String label){
	
	for (NutrientOther en:NutrientOther.values()){
		if (en.getLabel().equals(label)){
			return true;
		}
	}
	return false;
}
public String getLabel() {
	return label;
}
	public String nameToString(){
	return name;
	}
	public String getUnite(){
	return unite;
	}
	public int getCoef(){
		  return coef;
		}
	public static int size(){
		  return 13;
		}
	public UnitEnum getUe() {
		return ue;
	}
	}
