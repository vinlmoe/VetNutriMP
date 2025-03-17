
package Enumerise;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AllNutrient implements Serializable{
	private static final long serialVersionUID = 1L;
	private String Label="";
	private String unit="";
	private int mne=0;
	private int kindnut=0;
	
	
	private AllNutrient (String lab,int mne, int kind) {
		this.Label=lab;
		this.mne=mne;
		this.kindnut=kind;
	}
	public AllNutrient(Nutrient n) {
		this.Label=n.getLabel();
		this.unit=n.getUnite();
		this.mne=n.getMNE().getCoef();
		this.kindnut=n.getCoef();
	}

	
	public static  Map values() {
	Map v=new HashMap<>();
		
		for (NutrientBase n:NutrientBase.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}
		for (NutrientMacro n:NutrientMacro.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}
		for (NutrientMin n:NutrientMin.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}
		for (NutrientAnalysis n:NutrientAnalysis.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}
		for (NutrientLipid n:NutrientLipid.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}
		for (NutrientVitam n:NutrientVitam.values()) {
			v.put(n.getMNE().getCoef()*1000+n.getCoef(),new AllNutrient(n));
		}		
		return v;
		
	}
	
	public int getKindnut() {
		return kindnut;
	}
	public String getLabel() {
		return Label;
	}
	public int getMne() {
		return mne;
	}
	public String getUnit() {
		return unit;
	}
	public int getID() {
		System.out.println("ALLnut ID "+ Label+" "+(mne*1000+kindnut));
		return (mne*1000+kindnut);
		
	}
}