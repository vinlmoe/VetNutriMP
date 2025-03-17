package Enumerise;

public enum ConditionEnum {

MORE(">",""),
LESS("<",""),
INCLUDE("INCLUDE",""),
EXCLIDE("EXCLUDE","")

			;
			


		private String name = "";
		private String unite = "";
		private int coef = 0;
		private String Label="";

		//Constructeur
	ConditionEnum(String name, String unit ){
		this.name = name;
this.unite=unit;
		}




		public String nameToString(){
		return name;
		}
		public String getName(){
		return name;
		}
}
