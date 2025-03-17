package application;

public enum TextConstant {
	VERSION("0.1.30"),
	NOM("VetNutri"),
	STADE("Beta"),
	NBRATION("7")
	;
	


private String name = "";


//Constructeur
TextConstant(String name){
this.name = name;

}



public String nameToString(){
return name;
}

}



