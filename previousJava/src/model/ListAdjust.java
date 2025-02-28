package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListAdjust  implements Serializable {
	static private final long serialVersionUID = 1L;
	private List<AdjustSave> Adjust= new ArrayList<AdjustSave>();
	
	public ListAdjust() {
		
		// TODO Auto-generated constructor stub
	}
	public List<AdjustSave> getList() {
		return Adjust;
	}

	
	public AdjustSave getAdjustByUUID(String UUID) {
		int a=0;
		for(int i=0;i<Adjust.size();i++){
			if (Adjust.get(i).getUUID().equals(UUID)){
				a=i;
				
			}
		}
		return Adjust.get(a);
		
			
		
	}
	public boolean getAdjustByUUIDexist(String UUID) {
		boolean touch=false;
		for(int i=0;i<Adjust.size();i++){
			if (Adjust.get(i).getUUID().equals(UUID)){
				touch=true;
				
			}
		}
		return touch;
		
			
		
	}
	public AdjustSave getAdjustByID(int i) {
		if (Adjust.size()!=0){
		return Adjust.get(i);}
		else { return new AdjustSave();}
		
			
		
	}
public void removeByID(int i) {
		
		Adjust.remove(i);
		
			
		
	}
	public void replaceAdjust(AdjustSave ani) {
		int a=0;
	
		for(int i=0;i<Adjust.size();i++){
			if (Adjust.get(i).getUUID().equals(ani.getUUID())){
				a=i;
				
			}
		}
Adjust.remove(a);
Adjust.add(a, ani);
		
			
		
	}
	
	public int size(){
		return this.Adjust.size();
	}
	public void addAdjust(AdjustSave a){
		if (getAdjustByUUIDexist(a.getUUID())){
			this.replaceAdjust(a);
		}else{
			this.Adjust.add(a);}
	}
	public void removeAdjust(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<Adjust.size();i++){
			if (Adjust.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 this.Adjust.remove(a);
		}
	}
	public AdjustSave getAdjust(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<Adjust.size();i++){
			if (Adjust.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 return this.Adjust.get(a);
		}else{
			return null;}
	}
	
	
}

