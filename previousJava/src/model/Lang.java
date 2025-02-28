package model;

public enum Lang {
	FR("Fran??ais", "French", 1),
	EN("Anglais", "English", 2)

	
						;
						


		private String name = "";
		private String name_en="";
		private int coef = 0;
	

		//Constructeur
	Lang(String name, String name_en, int coef){
		this.name = name;
		this.name_en = name_en;
		this.coef=coef;

		}

	public static Lang getByCoef(int i){
		Lang enu=Lang.FR;
		for (Lang en:Lang.values()){
			if (en.getCoef()==i){
				enu=en;
			}
		}
		return enu;
	}public static Lang getByLabel(String label, Lang lang ){
		
		Lang enu=Lang.FR;
		for (Lang en:Lang.values()){
		
			if (en.nameToString(lang).equals(label)){
				enu=en;
			}
		}
		return enu;
	}
	public String nameToString(Lang lang) {
		if (lang.equals(Lang.FR)) {
			return name;
		}
		else {
			return name_en;
		}
		
	}
	
		public int getCoef(){
			  return coef;
			}
					public static int size(){
						  return 2;
						}
					}
