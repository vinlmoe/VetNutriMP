package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlimDBList implements Serializable {
	 private static final long serialVersionUID = 1L;
	private Map<String, alimDB> dbList = new HashMap<String, alimDB>();
	
	  public AlimDBList() {
		// TODO Auto-generated constructor stub
	}
	  public void add(alimDB db){
		  dbList.put(db.getUUID(), db);
	  }
	  
	  public void addAll(AlimDBList dba){
		for (alimDB db:dba.dbList.values()){
		  dbList.put(db.getUUID(), db);}
	  }

	  public void remove(alimDB db){
		  dbList.remove(db.getUUID());
	  }
	  public void replace (alimDB db) {
		  dbList.replace(db.getUUID(), db);
	  }
public String getNom(String uuid) {
	if(dbList.containsKey(uuid)) {
	return dbList.get(uuid).getsNom();}
	else {
		return "Generic";
	}
}
public void setNumber (String uuid, int n) {
	dbList.get(uuid).setNumber(n);
}
public String getNomComp(String uuid) {
	return dbList.get(uuid).getCompNom();
}
public ArrayList<alimDB> values(){
	return  new ArrayList<alimDB>(dbList.values());
}
public alimDB get(String uuid) {
	if (dbList.containsKey(uuid)) {
	return dbList.get(uuid);}
	else {
		if(dbList.containsKey("4")) {
			return dbList.get("4");
		}else {
			dbList.put("4", new alimDB("4","Generic", "Generic"));
			return dbList.get("4");
		}
	}
}

public String condition() {
	String r="";
			for (alimDB al:dbList.values()) {
				if (r.isBlank()) {
					r=" WHERE DataB = \""+al.getUUID()+"\"";
				}else {
					r+=" OR DataB = \""+al.getUUID()+"\"";
				}
			}
			return r;
}
public boolean contains (String uuid) {
	return this.dbList.containsKey(uuid);
}
}
