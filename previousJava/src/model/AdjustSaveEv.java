package model;

import java.awt.List;
import java.io.Serializable;
import java.util.ArrayList;

import Enumerise.UnitReqEnum;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AdjustSaveEv implements Serializable{
	static private final long serialVersionUID = 101L;
	private String UUID;
	private String Name=""; 
private String description="";
private Espece esp=Espece.CH;
private ArrayList<TargetDefinitionEv> list=new ArrayList<TargetDefinitionEv>();
	
	public AdjustSaveEv (){
		UUID=java.util.UUID.randomUUID().toString();
		list.add(new TargetDefinitionEv( targetAdjust.PROT, 100, UnitReqEnum.PERC, 90F,25F));
		list.add(new TargetDefinitionEv( targetAdjust.LIP, 100,  UnitReqEnum.PERC,100F,4F));
		list.add(new TargetDefinitionEv( targetAdjust.FIBER, 2.5F, UnitReqEnum.MCAL, 90F,25F));
		list.add(new TargetDefinitionEv( targetAdjust.CALCIUM, 150,UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.O6, 100, UnitReqEnum.PERC, 100F, 4F));
		list.add(new TargetDefinitionEv( targetAdjust.O3, 100, UnitReqEnum.PERC, 100F,4F));
		list.add(new TargetDefinitionEv( targetAdjust.EPA, 100, UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.NA, 100, UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.MG, 100, UnitReqEnum.PERC, 100, 1F));

		
	}
	public AdjustSaveEv (ObservableList<TargetDefinitionEv> list){
		UUID=java.util.UUID.randomUUID().toString();
		this.list.clear();
	this.list.addAll(list);
		
	}
	public AdjustSaveEv (String UUID){
		this.UUID=UUID;
		list.add(new TargetDefinitionEv( targetAdjust.PROT, 100, UnitReqEnum.PERC, 90F,25F));
		list.add(new TargetDefinitionEv( targetAdjust.LIP, 100,  UnitReqEnum.PERC,100F,4F));
		list.add(new TargetDefinitionEv( targetAdjust.FIBER, 2.5F, UnitReqEnum.MCAL, 90F,25F));
		list.add(new TargetDefinitionEv( targetAdjust.CALCIUM, 150,UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.O6, 100, UnitReqEnum.PERC, 100F, 4F));
		list.add(new TargetDefinitionEv( targetAdjust.O3, 100, UnitReqEnum.PERC, 100F,4F));
		list.add(new TargetDefinitionEv( targetAdjust.EPA, 100, UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.NA, 100, UnitReqEnum.PERC, 100, 1F));
		list.add(new TargetDefinitionEv( targetAdjust.MG, 100, UnitReqEnum.PERC, 100, 1F));

		
	}
	public AdjustSaveEv (AdjustSaveEv AS){
		UUID=java.util.UUID.randomUUID().toString();
		Name="Dup "+AS.Name ;
		description=AS.description+"";
		for (TargetDefinitionEv TD:AS.getAll()) {
						list.add((TargetDefinitionEv)TD.clone());
		}
	

		
	}
	public AdjustSaveEv (AdjustSave AS){
		UUID=AS.getUUID();
		Name=AS.getName() ;
		description="";
		for (TargetDefinition TD:AS.getAll()) {
						list.add(new TargetDefinitionEv(TD.getTarg(), TD.getValue(), UnitReqEnum.MCAL, TD.getPercentCompletion(), TD.getPas()));
		}
	

		
	}
	public String getEspece() {
		return esp.getName();
	}
	public String getName() {
		return Name;
	}public String getUUID() {
		return UUID;
	}public void setName(String name) {
		Name = name;
	}
	public int Up(int i) {
		TargetDefinitionEv trans;
		if (i!=0) {
			trans=list.get(i);
			list.set(i, list.get(i-1));
			list.set(i-1, trans);
		return i-1;
		}else {
			return 0;
		}
		
	}
	public int Down(int i) {
		TargetDefinitionEv trans;
		if (i!=list.size()-1) {
			trans=list.get(i);
			list.set(i, list.get(i+1));
			list.set(i+1, trans);
			return i+1;
		}else {
			return i;
		}
		
	}
	public int size() {
		return list.size(); 
	}
	public TargetDefinitionEv get(int i) {
		return list.get(i);
	}
	public ObservableList<TargetDefinitionEv> getAll() {
		 ObservableList<TargetDefinitionEv> l=FXCollections.observableArrayList();
		 l.addAll(list);
		return l;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setEsp(Espece esp) {
		this.esp = esp;
	}
	public String getDescription() {
		return description;
	} 
	public void setList(ObservableList<TargetDefinitionEv> list) {
		this.list.clear();
		this.list.addAll(list);
	}
	public Espece getEsp() {
		return esp;
	}
	public ObservableList<TargetDefinitionEv> getList() {
		 ObservableList<TargetDefinitionEv> l=FXCollections.observableArrayList();
		 l.addAll(list);
		return l;
	}
	
	@Override
	public String toString() {
		return Name;
	}
}
