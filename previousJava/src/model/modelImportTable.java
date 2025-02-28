package model;

import java.awt.List;
import java.util.ArrayList;

import Enumerise.AAEnum;
import Enumerise.FoodKind;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;

public class modelImportTable {
	private ArrayList<AdressNutrientTable> listNutrient=new ArrayList <AdressNutrientTable> ();
	
	
	public modelImportTable(){
		
	}
	
	public boolean importAdress(String [] names, String[] coefs){
		int lengthn=names.length;
		int lengthc=coefs.length;

		String name=new String("");
		float coef=1.0F;
		if (lengthn!=lengthc){
			return false;
		}
		else {
			for (int i=0; i<lengthn; i++){
				
				if ((!names[i].isEmpty()) & (!coefs[i].isEmpty())){
					
					name=names[i];

		
			
					if(new AdressNutrientTable().isDef(name)){
					coef=Float.parseFloat(coefs[i]);
					this.listNutrient.add(new AdressNutrientTable(i, name, coef));}
						
				}
			
			}
			if (!listNutrient.isEmpty()){
								return true;
			}else{
				return false;
			}
		}
			
	}
public boolean isPresent(String name){
	boolean rep=false;
	for (int i=0; i<listNutrient.size(); i++){
		if (listNutrient.get(i).getName().equals(name)){
			rep=true;
		}
	}
return rep;
}
public int getPosition(String name){
	int rep=0;
	for (int i=0; i<listNutrient.size(); i++){
		if (listNutrient.get(i).getName().equals(name)){
			rep=listNutrient.get(i).getPosition();
		}
	}
return rep;
}
public String getData(String name, String[]alimd){
	String rep="";
	for (int i=0; i<listNutrient.size(); i++){
		if (listNutrient.get(i).getName().equals(name)){
	
			rep=alimd[i];
		}
	}
return rep;
}
public String getData(String name, AlimentEv alim, Vet vet){
	String rep="";
	AdressNutrientTable tab=new AdressNutrientTable();
	if (name.equals("ID")){
		return alim.getUUID();
	}else if (name.equals("NAME")){
		return alim.getNom();
	}else if (name.equals("LABEL")){
		return alim.getIngredients();
	}else if (name.equals("MARQUE")){
		return alim.getMarque();
	}else if (name.equals("GAMME")){
		return alim.getGamme();
	}	else if (name.equals("PRIX")){
		return ""+alim.getPrix();
		}else if (name.equals("CATEGPRIX")){
		return ""+alim.getCategoriePrix();
	}else if (name.equals("PRES")){
		return ""+alim.getPresentation();
	}
	else if (name.equals("PRESQUANT")){
		return ""+alim.getQuantInt();
	}
	else if (name.equals("PRESTYPE")){
		return ""+alim.getCont().getID();
	}
	else if (name.equals("DBNOM")){
		return ""+vet.getAlDBL().getNom(alim.getDataB());
	}
	else if (name.equals("DBDESC")){
		return ""+vet.getAlDBL().getNomComp(alim.getDataB());
	}
	else if (name.equals("DBID")){
		return ""+alim.getDataB();
	}
	else if (name.equals("DEPRECATED")){
		return ""+(alim.isDeprecated()?1:0);
	}
	else if (name.equals("INDICAT")){
	ArrayList<String>ind=	alim.getIndicat();
	String sortie="";	
	for(int i=0; i<ind.size(); i++){
		if (i==0){
			sortie=""+AlimIndic.StringToGroup(ind.get(i)).getCoefStr();
		}else{
			sortie=sortie+"_"+AlimIndic.StringToGroup(ind.get(i)).getCoefStr();
		}
		}
		return sortie;
	}
	else if (name.equals("ESPECE")){
		ArrayList<String>ind=	alim.getEspeces();
		String sortie="";	
		for(int i=0; i<ind.size(); i++){
			if (i==0){
				sortie=""+ind.get(i);
			}else{
				sortie=sortie+"_"+ind.get(i);
			}
			}
			return sortie;
		
		
	}	else if (name.equals("TYPEALIM")){
		if (alim.getTypeAliment().equals(FoodKind.COMPLET)){
			return "COMPLET";
		}else if (alim.getTypeAliment().equals(FoodKind.MEN)){
			return "MEN";} else if (alim.getTypeAliment().equals(FoodKind.BARF)){
				return "BARF";}else if (alim.getTypeAliment().equals(FoodKind.COMPLEMENTAIRE)){
					return "COMPLEMENTAIRE";} else {return "MEN";}
	
		
	}
	else if (tab.toType(name).equals("BASE")){
		if (alim.isNutrient(NutrientBase.getByLabel(name))){
		return ""+alim.getNutrient(NutrientBase.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("OTHER")){
		if (alim.isNutrient(NutrientOther.getByLabel(name))){
		return ""+alim.getNutrient(NutrientOther.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("MACRO")){
		if (alim.isNutrient(NutrientMacro.getByLabel(name))){
		return ""+alim.getNutrient(NutrientMacro.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("MIN")){
		if (alim.isNutrient(NutrientMin.getByLabel(name))){
			return ""+alim.getNutrient(NutrientMin.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("VIT")){
		if (alim.isNutrient(NutrientVitam.getByLabel(name))){
			return ""+alim.getNutrient(NutrientVitam.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("LIPID")){
		if (alim.isNutrient(NutrientLipid.getByLabel(name))){
			return ""+alim.getNutrient(NutrientLipid.getByLabel(name));
		}else {return "";}
	}else if (tab.toType(name).equals("AA")){
		if (alim.isNutrient(AAEnum.getByLabel(name))){
			return ""+alim.getNutrient(AAEnum.getByLabel(name));
		}else {return "";}
	}else {return "";}




}
public AlimentEv createAlim(String [] alimd, Vet vet){
	AlimentEv al;
	if(this.isPresent("ID")){
	al=new AlimentEv(this.getData("ID", alimd));
	}else{
		 al=new AlimentEv();
	}
	
	for (int i=1; i<listNutrient.size(); i++){
		System.out.println(listNutrient.get(i).getName());
		if (listNutrient.get(i).getType().equals("NONNUTRIENT")){
			if (listNutrient.get(i).getName().equals("ID")){
				
			}else if (listNutrient.get(i).getName().equals("NAME")){
				al.setNom(alimd[listNutrient.get(i).getPosition()]);
			}
			else if (listNutrient.get(i).getName().equals("LABEL")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setIngredients(alimd[listNutrient.get(i).getPosition()]);}
			}else if (listNutrient.get(i).getName().equals("MARQUE")){
			
				
				al.setMarque(alimd[listNutrient.get(i).getPosition()]);
				al.setGroup(GroupAlim.StringToGroup(alimd[listNutrient.get(i).getPosition()]));
				
				
				
			}else if (listNutrient.get(i).getName().equals("GAMME")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setGamme(alimd[listNutrient.get(i).getPosition()]);}
				
			}else if (listNutrient.get(i).getName().equals("PRIX")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setPrix(Float.parseFloat(alimd[listNutrient.get(i).getPosition()]));}
				
			}else if (listNutrient.get(i).getName().equals("CATEGPRIX")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setCategoriePrix(alimd[listNutrient.get(i).getPosition()]);}
				
			}else if (listNutrient.get(i).getName().equals("PRES")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				
				al.setPresentation(alimd[listNutrient.get(i).getPosition()]);}
				
			}
		else if (listNutrient.get(i).getName().equals("DBID")){
			if (alimd[listNutrient.get(i).getPosition()].isBlank() |
					getFromName(listNutrient, "DBNOM")==null| 
					getFromName(listNutrient, "DBDESC")==null	){
			al.setDataB("4");
			}else if (alimd[getFromName(listNutrient, "DBNOM").getPosition()].isBlank()|
					alimd[getFromName(listNutrient, "DBDESC").getPosition()].isBlank()	){
				al.setDataB("4");
			}
			else{
				if (vet.getAlDBL().contains(alimd[getFromName(listNutrient, "DBID").getPosition()])){
					al.setDataB(alimd[getFromName(listNutrient, "DBID").getPosition()]);
				}else {
					vet.getAlDBL().add(new alimDB(alimd[getFromName(listNutrient, "DBID").getPosition()],alimd[getFromName(listNutrient, "DBNOM").getPosition()],alimd[getFromName(listNutrient, "DBDESC").getPosition()]));
					al.setDataB(alimd[getFromName(listNutrient, "DBID").getPosition()]);
				}
			}
			
		}
			else if (listNutrient.get(i).getName().equals("DEPRECATED")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				
				al.setDeprecated(Integer.parseInt(   alimd[listNutrient.get(i).getPosition()]));}
				
			}
			else if (listNutrient.get(i).getName().equals("PRESQUANT")){
				if (!alimd[listNutrient.get(i).getPosition()].isBlank()){			
				al.setQuantInt (Float.parseFloat(alimd[listNutrient.get(i).getPosition()]));
			System.out.println("ValQUant "+ (Float.parseFloat(alimd[listNutrient.get(i).getPosition()])));}		
			}
			else if (listNutrient.get(i).getName().equals("PRESTYPE")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setCont(Integer.parseInt(alimd[listNutrient.get(i).getPosition()]));
				System.out.println(" ValType  "+ (Float.parseFloat(alimd[listNutrient.get(i).getPosition()])));}		
				
			}
			else if (listNutrient.get(i).getName().equals("INDICAT")){
				
				if (!alimd[listNutrient.get(i).getPosition()].isEmpty()){String [] indicats=alimd[listNutrient.get(i).getPosition()].split("_");
						for (String indicat: indicats){
							if (!indicat.isBlank()) {
						al.addIndicat(AlimIndic.IntToGroup(Integer.parseInt(indicat)));}}}
		
			}else if (listNutrient.get(i).getName().equals("ESPECE")){
				if (!alimd[listNutrient.get(i).getPosition()].isEmpty()){String [] indicats=alimd[listNutrient.get(i).getPosition()].split("_");
				for (String indicat: indicats){
				al.addEspeces(indicat);}}
				}
			else if (listNutrient.get(i).getName().equals("TYPEALIM")){
				if(alimd[listNutrient.get(i).getPosition()].trim().equals("COMPLET")){al.setTypeAliment(FoodKind.COMPLET);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("MEN")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("BARF")){al.setTypeAliment(FoodKind.BARF);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("USDA")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("CIQUAL")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("COMPLEMENTAIRE")){al.setTypeAliment(FoodKind.COMPLEMENTAIRE);}
				else{al.setTypeAliment(FoodKind.MEN);}
				}
			
			
		//	rep=true;
		}else if (listNutrient.get(i).getType().equals("BASE")){
			try{
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientBase.getByLabel(listNutrient.get(i).getName()));
				}

			}catch(NumberFormatException ex){
				al.removeNutrient(NutrientBase.getByLabel(listNutrient.get(i).getName()));
			}}else if (listNutrient.get(i).getType().equals("OTHER")){
				
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientOther.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					
					al.removeNutrient(NutrientOther.getByLabel(listNutrient.get(i).getName()));
				}
			}else if (listNutrient.get(i).getType().equals("MACRO")){
					try{
						if (alimd[listNutrient.get(i).getPosition()]!=""){
						al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientMacro.getByLabel(listNutrient.get(i).getName()));
						}

					}catch(NumberFormatException ex){
						al.removeNutrient(NutrientMacro.getByLabel(listNutrient.get(i).getName()));
					}
			}else if (listNutrient.get(i).getType().equals("MIN")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientMin.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrient(NutrientMin.getByLabel(listNutrient.get(i).getName()));
				}
				}
			else if (listNutrient.get(i).getType().equals("VIT")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientVitam.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrient(NutrientVitam.getByLabel(listNutrient.get(i).getName()));
				}}
			else if (listNutrient.get(i).getType().equals("LIPID")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientLipid.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrient(NutrientLipid.getByLabel(listNutrient.get(i).getName()));
				}
			}else if (listNutrient.get(i).getType().equals("AA")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrient(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), AAEnum.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrient(AAEnum.getByLabel(listNutrient.get(i).getName()));
				}
			}
		
		}
	
return al;
}
	



public AlimentUnif UpdateAlim(String [] alimd, AlimentUnif al){
	
	
	
	for (int i=1; i<listNutrient.size(); i++){
		if (listNutrient.get(i).getType().equals("NONNUTRIENT")){
			if (listNutrient.get(i).getName().equals("ID")){
				
			}else if (listNutrient.get(i).getName().equals("NAME")){
				if(alimd[listNutrient.get(i).getPosition()]!=""){
				al.setNom(alimd[listNutrient.get(i).getPosition()]);}
			}
			else if (listNutrient.get(i).getName().equals("LABEL")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setIngredients(alimd[listNutrient.get(i).getPosition()]);}
			}else if (listNutrient.get(i).getName().equals("MARQUE")){
				al.setMarque(alimd[listNutrient.get(i).getPosition()]);
			}else if (listNutrient.get(i).getName().equals("GAMME")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setGamme(alimd[listNutrient.get(i).getPosition()]);}
			}else if (listNutrient.get(i).getName().equals("PRIX")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setPrix(Float.parseFloat(alimd[listNutrient.get(i).getPosition()]));
				}
			}else if (listNutrient.get(i).getName().equals("CATEGPRIX")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setCategoriePrix(alimd[listNutrient.get(i).getPosition()]);
				}
				
			}else if (listNutrient.get(i).getName().equals("PRES")){
				if (alimd[listNutrient.get(i).getPosition()]!=""){
					
				al.setPresentation(alimd[listNutrient.get(i).getPosition()]);}
			
			}
				else if (listNutrient.get(i).getName().equals("INDICAT")){
				
				if (!alimd[listNutrient.get(i).getPosition()].isEmpty()){String [] indicats=alimd[listNutrient.get(i).getPosition()].split("_");
				al.removeAllIndicat();		
				for (String indicat: indicats){
						al.addIndicat(AlimIndic.IntToGroup(Integer.parseInt(indicat)));}}
		
			}else if (listNutrient.get(i).getName().equals("ESPECE")){
				if(alimd[listNutrient.get(i).getPosition()].trim().equals("1")){al.setEspece(Espece.CHIEN.getCategorie());}
				else if (alimd[listNutrient.get(i).getPosition()].trim().equals("0")){al.setEspece(Espece.CHAT.getCategorie());}
				else {al.setEspece(Espece.CH.getCategorie()); }
				}
			else if (listNutrient.get(i).getName().equals("TYPEALIM")){
				if(alimd[listNutrient.get(i).getPosition()].trim().equals("COMPLET")){al.setTypeAliment(FoodKind.COMPLET);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("MEN")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("BARF")){al.setTypeAliment(FoodKind.BARF);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("USDA")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("CIQUAL")){al.setTypeAliment(FoodKind.MEN);}
				else if(alimd[listNutrient.get(i).getPosition()].trim().equals("COMPLEMENTAIRE")){al.setTypeAliment(FoodKind.COMPLEMENTAIRE);}
				else{al.setTypeAliment(FoodKind.MEN);}
				}
			
			
		//	rep=true;
		}else if (listNutrient.get(i).getType().equals("BASE")){
			try{
				if (alimd[listNutrient.get(i).getPosition()]!=""){
				al.setNutrientBase(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientBase.getByLabel(listNutrient.get(i).getName()));
				}

			}catch(NumberFormatException ex){
				al.removeNutrientBase(NutrientBase.getByLabel(listNutrient.get(i).getName()));
			}}else if (listNutrient.get(i).getType().equals("OTHER")){
				
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrientOther(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientOther.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					
					al.removeNutrientOther(NutrientOther.getByLabel(listNutrient.get(i).getName()));
				}
			}else if (listNutrient.get(i).getType().equals("MACRO")){
					try{
						if (alimd[listNutrient.get(i).getPosition()]!=""){
						al.setNutrientMacro(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientMacro.getByLabel(listNutrient.get(i).getName()));
						}

					}catch(NumberFormatException ex){
						al.removeNutrientMacro(NutrientMacro.getByLabel(listNutrient.get(i).getName()));
					}
			}else if (listNutrient.get(i).getType().equals("MIN")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrientMin(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientMin.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrientMin(NutrientMin.getByLabel(listNutrient.get(i).getName()));
				}
				}
			else if (listNutrient.get(i).getType().equals("VIT")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrientVitam(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientVitam.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrientVitam(NutrientVitam.getByLabel(listNutrient.get(i).getName()));
				}}
			else if (listNutrient.get(i).getType().equals("LIPID")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrientLipid(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), NutrientLipid.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrientLipid(NutrientLipid.getByLabel(listNutrient.get(i).getName()));
				}
			}else if (listNutrient.get(i).getType().equals("AA")){
				try{
					if (alimd[listNutrient.get(i).getPosition()]!=""){
					al.setNutrientAcideAmine(listNutrient.get(i).getCoef()*Float.parseFloat(alimd[listNutrient.get(i).getPosition()]), AAEnum.getByLabel(listNutrient.get(i).getName()));
					}

				}catch(NumberFormatException ex){
					al.removeNutrientAcideAmine(AAEnum.getByLabel(listNutrient.get(i).getName()));
				}
			}
		
		}
	
return al;
}
	

public AdressNutrientTable getFromName(ArrayList<AdressNutrientTable>tab, String n) {
	for (AdressNutrientTable a:tab) {
		if (a.getName().equals(n)) {
			return a;
		}
	}

	return null;
	
}
}

