package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientMin implements Nutrient{
FE("Fer",0,"mg",UnitEnum.BUmg,"FE", "Fe"),
CU("Cuivre",1, "mg",UnitEnum.BUmg,"CU", "Cu"),
ZN("Zinc ", 2,"mg",UnitEnum.BUmg,"ZN", "Zn"),
MN("Mangan??se",3, "mg",UnitEnum.BUmg,"MN", "Mn"),
I("Iode ",4,"µg",UnitEnum.BUmu,"I", "I"),
SE("Sélénium ",5,"µg",UnitEnum.BUmu,"SE", "Se")
					;
					


	private String name = "";
	private int coef = 0;
	private UnitEnum ue=UnitEnum.BUg;
	private String unite="g";
	private String label="FE";
	private String abr="Fe";

	//Constructeur
NutrientMin(String name, int coef, String unite,UnitEnum ue, String label, String abr){
	this.name = name;
	this.coef=coef;
	this.unite=unite;
	this.ue=ue;
	this.label=label;
	this.abr=abr;
	}

  public String getAbr() {
	return abr;
}

	public static boolean isByLabel(String label){
		
		for (NutrientMacro en:NutrientMacro.values()){
			if (en.getLabel().equals(label)){
				return true;
			}
		}
		return false;
	}
	public static NutrientMin getByCoef(int i){
		
		return (NutrientMin) map.get(i);
	}
	public MainNutrientEnum getMNE() {
		return MainNutrientEnum.MIN;
	}
	 private static Map map = new HashMap<>();
	static {
        for (NutrientMin pageType : NutrientMin.values()) {
            map.put(pageType.coef, pageType);
        }
    }
	public static NutrientMin getByLabel(String label){
	NutrientMin enu=NutrientMin.FE;
	for (NutrientMin en:NutrientMin.values()){
		if (en.getLabel().equals(label)){
			enu=en;
		}
	}
	return enu;
}
public String getLabel() {
	return label;
}
	public String nameToString(){
	return name;
	}
	public UnitEnum getUe() {
		return ue;
	}
	public String getUnite(){
	return unite;
	}
	public int getCoef(){
		  return coef;
		}
				public static int size(){
					  return 6;
					}
				}