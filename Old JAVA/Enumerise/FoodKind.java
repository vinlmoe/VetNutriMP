package Enumerise;

import java.util.ArrayList;

public enum FoodKind {
	ALL("All", 100),
	
	//Pas de valeur 0, 1/
	
	COMPLET("Complet", 2),
	COMPLEMENTAIRE("Complementary",3),
	MEN("MENAGE",4),
	BARF("BARF",5)
	
	
		;
		


	private String name = "";
	private int coef = 0;

	//Constructeur
	FoodKind(String name, int coef){
	this.name = name;
	this.coef=coef;
	}



	public String nameToString(){
	return name;
	}
	public int getCoef(){
		  return coef;
		}
	
	
public static FoodKind IntToType(int sz){
	FoodKind r=FoodKind.MEN;
	if (sz==0|sz==1) {
		return FoodKind.MEN;
	}
	for (FoodKind ga: FoodKind.values()){
		if (ga.getCoef()==sz){
			return ga;
		}
		
	}
	return r;
}
public static ArrayList<FoodKind> valuesExcept() {
	
	ArrayList<FoodKind>es=new ArrayList<FoodKind>();
	for (FoodKind e:FoodKind.values()) {
		if (e!=FoodKind.ALL) {
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


