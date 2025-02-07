package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListReference implements Serializable {
	static private final long serialVersionUID = 1L;
	private List<Reference> ref= new ArrayList<Reference>();
	
	public ListReference() {
		// TODO Auto-generated constructor stub
	}
	public List<Reference> getListReference() {
		return ref;
	}

	
	public Reference getByUUID(String UUID) {
		int a=0;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getUUID().equals(UUID)){
				a=i;
				
			}
		}
		return ref.get(a);
		
			
		
	}
	public Reference getByName(String Name, String esp) {
		int a=0;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getNom().equals(Name) & ref.get(i).getEspece().equals(esp)){
				a=i;
				
			}
		}
		return ref.get(a);
		
			
		
	}
	public Boolean Contain(String Name) {
		Boolean res=false;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getNom().equals(Name)){
				res=true;
				
			}
		}
		return (res);
		
			
		
	}
	public void replaceReference(Reference ani) {
		int a=0;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getUUID().equals(ani.getUUID())){
				a=i;
				
			}
		}
ref.remove(a);
ref.add(ani);
		
			
		
	}
	public ArrayList<Reference> getRef(String esp) {
		ArrayList<Reference> listR= new ArrayList<Reference>();
		for (Reference elem:ref){
			if (elem.getEspece().equals(esp))
				listR.add(elem);
		}
		return listR;
	}

	public ArrayList<String> getRefName(ArrayList<Reference> listR){
		ArrayList<String> listS=new ArrayList<String>();
		for (Reference elem:listR){
			listS.add(elem.getNom());
		}
		return listS;
	}
	public int size(){
		return this.ref.size();
	}
	public void addReference(Reference a){
		this.ref.add(a);
	}
	public void removeReference(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 this.ref.remove(a);
		}
	}
	public Reference getReference(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<ref.size();i++){
			if (ref.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 return this.ref.get(a);
		}else{
			return null;}
	}
	
	
	
}
