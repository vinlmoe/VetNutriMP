package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum AAEnum  implements Nutrient {
ALANINE("Alanine", 0, UnitEnum.BUg,"ALANINE"),
ARGININE("Arginine",1, UnitEnum.BUg,"ARGININE"),
ASPARAGINE("Asparagine", 2,UnitEnum.BUg,"ASPARAGINE"),
ASPARATE("Asparate",3, UnitEnum.BUg,"ASPARATE"),
CYSTEINE("Cyst??ine",4, UnitEnum.BUg,"CYSTEINE"),
GLUTAMATE("Glutamate",5, UnitEnum.BUg,"GLUTAMATE"),
GLUTAMINE("Glutamine", 6,UnitEnum.BUg, "GLUTAMINE"),
GLYCINE("Glycine", 7, UnitEnum.BUg,"GLYCINE"),
HISTIDINE("Histidine", 8, UnitEnum.BUg,"HISTIDINE"),
ISOLEUCINE("Isoleucine", 9, UnitEnum.BUg,"ISOLEUCINE"),
LEUCINE("Leucine", 10, UnitEnum.BUg,"LEUCINE"),
LYSINE("Lysine", 11, UnitEnum.BUg,"LYSINE"),
METHIONINE("Methionine", 12,UnitEnum.BUg, "METHIONINE"),
PHENYLALANINE("Ph??nylalanine", 13,UnitEnum.BUg, "PHENYLALANINE"),
PROLINE("Proline", 14, UnitEnum.BUg,"PROLINE"),
PYRROLYSINE("Pyrrolysine", 15,UnitEnum.BUg,"PYRROLYSINE"),
SELENOCYSTEINE("S??l??nocyst??ine", 16, UnitEnum.BUg,"SELENOCYSTEINE"),
SERINE("S??rine",17,UnitEnum.BUg, "SERINE"),
THREONINE("Thr??onine", 18, UnitEnum.BUg,"THREONINE"),
TRYPTOPHANE("Tryptophane", 19,UnitEnum.BUg, "TRYPTOPHANE"),
TYROSINE("Tyrosine",20,UnitEnum.BUg, "TYROSINE"),
VALINE("Valine", 21,UnitEnum.BUg, "VALINE")

	;
	


private String name = "";
private int coef = 0;
private String label="ASPARGINE";
private UnitEnum ue =UnitEnum.BUg;

//Constructeur
AAEnum(String name, int coef, UnitEnum ue,  String label){
this.name = name;
this.coef=coef;
this.ue=ue;
this.label=label;
}
public static AAEnum getByLabel(String label){
	AAEnum enu=AAEnum.ASPARAGINE;
	for (AAEnum en:AAEnum.values()){
		if (en.getLabel().equals(label)){
			enu=en;
		}
	}
	return enu;
}
public String getLabel() {
	return label;
}
public static AAEnum getByCoef(int i){
	
	return (AAEnum) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (AAEnum pageType : AAEnum.values()) {
        map.put(pageType.coef, pageType);
    }
}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.AMA;
}
public String nameToString(){
return name;
}
public int getCoef(){
	  return coef;
	}
public UnitEnum getUe() {
	return ue;
}
@Override
public String getUnite() {
	// TODO Auto-generated method stub
	return ue.getName();
}
}
