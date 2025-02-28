package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientVitam implements Nutrient{
	VITA("Vitamine A",0,"UI", UnitEnum.AUui,"VITA", "Vit A"),

	
	VITC("Vitamine C", 1, "mg",UnitEnum.BUmg,"VITC", "Vit C"),
	VITD("Vitamine D",2, "UI",UnitEnum.DUui,"VITD", "Vit D"),
	VITE("Vitamine E", 3,"UI",UnitEnum.EUui,"VITE", "Vit E"),
	VITK("Vitamine K", 4, "mg", UnitEnum.BUmg,"VITK", "Vit K"),
	VITB1("Thiamine (B1)", 5, "mg",UnitEnum.BUmg,"VITB1", "Vit B1"),
	VITB2("Riboflavine (B2)",6,"mg",UnitEnum.BUmg,"VITB2", "Vit B2"),
	VITB3("Nicotinamide, Niacine (B3/PP)",7,"mg",UnitEnum.BUmg, "VITB3", "Vit B3"),
	VITB5("Acide pantoth??nique (B5)",8,"mg",UnitEnum.BUmg,"VITB5", "Vit B5"),
	VITB6("Pyridoxine (B6)",9,"mg", UnitEnum.BUmg,"VITB6", "Vit B6"),
	VITB8("Biotine (B8)",10,"µg",UnitEnum.BUmu, "VITB8", "Vit B8"),
	VITB9("Acide folique (B9)",11,"µg",UnitEnum.BUmu, "VITB9", "Vit B9"),
	VITB12("Cyanocobalamine (B12)",12,"µg",UnitEnum.BUmu, "VITB12", "Vit B12"),
	CHOLINE("Choline", 13,"mg", UnitEnum.BUmg,"CHOLINE", "CHL"),

	RETINOL("Retinol",14,"µg", UnitEnum.BUmu,"RETINOL", "Ret"),
	BETACAR("B??ta-carot??ne",15,"µg", UnitEnum.BUmu,"BETACAR", "Bet")
;


private String name = "";
private int coef = 0;
private String unite="g";
private UnitEnum ue=UnitEnum.BUg;
private String label="VITA";
private String abr="Vit A";

//Constructeur
NutrientVitam(String name, int coef, String unite, UnitEnum ue,String label, String abr){
	this.name = name;
	this.coef=coef;
	this.unite=unite;
	this.ue=ue;
	this.label=label; 
	this.abr=abr;
}
public static boolean isByLabel(String label){
	
	for (NutrientVitam en:NutrientVitam.values()){
		if (en.getLabel().equals(label)){
			return true;
		}
	}
	return false;
}
public static NutrientVitam getByCoef(int i){
	
	return (NutrientVitam) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (NutrientVitam pageType : NutrientVitam.values()) {
        map.put(pageType.coef, pageType);
    }
}
public static NutrientVitam getByLabel(String label){
	NutrientVitam enu=NutrientVitam.VITA;
	for (NutrientVitam en:NutrientVitam.values()){
		if (en.getLabel().equals(label)){
			enu=en;
		}
	}
	return enu;
}
public String getLabel() {
	return label;
}
public String getAbr() {
	return abr;
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
public UnitEnum getUe() {
	return ue;
}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.VITAM;
}
public static int size(){
	return 16;
}}