package model;

public enum StadePhysio {

		
		ADULTE("Adulte", 0),
		CROISSANCE("Croissance", 1),
		LACTATION("Lactation", 2),
		GESTATION("Gestation", 3),
		HOSPIT("Hospit", 4)
		
		
		;


	private String name = "";


	private int categorie=0;

	 
	//Constructeur
	 StadePhysio(String name, int categrorieo){
	  this.name = name;

	  this.categorie=categrorieo;

	}
	 
	 public String nameToString() {
		return name;
	}
	 public int getCategorie() {
		return categorie;
	}
	 public static String getStringFromInt(int id){
		 String str=StadePhysio.ADULTE.nameToString();
		 for (StadePhysio espe:StadePhysio.values()){
			 if (id==espe.getCategorie()){
				 str=espe.nameToString();
			 }
		 }
		 return str;
	 }
	 public static StadePhysio getEnumFromInt(int id){
		 StadePhysio esp=StadePhysio.ADULTE;
		 for (StadePhysio espe:StadePhysio.values()){
			 if (id==espe.getCategorie()){
				 esp=espe;
			 }
		 }
		 return esp;
	 }
	 public static StadePhysio getEnumFromString(String id){
		 StadePhysio esp=StadePhysio.ADULTE;
		 for (StadePhysio espe:StadePhysio.values()){
			 if (id.equals(espe.nameToString())){
				 esp=espe;
			 }
		 }
		 return esp;
	 }

	 




}
