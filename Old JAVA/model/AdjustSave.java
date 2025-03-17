package model;

import java.awt.List;
import java.io.Serializable;
import java.util.ArrayList;

public class AdjustSave implements Serializable{
	static private final long serialVersionUID = 101L;
	private String UUID;
	private String Name=""; 

private ArrayList<TargetDefinition> list=new ArrayList<TargetDefinition>();
	
	public AdjustSave (){
		UUID=java.util.UUID.randomUUID().toString();
		list.add(new TargetDefinition( targetAdjust.PROT, 70F, 90F,25F));
		list.add(new TargetDefinition( targetAdjust.LIP, 22F, 100F,4F));
		list.add(new TargetDefinition( targetAdjust.FIBER, 3F, 90F,25F));
		list.add(new TargetDefinition( targetAdjust.CALCIUM, 1.5F, 100, 1F));
		list.add(new TargetDefinition( targetAdjust.O6, 2.6F, 100F, 4F));
		list.add(new TargetDefinition( targetAdjust.O3, 0.5F, 100F,4F));
		list.add(new TargetDefinition( targetAdjust.EPA, 0.3F, 100, 1F));
		list.add(new TargetDefinition( targetAdjust.NA, 0.5F, 100, 1F));
		list.add(new TargetDefinition( targetAdjust.MG, 1F, 100, 1F));

		
	}
	public AdjustSave (AdjustSave AS){
		UUID=java.util.UUID.randomUUID().toString();
		for (TargetDefinition TD:AS.getAll()) {
						list.add((TargetDefinition)TD.clone());
		}
	

		
	}
	public String getName() {
		return Name;
	}public String getUUID() {
		return UUID;
	}public void setName(String name) {
		Name = name;
	}
	public int Up(int i) {
		TargetDefinition trans;
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
		TargetDefinition trans;
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
	public TargetDefinition get(int i) {
		return list.get(i);
	}
	public ArrayList<TargetDefinition> getAll() {
		return list;
	}
}
