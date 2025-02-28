package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class listAnim implements Serializable{
	static private final long serialVersionUID = 1L;
	private List<Animal> anim= new ArrayList<Animal>();
	
	public listAnim() {
		// TODO Auto-generated constructor stub
	}
	public List<Animal> getListAnim() {
		return anim;
	}

	
	public Animal getAnimByUUID(String UUID) {
		int a=0;
		for(int i=0;i<anim.size();i++){
			if (anim.get(i).getUUID().equals(UUID)){
				a=i;
				
			}
		}
		return anim.get(a);
		
			
		
	}
	public Animal getAnim(int a) {
		
	
		return anim.get(a);
		
			
		
	}
	public void replaceAlim(Animal ani) {
		int a=0;
		for(int i=0;i<anim.size();i++){
			if (anim.get(i).getUUID().equals(ani.getUUID())){
				a=i;
				
			}
		}
anim.remove(a);
anim.add(ani);
		
			
		
	}
	
	public int size(){
		return this.anim.size();
	}
	public void addAnim(Animal a){
		this.anim.add(a);
	}
	
	public void removeAnim(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<anim.size();i++){
			if (anim.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 this.anim.remove(a);
		}
	}

}
