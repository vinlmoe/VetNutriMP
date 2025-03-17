package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class listConsult implements Serializable {
	static private final long serialVersionUID = 1L;
	private List<Consultation> consult= new ArrayList<Consultation>();
	
	public listConsult() {
		// TODO Auto-generated constructor stub
	}
	public List<Consultation> getListConsult() {
		return consult;
	}

	
	public Consultation getConsultByUUID(String UUID) {
		int a=0;
		for(int i=0;i<consult.size();i++){
			if (consult.get(i).getUUID().equals(UUID)){
				a=i;
				
			}
		}
		return consult.get(a);
		
			
		
	}
	public void replaceConsult(Consultation ani) {
		int a=0;
		for(int i=0;i<consult.size();i++){
			if (consult.get(i).getUUID().equals(ani.getUUID())){
				a=i;
				
			}
		}
consult.remove(a);
consult.add(ani);
		
			
		
	}
	
	public int size(){
		return this.consult.size();
	}
	public void addConsult(Consultation a){
		this.consult.add(a);
	}
	public void removeConsult(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<consult.size();i++){
			if (consult.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 this.consult.remove(a);
		}
	}
	public Ration getRation(String UUIDcons, String UUIDrat) {
		return this.getConsult(UUIDcons).getRationByUUID(UUIDrat);
				
	}
	public AlimentRation getAliment (String UUIDcons, String UUIDrat, String UUIDalim) {
		return this.getConsult(UUIDcons).getRationByUUID(UUIDrat).getAlimentByUUID(UUIDalim);
	}
	public Consultation getConsult(String UUID){
		int a=0;
		boolean touch=false;
		for(int i=0;i<consult.size();i++){
			if (consult.get(i).getUUID().equals(UUID)){
				a=i;
				touch=true;
			}
		}
		if (touch){
	 return this.consult.get(a);
		}else{
			return null;}
	}
	public Consultation getLastConsult(){
		if (yetConsult()){
			int a=0;
			Consultation cons=consult.get(a);
			for (int i=1; i<consult.size(); i++){
				if (cons.getDate().before(consult.get(i).getDate())){
					a=i;
					cons=consult.get(a);
				}
			}
			return cons;
		}
		else{
			return null;
		}
	}
	public Consultation getPrevConsult(Date d){
		if (yetConsult()){
			int a=0;
			Consultation cons=consult.get(a);
			for (int i=1; i<consult.size(); i++){
				if (consult.get(i).getDate().after(cons.getDate())&& consult.get(i).getDate().before(d)){
					a=i;
					cons=consult.get(a);
				}
			}
			return cons;
		}
		else{
			return null;
		}}
		public Consultation getPostConsult(Date d){
			if (yetConsult()){
				int a=0;
				Consultation cons=consult.get(a);
				for (int i=1; i<consult.size(); i++){
					if (consult.get(i).getDate().before(cons.getDate())&& consult.get(i).getDate().after(d)){
						a=i;
						cons=consult.get(a);
					}
				}
				return cons;
			}
			else{
				return null;
			}
	}
	public boolean yetConsult(){
	if (consult.size()==0){
			return false;
		}
		else{
			return true;
		}
	}

}
