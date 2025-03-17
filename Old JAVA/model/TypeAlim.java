package model;

import java.util.ArrayList;

public enum TypeAlim {
	ALL("All", 100),
	CIQUAL("CALNUT", 0),
	USDA("FCEN",1),
	COMPLET("Complet", 2),
	COMPLEMENTAIRE("Complementary",3),
	FUSION("MENAGE",4),
	BARF("BARF",5)
	
	
		;
		


	private String name = "";
	private int coef = 0;

	//Constructeur
	TypeAlim(String name, int coef){
	this.name = name;
	this.coef=coef;
	}



	public String nameToString(){
	return name;
	}
	public int getCoef(){
		  return coef;
		}
	public static TypeAlim StringToType(String sz){
		TypeAlim r=TypeAlim.CIQUAL;
		for (TypeAlim ga: TypeAlim.values()){
			if (ga.nameToString().toLowerCase().equals(sz.toLowerCase())){
				r=ga;
			}
			
		}
		return r;
	}
	
public static TypeAlim IntToType(int sz){
	TypeAlim r=TypeAlim.CIQUAL;
	for (TypeAlim ga: TypeAlim.values()){
		if (ga.getCoef()==sz){
			return ga;
		}
		
	}
	return r;
}
public static ArrayList<TypeAlim> valuesExcept() {
	
	ArrayList<TypeAlim>es=new ArrayList<TypeAlim>();
	for (TypeAlim e:TypeAlim.values()) {
		if (e!=TypeAlim.ALL) {
			es.add(e);
		}
			
	}
	return es;
}

@Override
public String toString() {
	return name;
}
	
}


