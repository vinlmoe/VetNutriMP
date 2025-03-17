package equation;

import java.util.HashMap;
import java.util.Map;

import DataStruct.NutrientRef;
import DataStruct.SupplementalvariableP;
import Enumerise.AAEnum;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Ration;
import model.RationCalculator;
import model.ReferenceEv;

public class RequirementAnalyzer implements Cloneable {
	
Map<String,listNutrientRef> mapRef=new HashMap<String,listNutrientRef>();
	float BEE=0;
	float BW=0;
	float MW=0;
	
	
	public RequirementAnalyzer() {
	
		// TODO Auto-generated constructor stub
	}
	
	public void addReference(ReferenceEv ref,  RationCalculator calc, ObservableList<SupplementalvariableP>svp, Ration rat) {
	
	for (MainNutrientEnum en: MainNutrientEnum.values()) {

		switch(en) {
		case AMA:
			for(AAEnum n:AAEnum.values()) {
			
				refSet( n, ref,calc,svp,rat);
			}
			break;
		case ANA:
			for(NutrientAnalysis n:NutrientAnalysis.values()) {
				refSet( n, ref,calc,svp,rat);
				
			}
			break;
		case BASE:
			for(NutrientBase n:NutrientBase.values()) {
				refSet(n, ref,calc,svp,rat);
			}
			break;
	

		case LIPID:
			for(NutrientLipid n:NutrientLipid.values()) {
				refSet(n, ref,calc,svp,rat);
				
			}
			break;
		case MACRO:
			for(NutrientMacro n:NutrientMacro.values()) {
				refSet( n, ref,calc,svp,rat);
				
			}
			break;
		case MIN:
			for(NutrientMin n:NutrientMin.values()) {
				refSet( n, ref,calc,svp,rat);
				
			}
			break;
		
		case OTHER:
			for(NutrientOther n:NutrientOther.values()) {
				refSet(n, ref,calc,svp,rat);
				
			}
			break;
		case VITAM:
			for(NutrientVitam n:NutrientVitam.values()) {
				refSet( n, ref,calc,svp,rat);
				
			}
			break;
		default:
			break;
		
		}
	}
	}
	
	public ObservableList<NutrientRef> getReferences(int mne, int kind){
		
		if (mapRef.containsKey(MainNutrientEnum.getByCoef(mne).getNutrient(kind).toString())) {
				return mapRef.get(MainNutrientEnum.getByCoef(mne).getNutrient(kind).toString()).get();
				
		}else {
			ObservableList<NutrientRef>oo=FXCollections.observableArrayList();
			return oo;
		}
			}
	public void setBEE(float bEE) {
		BEE = bEE;
	}
	public void setBW(float bW) {
		BW = bW;
	}
	public void setMW(float mW) {
		MW = mW;
	}
	public void refSet( Nutrient n, ReferenceEv ref, RationCalculator calc, ObservableList<SupplementalvariableP>svp, Ration rat) {
	if(ref!=null) {
		if (ref.getListNutrient( n, BEE, BW, MW,calc,svp,rat).size()>0) {
			if (ref.isDisease()&mapRef.containsKey(n.toString())) {
				mapRef.get(n.toString()).add(ref.getListNutrient(n, BEE, BW, MW,calc,svp,rat));
			}else {
			
			mapRef.put(n.toString(), new listNutrientRef(ref.getListNutrient(n, BEE, BW, MW,calc,svp,rat)));
			}
	}
	}}

public Map<String, listNutrientRef> getMapRef() {
	return mapRef;
}
public float getBEE() {
	return BEE;
}
public float getBW() {
	return BW;
}
public float getMW() {
	return MW;
}

@Override
public RequirementAnalyzer clone() {
	try {
		return (RequirementAnalyzer) super.clone();
	} catch (CloneNotSupportedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
}
}
	

	
	


