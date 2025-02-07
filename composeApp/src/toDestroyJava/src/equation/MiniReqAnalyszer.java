package equation;

import java.util.HashMap;
import java.util.Map;

import DataStruct.NutrientRef;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MiniReqAnalyszer {
	
	
	Map<String,listNutrientRef> mapRef=new HashMap<String,listNutrientRef>();
	float BEE=0;
	float BW=0;
	float MW=0;
	
	public MiniReqAnalyszer(RequirementAnalyzer ra) {
		
		for  (String s:ra.getMapRef().keySet()) {
			mapRef.put(s, new listNutrientRef(ra.getMapRef().get(s).get()));
		}
		this.BEE=ra.getBEE();
		this.BW= ra.getBW();
				this.MW=ra.getMW();
	}
	public ObservableList<NutrientRef> getReferences(int mne, int kind){
	Nutrient n=MainNutrientEnum.getByCoef(mne).getNutrient(kind);
	if (n!=null) {
		if (mapRef.containsKey(n.toString())) {
		
				return mapRef.get(n.toString()).get();
				
		}}
			ObservableList<NutrientRef>oo=FXCollections.observableArrayList();
			return oo;
		}
			
	public Map<String, listNutrientRef> getMapRef() {
		return mapRef;
	}
}
