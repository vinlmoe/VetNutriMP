package model;

import java.util.HashMap;
import java.util.Map;

import Enumerise.MainNutrientEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientVitam;
import Enumerise.UnitReqEnum;

public enum targetAdjust {
	PROT("Prot??ines", "Proteins", 0, MainNutrientEnum.BASE.getCoef(),NutrientBase.PROTEINE.getCoef() ),
	O3("Om??ga 3", "Omega 3", 1,MainNutrientEnum.LIPID.getCoef(),NutrientLipid.O3.getCoef()),
	O6("Om??ga 6", "Omega 6", 2,MainNutrientEnum.LIPID.getCoef(),NutrientLipid.O6.getCoef()),
	CALCIUM("Calcium", "Calcium", 3,MainNutrientEnum.MACRO.getCoef(),NutrientMacro.CAL.getCoef()),
	CALCIUMPHOS("Calcium et Phosphore", "Calcium and Phosphorus", 4,MainNutrientEnum.ANA.getCoef(),NutrientAnalysis.PCa.getCoef()),
	FIBER("Fibres", "Fiber", 5,MainNutrientEnum.BASE.getCoef(),NutrientBase.FIBRETOT.getCoef()),
	
VITA("Vitamine A", "Vitamin A", 6,MainNutrientEnum.VITAM.getCoef(), NutrientVitam.VITA.getCoef()),
	VITD("Vitamine D", "Vitamin D", 7, MainNutrientEnum.VITAM.getCoef(), NutrientVitam.VITD.getCoef()),
	VITE("Vitamine E", "Vitamin E", 8,MainNutrientEnum.VITAM.getCoef(),NutrientVitam.VITE.getCoef()),
	NA("Sodium", "Sodium", 9, MainNutrientEnum.MACRO.getCoef(), NutrientMacro.NA.getCoef()),
	MG("Magn??sium", "Magnesium", 10, MainNutrientEnum.MACRO.getCoef(), NutrientMacro.MG.getCoef()),
	
	EPA("EPA + DHA", "EPA + DHA", 11, MainNutrientEnum.LIPID.getCoef(), NutrientLipid.EPADHA.getCoef()),
	
	ENERGIE("Energie 2", "Energy 2", 12, MainNutrientEnum.ENERGIE.getCoef(), 0),
	COMP("Energie 1", "Energie 1", 13, MainNutrientEnum.ENERGIE.getCoef(), 0),
	LIP("Mati??res grasses", "Crude fat", 14, MainNutrientEnum.BASE.getCoef(),NutrientBase.LIPIDE.getCoef()),
	NO("Non", "No", 15, MainNutrientEnum.NO.getCoef(),0)

	
						;
						


		private String name = "";
		private String name_en="";
		private int coef = 0;
		private int mne=0;
private int kind=0;	

		//Constructeur
	targetAdjust(String name, String name_en, int coef, int mne, int kind){
		this.name = name;
		this.name_en = name_en;
		this.coef=coef;
this.mne=mne;
this.kind=kind;
		}

public static targetAdjust getByName(String label, Lang lang){
		targetAdjust enu=targetAdjust.PROT;
		for (targetAdjust en:targetAdjust.values()){
			if (en.nameToString(lang).equals(label)){
				enu=en;
			}
		}
		return enu;
	}
	public static targetAdjust getByCoef(int i){
		
		return (targetAdjust) map.get(i);
	}
	 private static Map map = new HashMap<>();
	static {
	    for (targetAdjust pageType : targetAdjust.values()) {
	        map.put(pageType.coef, pageType);
	    }
	} 
		public String nameToString(Lang lang){
			if (lang.equals(Lang.FR)) {
				return name;
			}else {
				return name_en;
			}
		
		}
	public int getKind() {
		return kind;
	}
	public int getMne() {
		return mne;
	}
		
		public int getCoef(){
			  return coef;
			}
					public static int size(){
						  return 16;
						}
					}