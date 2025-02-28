package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListRecette  implements Serializable {
	static private final long serialVersionUID = 1L;
	private List<Recette> recette= new ArrayList<Recette>();
	
	public ListRecette() {
		
		// TODO Auto-generated constructor stub
	}
	public List<Recette> getList() {
		return recette;
	}

	
	public Recette getRecetteByUUID(String UUID) {
		int a=0;
		for(int i=0;i<recette.size();i++){
			if (recette.get(i).getUUID().equals(UUID)){
				a=i;
				
			}
		}
		return recette.get(a);
		
			
		
	}
	public Recette getRecetteByID(int i) {
		if (recette.size()!=0){
		return recette.get(i);}
		else { return new Recette("Nouvelle recette");}
		
			
		
	}
public void removeByID(int i) {
		
		recette.remove(i);
		
			
		
	}
	public void replacerecette(Recette ani) {
		int a=0;
	
		for(int i=0;i<recette.size();i++){
			if (recette.get(i).getUUID().equals(ani.getUUID())){
				a=i;
				
			}
		}
recette.remove(a);
recette.add(a, ani);
		
			
		
	}
	
	public int size(){
		return this.recette.size();
	}
	public void addrecette(Recette a){
		this.recette.add(a);
	}
	public void removeRecette(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<recette.size();i++){
			if (recette.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 this.recette.remove(a);
		}
	}
	public Recette getRecette(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<recette.size();i++){
			if (recette.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 return this.recette.get(a);
		}else{
			return null;}
	}
	
	
}


