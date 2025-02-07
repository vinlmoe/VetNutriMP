package Enumerise;

public enum KindData {

SER("SER", "Mcal"),
DM("DM", "100g DM"),
BW("BW", "kg"),
MW("BW", "kg MW"),
FENER("BW", "Mcal"),
FDESC("BW", "100g"),
NO("no",""), 
AMINO("",""), 
INGRED("",""),
PP("",""),
INDICAT("",""),
LIP("","")


			;
			


		private String name = "";
		private String unite = "";
	

		//Constructeur
	KindData(String name, String unit ){
		this.name = name;
this.unite=unit;
		}




		public String nameToString(){
		return name;
		}
		public String getUnit(){
		return unite;
		}
}
