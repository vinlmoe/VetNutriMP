package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class listConsultEv implements Serializable {
	static private final long serialVersionUID = 1L;
	 private Map<String, ConsultationEv> consultList = new HashMap<String, ConsultationEv>();
	private List<ConsultationEv> consult= new ArrayList<ConsultationEv>();
	
	public listConsultEv() {
		// TODO Auto-generated constructor stub
	}
	public void convert() {
		if (consult.size()>0) {
			for(ConsultationEv c:consult) {
				consultList.put(c.getUUID(), c);
			}
		}
		consult.clear();
	}
	public List<ConsultationEv> getListConsult() {
		return new ArrayList<ConsultationEv>(consultList.values());
	}

	
	public ConsultationEv getConsultByUUID(String UUID) {
		
		return consultList.get(UUID);
	}
	public void replaceConsult(ConsultationEv ani) {
		consultList.replace(ani.getUUID(), ani);
	}
	
	public int size(){
		return this.consultList.size();
	}
	public void addConsult(ConsultationEv a){
		this.consultList.put(a.getUUID(), a);
	}
	public void removeConsult(String UUID){
	this.consultList.remove(UUID);
	}
	public Ration getRation(String UUIDcons, String UUIDrat) {
		return this.getConsult(UUIDcons).getRationByUUID(UUIDrat);
				
	}
	public AlimentRation getAliment (String UUIDcons, String UUIDrat, String UUIDalim) {
		return this.getConsult(UUIDcons).getRationByUUID(UUIDrat).getAlimentByUUID(UUIDalim);
	}
	public ConsultationEv getConsult(String UUID){
	return this.consultList.get(UUID);
	}
	public ConsultationEv getLastConsult(){
		
		if (yetConsult()){
			consult=getListConsult();
			int a=0;
			ConsultationEv cons=consult.get(a);
			for (int i=1; i<consult.size(); i++){
				if (cons.getDate().isBefore(consult.get(i).getDate())){
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
	public ConsultationEv getPrevConsult(LocalDate d){
		if (yetConsult()){
			consult=getListConsult();
			int a=0;
			ConsultationEv cons=consult.get(a);
			for (int i=1; i<consult.size(); i++){
				if (consult.get(i).getDate().isAfter(cons.getDate())&& consult.get(i).getDate().isBefore(d)){
					a=i;
					cons=consult.get(a);
				}
			}
			return cons;
		}
		else{
			return null;
		}}
		public ConsultationEv getPostConsult(LocalDate d){
			if (yetConsult()){
				consult=getListConsult();
				int a=0;
				ConsultationEv cons=consult.get(a);
				for (int i=1; i<consult.size(); i++){
					if (consult.get(i).getDate().isBefore(cons.getDate())&& consult.get(i).getDate().isAfter(d)){
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
	if (consultList.size()==0){
			return false;
		}
		else{
			return true;
		}
	}

}
