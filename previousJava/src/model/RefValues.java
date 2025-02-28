package model;

import java.awt.Color;
import java.text.DecimalFormat;

import DataStruct.NutrientRef;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.Reflevel;
import Enumerise.ReturnRefTrigger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RefValues {
private	ObservableList<ReturnRefTrigger> olT=FXCollections.observableArrayList();
private	ObservableList<NutrientRef> olR=FXCollections.observableArrayList();
	
	private MainNutrientEnum MNE=MainNutrientEnum.ANA;
	private Nutrient nut=NutrientAnalysis.NaK;
	private float BEE=0;
	private float BW=0;
	private float MW=0;
	private int esp=0;
	
	public RefValues(){
		
	}
	
	
	
	//PM**********************
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	
	
	public String getColor(){


		if ( olT.contains(ReturnRefTrigger.DISDOWN)|olT.contains(ReturnRefTrigger.DISUP)){
			return new String("F333FF");
		}
		else if (  olT.contains(ReturnRefTrigger.REFUP)|olT.contains(ReturnRefTrigger.REFDOWN)){
			return new String("F81818");
		}
		else if ( olT.contains(ReturnRefTrigger.REFOPTIUP)|olT.contains(ReturnRefTrigger.REFOPTIDOWN)){
			return new String("1844F8");
		}
		else {
			return new String("000000");
		}
		}
		
	public String getSign(){
	 if (  olT.contains(ReturnRefTrigger.REFOPTIUP)|olT.contains(ReturnRefTrigger.REFUP)){
			return new String("+");
		}
		else if ( olT.contains(ReturnRefTrigger.REFDOWN)|olT.contains(ReturnRefTrigger.REFOPTIDOWN)){
			return new String("-");
		}
		else {
			return new String("");
		}
		
		}
	
	public String getNorm(KindData kd){
		
		String Rep=new String();
		NutrientRef nr=null; 
		float divider=1;
		if (!MNE.equals(MainNutrientEnum.ANA)|nut.equals(NutrientAnalysis.MethCys)|nut.equals(NutrientAnalysis.PhenTyr)) {
		switch (kd) {
		case BW:
			divider=BW;
			break;
		
		case MW:
			divider=MW; 
			break;
		case NO:
			break;
		case SER:
			divider=BEE/1000;
			break;
		default:
			break;
		
		}}

		Rep+="<html>Minimum et Maximum [";
		nr=NutrientRef.getNutrientRef(olR, Reflevel.MIN);
		if ( nr==null){
			Rep+="|-";
			
		}
		else {
		
			Rep+=""+ twoDForm.format(nr.getTotalQuant()/divider);
			Rep+="<sup>"+ nr.getBiblio()+"</sup> - ";
		}

		nr=NutrientRef.getNutrientRef(olR, Reflevel.MAX);
		if ( nr==null){
			Rep+="|]";
			
		}
		else {
			Rep+=""+ twoDForm.format(nr.getTotalQuant()/divider);
			Rep+="<sup>"+ nr.getBiblio()+"</sup>] ";
		}
		Rep+="<br/>Optimum [";
		nr=NutrientRef.getNutrientRef(olR, Reflevel.OPTIMIN);
		
		if ( nr==null){
			Rep+="|-";
			
		}
		else {
			Rep+=""+ twoDForm.format(nr.getTotalQuant()/divider);
			Rep+="<sup>"+ nr.getBiblio()+"</sup> - ";
		}
		nr=NutrientRef.getNutrientRef(olR, Reflevel.OPTIMAX);
		if ( nr==null){
			Rep+="|]";
			
		}
		else {
			Rep+=""+ twoDForm.format(nr.getTotalQuant()/divider);
			Rep+="<sup>"+ nr.getBiblio()+"</sup> ] ";
		}
		
		ObservableList<NutrientRef>disL=NutrientRef.getNutrientRefDis(olR);
		
		for ( NutrientRef nd:disL) {
			Rep+="<br/>";
			Rep+=nd.getNameRef();
			Rep+=nd.getRelativekind().equals(Reflevel.MIN)?" > ":" < ";
			Rep+=""+ twoDForm.format(nd.getTotalQuant()/divider);
			Rep+="<sup>"+ nd.getBiblio()+"</sup> ";
		}
		Rep+="<p>";
		return Rep;
		}

	
public float getMinQuant() {
	NutrientRef nr=NutrientRef.getNutrientRef(olR, Reflevel.OPTIMIN);	
	if(nr!=null) {
	return	nr.getTotalQuant()
;	}
	nr=NutrientRef.getNutrientRef(olR, Reflevel.MIN);	
	if(nr!=null) {
	return	nr.getTotalQuant()
;	}
	return 0;
}

public void setRef(float values, ObservableList<NutrientRef> ol, float BEE, float BW, float MW, KindData kd, MainNutrientEnum mn, Nutrient nu){
	this.BEE=BEE;
	this.BW=BW;
	this.MW=MW;
	this.nut=nu;
olT.clear();

	for (NutrientRef nut :ol) {
olT.add(nut.Trigger(values));
/*if (mn.equals(MainNutrientEnum.ANA)){
	System.out.println(nut.getName()+" ref "+nut.getTotalQuant() + " value "+values+ " "+nut.getRelativekind().nameToString());
}*/
	}

	olR.clear();
	olR=ol;
	esp=ol.size();
	
	this.MNE=mn;
}
public ObservableList<NutrientRef> getOlR() {
	return olR;
}
public ObservableList<ReturnRefTrigger> getOlT() {
	return olT;
}
}
