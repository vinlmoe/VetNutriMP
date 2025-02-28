package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientEnergy implements Nutrient{
	TOT("tot", 0,"kcal", "TOT",  "5DFFFA"),
DE("iDE", 1,"kcal/100g","DE", "3358FF"),
DEDM("DEDM",2,"Kcal/100g","DEDM", "F8FF5D"),
K("K",4,"","Kener", "F8FF5D"),
PERC("PERC",4,"%","PERC", "F8FF5D"),
BEE("BEE",4,"kcal","BEE", "F8FF5D"),
BE("BE",4,"kcal","BE", "F8FF5D"),
MW("MW",4,"kg","MW", "F8FF5D"),
KPRED("KPRend",4,"","KPRED", "F8FF5D")

		;
		


	private String name = "";
	private int coef = 0;
	private String unite="g";

	private String label="HUM";
	private String colr="#00000";

	//Constructeur
NutrientEnergy(String name, int coef, String unite, String label, String Color){
	this.name = name;
	this.coef=coef;
	this.unite=unite;
	this.label=label;
	this.colr=Color;
	}

public static NutrientEnergy getByCoef(int i){
	
	return (NutrientEnergy) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (NutrientEnergy pageType : NutrientEnergy.values()) {
        map.put(pageType.coef, pageType);
    }
}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.ENERGIE;
}
public static NutrientEnergy getByLabel(String label){
	NutrientEnergy enu=NutrientEnergy.TOT;
	for (NutrientEnergy en:NutrientEnergy.values()){
		if (en.getLabel().equals(label)){
			enu=en;
		}
	}
	return enu;
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
		  return 10;
		}
	public String getColr() {
		return colr;
	}

	@Override
	public UnitEnum getUe() {
		// TODO Auto-generated method stub
			return UnitEnum.NO;
	}
	
	}
