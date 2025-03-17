package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum NutrientAnalysis implements Nutrient {
NaK("Rapport K/NA",0,"", "KNA"),
PCa("Rapport phosphocalcique",1,"", "CAP"),
o6o3("Rapport omega 6/omega3",2,"", "O6O3"),

ZnCu("Rapport Zn/Cu",3,"", "ZNCU"),
nonOsPhos("Phosphore non osseux",7,"%", "nonOsPhos"),
nonOsProt("Proteine non osseuse",8,"%", "nonOsProt"),
nonOsPP("Ratio Prot/phos non osseux",9,"", "nonOsPP"),
PhosphProt("Rapport Prot??ines/Phosphore",4,"", "PROTP"),
MethCys("Methionine+cyst??ine",5,"g", "METHCYS"),
PhenTyr("Ph??nylalanine+tyrosine",6,"g", "PHENTYR")
			;
			


		private String name = "";
		private String unite = "";
		private int coef = 0;
		private String Label="";
	

		//Constructeur
	NutrientAnalysis(String name, int coef, String unite, String label){
		this.name = name;
		this.coef=coef;
		this.unite=unite;
		this.Label=label;
		}
	public static NutrientAnalysis getByCoef(int i){
	
		return (NutrientAnalysis) map.get(i);
	}
	 private static Map map = new HashMap<>();
	static {
        for (NutrientAnalysis pageType : NutrientAnalysis.values()) {
            map.put(pageType.coef, pageType);
        }
    }
	
	
	public String getUnite(){
	return unite;
	}
public MainNutrientEnum getMNE() {
	return MainNutrientEnum.ANA;
}
		public String nameToString(){
		return name;
		}
		public int getCoef(){
			  return coef;
			}
		public static int size(){
			  return 7;
			}
		 public String getLabel() {
			return Label;
		}
		@Override
		public UnitEnum getUe() {
			// TODO Auto-generated method stub
			return UnitEnum.NO;
		}
		
		}
