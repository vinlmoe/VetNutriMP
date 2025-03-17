package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientMacro implements Nutrient{
	CAL("Calcium",0,"g",UnitEnum.BUg,"CAL","Ca"),
	PHOS("Phosphore",1,"g",UnitEnum.BUg,"PHOS", "P"),
	MG("Magn??sium",2,"g",UnitEnum.BUg,"MG", "Mg"),
	NA("Sodium",3,"g",UnitEnum.BUg,"NA", "Na"),
	K("Potassium",4,"g",UnitEnum.BUg,"K", "K"),
	
	CHL("Chlore", 5,"g",UnitEnum.BUg, "CHL", "Cl")
					;
					

	private String name = "";
	private int coef = 0;
	private UnitEnum ue=UnitEnum.BUg;
	private String unite="g";
	private String label="CAL";
	private String abr="Ca";

	//Constructeur
NutrientMacro(String name, int coef, String unite, UnitEnum ue, String label, String abr){
	this.name = name;
	this.coef=coef;
	this.unite=unite;
	this.ue=ue;
	this.label=label;
	this.abr=abr;
	}
public static NutrientMacro getByCoef(int i){
	
	return (NutrientMacro) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (NutrientMacro pageType : NutrientMacro.values()) {
        map.put(pageType.coef, pageType);
    }
}


public MainNutrientEnum getMNE() {
	return MainNutrientEnum.MACRO;
}
	public String nameToString(){
	return name;
	}
	public String getUnite(){
	return unite;
	}
	public String getLabel() {
		return label;
	}
	public int getCoef(){
		  return coef;
		}
	   public String getAbr() {
		return abr;
	}
	   public UnitEnum getUe() {
		return ue;
	}
		public static boolean isByLabel(String label){
			
			for (NutrientMacro en:NutrientMacro.values()){
				if (en.getLabel().equals(label)){
					return true;
				}
			}
			return false;
		}
	public static NutrientMacro getByLabel(String label){
		NutrientMacro enu=NutrientMacro.CAL;
		for (NutrientMacro en:NutrientMacro.values()){
			if (en.getLabel().equals(label)){
				enu=en;
			}
		}
		return enu;
	}
				public static int size(){
					  return 6;
					}
				}