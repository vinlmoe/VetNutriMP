package model;

import Enumerise.AAEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;

public class AdressNutrientTable {
	private int position=0;
	private String name=new String("");
	private float coef=1.0F;
	private String type="";
	
	
	public  AdressNutrientTable(int position, String name, float coef){
		
	
	
		this.setType(name);
		
			this.coef=coef;
			this.position=position;
		
		
	}
	public AdressNutrientTable(){
		
	}
	public void setCoef(float coef) {
		this.coef = coef;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public float getCoef() {
		return coef;
	}
	public String getName() {
		return name;
	}
	public int getPosition() {
		return position;
	}
	public String getType() {
		return type;
	}
	
	public String  toType(String name){
		this.setType(name);
		return this.type;
		
		}
		
	
	private boolean  setType(String name){
		
		boolean rep=false;

		if (name.equals("NAME")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		if (name.equals("ID")){
			this.name=name;
			this.type="NONNUTRIENT";
		
			rep=true;
		}
		if (name.equals("ESPECE")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		if (name.equals("TYPEALIM")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("LABEL")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("MARQUE")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("GAMME")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("INDICAT")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("PRES")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("PRESQUANT")){
			this.name=name;
			this.type="NONNUTRIENT";

			rep=true;
		}
		else if  (name.equals("DEPRECATED")){
			this.name=name;
			this.type="NONNUTRIENT";
		
			rep=true;
		}
		else if  (name.equals("DBID")){
			this.name=name;
			this.type="NONNUTRIENT";
		
			rep=true;
		}
		else if  (name.equals("DBNOM")){
			this.name=name;
			this.type="NONNUTRIENT";
		
			rep=true;
		}
		else if  (name.equals("DBDESC")){
			this.name=name;
			this.type="NONNUTRIENT";
		
			rep=true;
		}
		else if  (name.equals("PRESTYPE")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("PRIX")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("CATEGPRIX")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}
		else if  (name.equals("ESPECE")){
			this.name=name;
			this.type="NONNUTRIENT";
			rep=true;
		}else{
			for (NutrientBase en:NutrientBase.values()){
				if (en.getLabel().equals(name)){
					this.name=name;
					this.type="BASE";
					rep=true;
				}
			}

			if(!rep){
				for (NutrientLipid en:NutrientLipid.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="LIPID";
						rep=true;
					}
				}
			} if(!rep){
				for (NutrientOther en:NutrientOther.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="OTHER";
						rep=true;
					}
				}
			} 	if(!rep){
				for (NutrientVitam en:NutrientVitam.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="VIT";
						rep=true;
					}
				}
			} if(!rep){
				for (NutrientMin en:NutrientMin.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="MIN";
						rep=true;
					}
				}
			} if(!rep){
				for (NutrientMacro en:NutrientMacro.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="MACRO";
						rep=true;
					}
				}
			} if(!rep){
				for (AAEnum en:AAEnum.values()){
					if (en.getLabel().equals(name)){
						this.name=name;
						this.type="AA";
						rep=true;
					}
				}
			}
				
		}
		return rep;
		}
		
		
public boolean  isDef(String name){
		boolean rep=false;
		if (name.equals("NAME")){
		
			rep=true;
		}
		if (name.equals("ID")){
			
			rep=true;
		}
		else if  (name.equals("LABEL")){
		
			rep=true;
		}
		else if  (name.equals("MARQUE")){
		
			rep=true;
		}
		else if  (name.equals("TYPEALIM")){
			
			rep=true;
		}
		else if  (name.equals("GAMME")){
		
			rep=true;
		}
		else if  (name.equals("INDICAT")){
			
			rep=true;
		}
		else if  (name.equals("PRIX")){
			
			rep=true;
		}
		else if  (name.equals("CATEGPRIX")){
			
			rep=true;
		}
	else if  (name.equals("PRES")){
			
			rep=true;
		}
	else if  (name.equals("DEPRECATED")){
		
		rep=true;
	}
	else if  (name.equals("PRESQUANT")){
		
		rep=true;
	}
	else if  (name.equals("PRESTYPE")){
		
		rep=true;
	}
	else if  (name.equals("DBID")){
		
		rep=true;
	}
	else if  (name.equals("DBDESC")){
		
		rep=true;
	}
	else if  (name.equals("DBNOM")){
		
		rep=true;
	}
		else if  (name.equals("ESPECE")){
			
			rep=true;
		}else{
			for (NutrientBase en:NutrientBase.values()){
				if (en.getLabel().equals(name)){
				
					rep=true;
				}
			}
			if(!rep){
				for (NutrientLipid en:NutrientLipid.values()){
					if (en.getLabel().equals(name)){
					
						rep=true;
					}
				}
			}
			if(!rep){
				for (NutrientOther en:NutrientOther.values()){
					if (en.getLabel().equals(name)){
					
						rep=true;
					}
				}
			} 	if(!rep){
				for (NutrientVitam en:NutrientVitam.values()){
					if (en.getLabel().equals(name)){
					
						rep=true;
					}
				}
			} if(!rep){
				for (NutrientMin en:NutrientMin.values()){
					if (en.getLabel().equals(name)){
					
						rep=true;
					}
				}
			} if(!rep){
				for (NutrientMacro en:NutrientMacro.values()){
					if (en.getLabel().equals(name)){
						
						rep=true;
					}
				}
			}
			 if(!rep){
				for (AAEnum en:AAEnum.values()){
					if (en.getLabel().equals(name)){
						
						rep=true;
					}
				}
			}
				
		}
		return rep;
		}
		
		
		
		
	}


