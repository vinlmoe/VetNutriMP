package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum MainNutrientEnum {
	
MIN("Mineraux", 0),
ANA("Analysis", 1),
MACRO("Macro", 2),
VITAM("Vit", 3),
BASE("Base", 4),
LIPID("lipide", 5),
	OTHER("autres", 6),
	ENERGIE("Energy", 7),
	NO("autres", 8), 
	AMA("amino Acides", 9),
	INGREDIENT("Ingredients", 10),
	INDICAT("Indication", 11)
			;
			


		private String name = "";
		private String unite = "";
		private int coef = 0;
		private String Label="";

		//Constructeur
	MainNutrientEnum(String name, int coef ){
		this.name = name;
		this.coef=coef;

		}


	public String getUnite(){
	return unite;
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
		 public Nutrient getNutrient(int i) {
			 switch(this) {
			case AMA:
				return AAEnum.getByCoef(i);
			case ANA:
				return NutrientAnalysis.getByCoef(i);
			case BASE:
				return NutrientBase.getByCoef(i);
			case ENERGIE:
				return NutrientEnergy.getByCoef(i);
			case INDICAT:
			return	null;
			case INGREDIENT:
			 return null;
			case LIPID:
				return NutrientLipid.getByCoef(i);
			case MACRO:
				return NutrientMacro.getByCoef(i);
			case MIN:
				return NutrientMin.getByCoef(i);
			case NO:
				return null;
			case OTHER:
				return NutrientOther.getByCoef(i);
			case VITAM:
				return NutrientVitam.getByCoef(i);
			default:
				return null;
			 
			 }
		 }
		 public static MainNutrientEnum getByCoef(int i){
				
				return (MainNutrientEnum) map.get(i);
			}
			 private static Map map = new HashMap<>();
			static {
			    for (MainNutrientEnum pageType : MainNutrientEnum.values()) {
			        map.put(pageType.coef, pageType);
			    }
			}
		}


