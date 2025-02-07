package model;

import java.io.Serializable;
import java.util.ArrayList;

public class AlimSaver implements Serializable{

	private static final long serialVersionUID = 1L;
private ArrayList<AlimentEv> listAl;
	private AlimDBList db;
	
	 public AlimSaver() {
		// TODO Auto-generated constructor stub
	}
	 
	  public boolean setDb(Vet vet) {
		  if (listAl!=null & vet!=null) {
			  db=new AlimDBList();
			  for(AlimentEv al: listAl) {
				  db.add(vet.getAlDBL().get(al.getDataB()));
			  }
			  
			  return true;
		  }else {
			  return false;
		  }
		
	}
	  public void setListAl(ArrayList<AlimentEv> listAl) {
		this.listAl = listAl;
	}
	  public AlimDBList getDb() {
		return db;
	}
	  public ArrayList<AlimentEv> getListAl() {
		return listAl;
	}
	
}
