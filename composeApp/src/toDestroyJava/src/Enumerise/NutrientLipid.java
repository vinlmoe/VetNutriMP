package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientLipid implements Nutrient{
AGSATURE("Acides gras satur??s",0,UnitEnum.BUg,"g", "AGSATURE"),
AGMONO("Acides gras mono-insatur??s",1,UnitEnum.BUg,"g", "AGMONO"),
AGPOLY("Acides gras poly-insatur??s",2,UnitEnum.BUg,"g", "AGPOLY"),
AG40("C4:0", 3,UnitEnum.BUg,"g", "AG40"),
AG60("C6:0", 4,UnitEnum.BUg,"g", "AG60"),
AG80("C8:0",5,UnitEnum.BUg,"g", "AG80"),
AG100("C10:0",6,UnitEnum.BUg,"g", "AG100"),
AG120("C12:0", 7,UnitEnum.BUg,"g","AG120"),
AG140("C14:0",8,UnitEnum.BUg,"g", "AG140"),
AG160("C16:0", 9,UnitEnum.BUg,"g", "AG160"),
AG180("C18:0", 10,UnitEnum.BUg,"g" , "AG180"),
AG181("C18:1-n9", 11,UnitEnum.BUg,"g", "AG181"),
AG182("C18:2-n6", 12,UnitEnum.BUg,"g", "AG182"),
AG183("C18:3-n3", 13,UnitEnum.BUg,"g", "AG183"),
AG204("C20:4-n6",14,UnitEnum.BUg,"g", "AG204"),
AG205("EPA", 15,UnitEnum.BUg,"g", "AG205"),
AG226("DHA",16,UnitEnum.BUg,"g", "AG226"),
CHOL("Cholesterol",17,UnitEnum.BUg,"g", "CHOLES"),
O3("Omega 3", 18,UnitEnum.BUg,"g", "O3"),
O6("Omega 6",19,UnitEnum.BUg,"g","O6"),
EPADHA("EPA et DHA", 20,UnitEnum.BUg,"g", "EPADHA")

			;
			


	private String name = "";
	private int coef = 0;
	private String unite="g";
	private UnitEnum ue=UnitEnum.BUg;
private String label="AGSATURE";
	//Constructeur
NutrientLipid(String name, int coef, UnitEnum ue, String unite, String label){
	this.name = name;
	this.coef=coef;
	this.ue=ue;
	this.unite=unite;
	this.label=label;
	}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.LIPID;
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
	public UnitEnum getUe() {
		return ue;
	}
	public static NutrientLipid getByLabel(String label){
		NutrientLipid enu=NutrientLipid.AG100;
		for (NutrientLipid en:NutrientLipid.values()){
			if (en.getLabel().equals(label)){
				return en;
			}
		}
		return enu;
	}
	
	public static boolean isByLabel(String label){
	
		for (NutrientLipid en:NutrientLipid.values()){
			if (en.getLabel().equals(label)){
				return true;
			}
		}
		return false;
	}
	public static NutrientLipid getByCoef(int i){
		return (NutrientLipid) map.get(i);
	}
	 private static Map map = new HashMap<>();
	static {
        for (NutrientLipid pageType : NutrientLipid.values()) {
            map.put(pageType.coef, pageType);
        }
    }
	public int getCoef(){
		  return coef;
		}
		public static int size(){
			  return 21;
			}
		}