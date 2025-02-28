package model;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import DataStruct.NutrientP;
import Enumerise.AAEnum;
import Enumerise.ContEnum;
import Enumerise.FoodKind;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;

public class AlimentUnif implements Serializable {
	static private final long serialVersionUID = 101L;
	public int consistent=1;
	public String UUID;
	private String nom="";
	private GroupAlim group;
	private TypeAlim typeAliment;
	private FoodKind foodKind;
	private String ingredients=""; 
	private double prix=0;
	private String categoriePrix="i";
private String marque="";
	private ArrayList<String> indication=new ArrayList<String>();
	private int espece;
	private ArrayList<String>Especes=new ArrayList<String>();
private String gamme="";
private String presentation="";
private float quantInt=0;
private ContEnum cont=ContEnum.NO;
public boolean deprecated=false;
public String DataB="6";
	//contenu en pour %

	private float NutrientAcideAmineVar []=new float[22];
	private float NutrientBaseVar []=new float[NutrientBase.size()];
	private float NutrientLipidVar []=new float[21];
	private float NutrientMacroVar []=new float[6];
	private float NutrientMinVar []=new float[6];
	private float NutrientVitamVar []=new float[16];
	private float NutrientOtherVar []=new float[20];
	private boolean NutrientAcideAminePresence []=new boolean[22];
	private boolean NutrientBasePresence []=new boolean[NutrientBase.size()];
	private boolean NutrientLipidPresence []=new boolean[21];
	private boolean NutrientMacroPresence []=new boolean[6];
	private boolean NutrientMinPresence []=new boolean[6];
	private boolean NutrientVitamPresence []=new boolean[16];
	private boolean NutrientOtherPresence []=new boolean[20];
	//

	public AlimentUnif() {
UUID=java.util.UUID.randomUUID().toString();
typeAliment=TypeAlim.FUSION;
foodKind=FoodKind.MEN;
group=GroupAlim.AIDE;
for (float element:NutrientAcideAmineVar){
	element=0;
}
for (float element:NutrientBaseVar){
	element=0;
}
for (float element:NutrientLipidVar){
	element=0;
}
for (float element:NutrientMacroVar){
	element=0;
}
for (float element:NutrientMinVar){
	element=0;
}
for (float element:NutrientVitamVar){
	element=0;
}
for (float element:NutrientOtherVar){
	element=0;
}
for (boolean element:NutrientAcideAminePresence){
	element=false;
}
for (boolean element:NutrientBasePresence){
	element=false;
}
for (boolean element:NutrientLipidPresence){
	element=false;
}
for (boolean element:NutrientMacroPresence){
	element=false;
}
for (boolean element:NutrientMinPresence){
	element=false;
}
for (boolean element:NutrientVitamPresence){
	element=false;
}
for (boolean element:NutrientOtherPresence){
	element=false;
}

	}
	public AlimentUnif(String uuid) {
		UUID=uuid;
		typeAliment=TypeAlim.FUSION;
		group=GroupAlim.AIDE;
		for (float element:NutrientAcideAmineVar){
			element=0;
		}
		for (float element:NutrientBaseVar){
			element=0;
		}
		for (float element:NutrientLipidVar){
			element=0;
		}
		for (float element:NutrientMacroVar){
			element=0;
		}
		for (float element:NutrientMinVar){
			element=0;
		}
		for (float element:NutrientVitamVar){
			element=0;
		}
		for (float element:NutrientOtherVar){
			element=0;
		}
		for (boolean element:NutrientAcideAminePresence){
			element=false;
		}
		for (boolean element:NutrientBasePresence){
			element=false;
		}
		for (boolean element:NutrientLipidPresence){
			element=false;
		}
		for (boolean element:NutrientMacroPresence){
			element=false;
		}
		for (boolean element:NutrientMinPresence){
			element=false;
		}
		for (boolean element:NutrientVitamPresence){
			element=false;
		}
		for (boolean element:NutrientOtherPresence){
			element=false;
		}

			}
	public String getUUID() {
		return UUID;
	}
	public void setPresentation(String presentation) {
		this.presentation = presentation;
	}
	public String getPresentation() {
		return presentation;
	}
	public void setCategoriePrix(String categoriePrix) {
		this.categoriePrix = categoriePrix;
	}
	public String getCategoriePrix() {
		return categoriePrix;
	}
	public void setPrix(double prix) {
		this.prix = prix;
	}
	public double getPrix() {
		return prix;
	}
	public String getFamillyBrand() {
		String s= new String();
				switch (this.getTypeAliment()){
				case MEN:
					case BARF:
						s=this.getGroup().nameToString();
						break;
					case COMPLET:
						s=this.getMarque();
						break;
					default:
						s=this.getMarque();
					break;
				}
				return s;
			}
	
	public void update(AlimentUnif aliment){
		if (!aliment.getNom().equals("")){
			this.setNom(aliment.getNom());
		}
		if (!aliment.getGamme().equals("")){
			this.setGamme(aliment.getGamme());
		}
		if (!aliment.getGroup().equals("")){
			this.setGroup(aliment.getGroup());
		}
		if (!aliment.getMarque().equals("")){
			this.setMarque(aliment.getMarque());
		}
		if (!aliment.getIngredients().equals("")){
			this.setIngredients(aliment.getIngredients());
		}
		
		if (aliment.getIndicat().size()>1){
			this.setIndicat(aliment.getIndicat());
		}else if(!aliment.getIndicat().get(0).equals(AlimIndic.PHYS.nameToString())){
			this.setIndicat(aliment.getIndicat());
			}
		for(AAEnum elem:AAEnum.values()){
			if (aliment.isNutrientAcideAmine(elem)){
				this.setNutrientAcideAmine(aliment.getNutrientAcideAmine(elem), elem);
			}
		}
		for(NutrientBase elem:NutrientBase.values()){
			if(aliment.isNutrientBase(elem)){
				this.setNutrientBase(aliment.getNutrientBase(elem), elem);
			}
		}
		for(NutrientMin elem:NutrientMin.values()){
			if(aliment.isNutrientMin(elem)){
				this.setNutrientMin(aliment.getNutrientMin(elem), elem);
			}
		}
		for(NutrientMacro elem:NutrientMacro.values()){
			if(aliment.isNutrientMacro(elem)){
				this.setNutrientMacro(aliment.getNutrientMacro(elem), elem);
			}
		}
		for(NutrientLipid elem:NutrientLipid.values()){
			if(aliment.isNutrientLipid(elem)){
				this.setNutrientLipid(aliment.getNutrientLipid(elem), elem);
			}
		}
		for(NutrientVitam elem:NutrientVitam.values()){
			if(aliment.isNutrientVitam(elem)){
				this.setNutrientVitam(aliment.getNutrientVitam(elem), elem);
			}
		}
		for(NutrientOther elem:NutrientOther.values()){
			if(aliment.isNutrientOther(elem)){
				this.setNutrientOther(aliment.getNutrientOther(elem), elem);
			}
		}
	}
	public void setNutrientAcideAmine(float a,AAEnum enu){
		
		this.NutrientAcideAmineVar[enu.getCoef()]=a;
		this.NutrientAcideAminePresence[enu.getCoef()]= true;
		
	}
	public void setNutrientBase(float a,NutrientBase enu){
		try{
		this.NutrientBaseVar[enu.getCoef()]=a;
		this.NutrientBasePresence[enu.getCoef()]= true;}
		catch(ArrayIndexOutOfBoundsException e){
			this.NutrientBaseVar=resize(NutrientBaseVar, NutrientBase.size());
			this.NutrientBasePresence=resize(NutrientBasePresence, NutrientBase.size());
					this.NutrientBaseVar[enu.getCoef()]=a;
			this.NutrientBasePresence[enu.getCoef()]= true;
		}
	}
	public void setNutrientLipid(float a,NutrientLipid enu){
		try{
			this.NutrientLipidVar[enu.getCoef()]=a;
			this.NutrientLipidPresence[enu.getCoef()]= true;}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientLipidVar=resize(NutrientLipidVar, NutrientLipid.size());
				this.NutrientLipidPresence=resize(NutrientLipidPresence, NutrientLipid.size());
						this.NutrientLipidVar[enu.getCoef()]=a;
				this.NutrientLipidPresence[enu.getCoef()]= true;
			}
	}
	public void setNutrientMacro(float a,NutrientMacro enu){
		try{
			this.NutrientMacroVar[enu.getCoef()]=a;
			this.NutrientMacroPresence[enu.getCoef()]= true;}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientMacroVar=resize(NutrientMacroVar, NutrientMacro.size());
				this.NutrientMacroPresence=resize(NutrientMacroPresence, NutrientMacro.size());
						this.NutrientMacroVar[enu.getCoef()]=a;
				this.NutrientMacroPresence[enu.getCoef()]= true;
			}
	}
	public void setNutrientMin(float a,NutrientMin enu){
		try{
			this.NutrientMinVar[enu.getCoef()]=a;
			this.NutrientMinPresence[enu.getCoef()]= true;}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientMinVar=resize(NutrientMinVar, NutrientMin.size());
				this.NutrientMinPresence=resize(NutrientMinPresence, NutrientMin.size());
						this.NutrientMinVar[enu.getCoef()]=a;
				this.NutrientMinPresence[enu.getCoef()]= true;
			}
	}
	public void setNutrientVitam(float a,NutrientVitam enu){
		try{
			this.NutrientVitamVar[enu.getCoef()]=a;
			this.NutrientVitamPresence[enu.getCoef()]= true;}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientVitamVar=resize(NutrientVitamVar, NutrientVitam.size());
				this.NutrientVitamPresence=resize(NutrientVitamPresence, NutrientVitam.size());
						this.NutrientVitamVar[enu.getCoef()]=a;
				this.NutrientVitamPresence[enu.getCoef()]= true;
			}
	}
	public void setNutrientOther(float a,NutrientOther enu){
		try{
			this.NutrientOtherVar[enu.getCoef()]=a;
			this.NutrientOtherPresence[enu.getCoef()]= true;}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientOtherVar=resize(NutrientOtherVar, NutrientOther.size());
				this.NutrientOtherPresence=resize(NutrientOtherPresence, NutrientOther.size());
						this.NutrientOtherVar[enu.getCoef()]=a;
				this.NutrientOtherPresence[enu.getCoef()]= true;
			}
	}
	public float getNutrientAcideAmine(AAEnum enu){
		
		return this.NutrientAcideAmineVar[enu.getCoef()];
	
	}
	public float getNutrientBase(NutrientBase enu){
		try{
			switch(enu) {
			case FIBRETOT:
				if (!NutrientBasePresence[enu.getCoef()] 
						& (NutrientBasePresence[NutrientBase.CELLULOSE.getCoef()]))
							{
					return this.NutrientBaseVar[NutrientBase.CELLULOSE.getCoef()];
				}else{ 
					return this.NutrientBaseVar[enu.getCoef()];
				}
			case ENA:
				if (NutrientBasePresence[enu.getCoef()] ) {
					return this.NutrientBaseVar[enu.getCoef()];
				}
				else	if (NutrientBasePresence[NutrientBase.LIPIDE.getCoef()]&
				NutrientBasePresence[NutrientBase.PROTEINE.getCoef()]&
				NutrientBasePresence[NutrientBase.HUMIDITE.getCoef()]&
				NutrientBasePresence[NutrientBase.CELLULOSE.getCoef()]&
				NutrientBasePresence[NutrientBase.CENDRE.getCoef()]
				){
					return (100 -this.NutrientBaseVar[NutrientBase.CELLULOSE.getCoef()]
						 -this.NutrientBaseVar[NutrientBase.PROTEINE.getCoef()]
									 -this.NutrientBaseVar[NutrientBase.LIPIDE.getCoef()]
											 -this.NutrientBaseVar[NutrientBase.HUMIDITE.getCoef()]
													 -this.NutrientBaseVar[NutrientBase.CENDRE.getCoef()]>0?100 -this.NutrientBaseVar[NutrientBase.CELLULOSE.getCoef()]
															 -this.NutrientBaseVar[NutrientBase.PROTEINE.getCoef()]
																	 -this.NutrientBaseVar[NutrientBase.LIPIDE.getCoef()]
																			 -this.NutrientBaseVar[NutrientBase.HUMIDITE.getCoef()]
																					 -this.NutrientBaseVar[NutrientBase.CENDRE.getCoef()]:0);
															 
				}	else	if (NutrientBasePresence[NutrientBase.LIPIDE.getCoef()]&
						NutrientBasePresence[NutrientBase.PROTEINE.getCoef()]&
						NutrientBasePresence[NutrientBase.HUMIDITE.getCoef()]&
						NutrientBasePresence[NutrientBase.FIBRETOT.getCoef()]&
						NutrientBasePresence[NutrientBase.CENDRE.getCoef()]
						){
							return (100 -this.NutrientBaseVar[NutrientBase.FIBRETOT.getCoef()]
								 -this.NutrientBaseVar[NutrientBase.PROTEINE.getCoef()]
											 -this.NutrientBaseVar[NutrientBase.LIPIDE.getCoef()]
													 -this.NutrientBaseVar[NutrientBase.HUMIDITE.getCoef()]
															 -this.NutrientBaseVar[NutrientBase.CENDRE.getCoef()]>0?100 -this.NutrientBaseVar[NutrientBase.FIBRETOT.getCoef()]
																	 -this.NutrientBaseVar[NutrientBase.PROTEINE.getCoef()]
																			 -this.NutrientBaseVar[NutrientBase.LIPIDE.getCoef()]
																					 -this.NutrientBaseVar[NutrientBase.HUMIDITE.getCoef()]
																							 -this.NutrientBaseVar[NutrientBase.CENDRE.getCoef()]:0);
				
				}	else	if (NutrientBasePresence[NutrientBase.AMIDON.getCoef()]|
						NutrientBasePresence[NutrientBase.SUCRE.getCoef()]
						){
							return this.NutrientBaseVar[NutrientBase.AMIDON.getCoef()]+this.NutrientBaseVar[NutrientBase.SUCRE.getCoef()] ;}
				else {return 0;}
			default:
			return this.NutrientBaseVar[enu.getCoef()];
			}}
		catch(ArrayIndexOutOfBoundsException e){
			this.NutrientBaseVar=resize(NutrientBaseVar, NutrientBase.size());
			this.NutrientBasePresence=resize(NutrientBasePresence, NutrientBase.size());
					return this.NutrientBaseVar[enu.getCoef()];
			
		}

	}
	public float getNutrientLipid(NutrientLipid enu){
		try{
			if(enu.equals(NutrientLipid.EPADHA)){
				if (!NutrientLipidPresence[enu.getCoef()] 
						& (NutrientLipidPresence[NutrientLipid.AG205.getCoef()] 
								| NutrientLipidPresence[NutrientLipid.AG226.getCoef()])){
					return this.NutrientLipidVar[NutrientLipid.AG205.getCoef()]+this.NutrientLipidVar[NutrientLipid.AG226.getCoef()];
				}else{ 
					return this.NutrientLipidVar[enu.getCoef()];
				}
			}else{
			return this.NutrientLipidVar[enu.getCoef()];}}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientLipidVar=resize(NutrientLipidVar, NutrientLipid.size());
				this.NutrientLipidPresence=resize(NutrientLipidPresence, NutrientLipid.size());
						return this.NutrientLipidVar[enu.getCoef()];
				
			}
		
	}
	public float getNutrientMacro(NutrientMacro enu){
		try{
			return this.NutrientMacroVar[enu.getCoef()];}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientMacroVar=resize(NutrientMacroVar, NutrientMacro.size());
				this.NutrientMacroPresence=resize(NutrientMacroPresence, NutrientMacro.size());
						return this.NutrientMacroVar[enu.getCoef()];
				
			}
	
	}
	public float getNutrientMin(NutrientMin enu){
		try{
			return this.NutrientMinVar[enu.getCoef()];}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientMinVar=resize(NutrientMinVar, NutrientMin.size());
				this.NutrientMinPresence=resize(NutrientMinPresence, NutrientMin.size());
						return this.NutrientMinVar[enu.getCoef()];
				
			}
		
	}
	public float getNutrientVitam(NutrientVitam enu){
		try{
			return this.NutrientVitamVar[enu.getCoef()];}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientVitamVar=resize(NutrientVitamVar, NutrientVitam.size());
				this.NutrientVitamPresence=resize(NutrientVitamPresence, NutrientVitam.size());
						return this.NutrientVitamVar[enu.getCoef()];
				
			}
	}
	public float getNutrientOther(NutrientOther enu){
		try{
			return this.NutrientOtherVar[enu.getCoef()];}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientOtherVar=resize(NutrientOtherVar, NutrientOther.size());
				this.NutrientOtherPresence=resize(NutrientOtherPresence, NutrientOther.size());
						return this.NutrientOtherVar[enu.getCoef()];
				
			}
		
	}
	public boolean isNutrientAcideAmine(AAEnum enu){
		return this.NutrientAcideAminePresence[enu.getCoef()];
	
	}
	public boolean isNutrientBase(NutrientBase enu){
		try{
			return this.NutrientBasePresence[enu.getCoef()];}
			catch(ArrayIndexOutOfBoundsException e){
				this.NutrientBaseVar=resize(NutrientBaseVar, NutrientBase.size());
				this.NutrientBasePresence=resize(NutrientBasePresence, NutrientBase.size());
				return this.NutrientBasePresence[enu.getCoef()];
			}


	}
	public boolean isNutrientLipid(NutrientLipid enu){
		return this.NutrientLipidPresence[enu.getCoef()];
		
	}
	public boolean isNutrientMacro(NutrientMacro enu){
		return this.NutrientMacroPresence[enu.getCoef()];
	
	}
	public boolean isNutrientMin(NutrientMin enu){
		return this.NutrientMinPresence[enu.getCoef()];
		
	}
	public boolean isNutrientVitam(NutrientVitam enu){
		return this.NutrientVitamPresence[enu.getCoef()];
	
	}
	public boolean isNutrientOther(NutrientOther enu){
		return this.NutrientOtherPresence[enu.getCoef()];
		
	}
	public void removeNutrientAcideAmine(AAEnum enu){
		this.NutrientAcideAmineVar[enu.getCoef()]=0;
		this.NutrientAcideAminePresence[enu.getCoef()]= false;
	}
	public void removeNutrientBase(NutrientBase enu){
		this.NutrientBaseVar[enu.getCoef()]=0;
		this.NutrientBasePresence[enu.getCoef()]= false;
	}
	public void removeNutrientLipid(NutrientLipid enu){
		this.NutrientLipidVar[enu.getCoef()]=0;
		this.NutrientLipidPresence[enu.getCoef()]= false;
	}
	public void removeNutrientMacro(NutrientMacro enu){
		this.NutrientMacroVar[enu.getCoef()]=0;
		this.NutrientMacroPresence[enu.getCoef()]= false;
	}
	public void removeNutrientMin(NutrientMin enu){
		this.NutrientMinVar[enu.getCoef()]=0;
		this.NutrientMinPresence[enu.getCoef()]= false;
	}
	public void removeNutrientVitam(NutrientVitam enu){
		this.NutrientVitamVar[enu.getCoef()]=0;
		this.NutrientVitamPresence[enu.getCoef()]= false;
	}
	public void removeNutrientOther(NutrientOther enu){
		this.NutrientOtherVar[enu.getCoef()]=0;
		this.NutrientOtherPresence[enu.getCoef()]= false;
	}

	public float enerDensCat(int d){
		if (typeAliment.equals(TypeAlim.COMPLET)){
		if (d==0){
		float GE = 5.7F * this.getNutrientBase(NutrientBase.PROTEINE) + 9.4F*this.getNutrientBase(NutrientBase.LIPIDE)+ 4.1F*(this.getNutrientBase(NutrientBase.ENA)+this.getNutrientBase(NutrientBase.CELLULOSE));
		float digestible=87.9F -(0.88F *this.getNutrientBase(NutrientBase.CELLULOSE)*100.F/(100.F-this.getNutrientBase(NutrientBase.HUMIDITE)));
		float DE=GE*digestible/100.F;
		return DE-0.77F*this.getNutrientBase(NutrientBase.PROTEINE);}
		else{
		return  3.5F * this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F*this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*(this.getNutrientBase(NutrientBase.ENA));}} else{
			return  4.F *  this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4.F*( this.getNutrientBase(NutrientBase.ENA));
		}
	
	}
	public float enerDensDog(int d){
		if (typeAliment.equals(TypeAlim.COMPLET)){
		if (d==0){
		float GE = 5.7F *  this.getNutrientBase(NutrientBase.PROTEINE)+ 9.4F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4.1F*( this.getNutrientBase(NutrientBase.ENA)+ this.getNutrientBase(NutrientBase.CELLULOSE));
		float digestible=91.2F -(1.43F * this.getNutrientBase(NutrientBase.CELLULOSE)*100.F/(100.F- this.getNutrientBase(NutrientBase.HUMIDITE)));
		float DE=GE*digestible/100.F;
		return DE-1.04F* this.getNutrientBase(NutrientBase.PROTEINE);}
		else{
		
			return  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrientBase(NutrientBase.ENA));
		}}else
		{
			return 4F *  this.getNutrientBase(NutrientBase.PROTEINE) + 9F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4F*( this.getNutrientBase(NutrientBase.ENA));
		}
		//return  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE)e + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrientBase(NutrientBase.ENA));
	
	}
	public float getEner(Espece esp) {
		float ener=0;
		switch(esp) {
		case CHIEN :
			ener=enerDensDog(0);
			break;
		case CHAT:
			ener=enerDensCat(0);
			break;
			default:
				ener=enerDensDog(1);
		}
		return ener;
	}
	public float getProtEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE) /(  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrientBase(NutrientBase.ENA)));
						default:
				return	  4F *  this.getNutrientBase(NutrientBase.PROTEINE) /(  4F *  this.getNutrientBase(NutrientBase.PROTEINE) + 9F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4F*( this.getNutrientBase(NutrientBase.ENA)));
						}
	}
	public float getENAEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  3.5F *  this.getNutrientBase(NutrientBase.ENA) /(  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrientBase(NutrientBase.ENA)));
						default:
				return	  4F *  this.getNutrientBase(NutrientBase.ENA) /(  4F *  this.getNutrientBase(NutrientBase.PROTEINE) + 9F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4F*( this.getNutrientBase(NutrientBase.ENA)));
						}
	}
	public float getLipEner() {
		switch(this.getTypeAliment()) {
		case COMPLET:
		case COMPLEMENTAIRE:
	return	  8.5F *  this.getNutrientBase(NutrientBase.LIPIDE) /(  3.5F *  this.getNutrientBase(NutrientBase.PROTEINE) + 8.5F* this.getNutrientBase(NutrientBase.LIPIDE)+ 3.5F*( this.getNutrientBase(NutrientBase.ENA)));
						default:
				return	  9F *  this.getNutrientBase(NutrientBase.LIPIDE) /(  4F *  this.getNutrientBase(NutrientBase.PROTEINE) + 9F* this.getNutrientBase(NutrientBase.LIPIDE)+ 4F*( this.getNutrientBase(NutrientBase.ENA)));
						}
	}
	public void setEspece(int espece) {
		this.espece = espece;
	}
	public String getGamme() {
		return gamme;
	}
	public void setGamme(String gamme) {
		this.gamme = gamme;
	}
	public void setGroup(String string) {
		
		this.group =GroupAlim.StringToGroup(string);
	}
	public void setGroup(GroupAlim string) {
		
		this.group =string;
	}
	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public void setTypeAliment(FoodKind typeAliment) {
		this.foodKind = typeAliment;
	}
	public int getEspece() {
		return espece;
	}
	public GroupAlim getGroup() {
		return group;
	}
	public String getIngredients() {
		return ingredients;
	}
	public String getNom() {
		return nom;
	}
	public FoodKind getTypeAliment() {
		if (foodKind==null) {
			foodKind=FoodKind.IntToType(typeAliment.getCoef())
;		}
		return foodKind;
	}
public String getMarque() {
	return marque;
}
public void setMarque(String marque) {
	this.marque = marque;
}

	public ArrayList<String> getIndicat(){
		try{
		 if (indication.isEmpty()){
			 indication.add(AlimIndic.PHYS.nameToString());
		 }}
		catch(NullPointerException e){
			indication=new ArrayList<String>();
			 indication.add(AlimIndic.PHYS.nameToString());
		}
		 return indication;
		
	 }
	public String getOneIndicat(int i){
		try{
		 if (indication.isEmpty()){
			 indication.add(AlimIndic.PHYS.nameToString());
		 }}
		catch(NullPointerException e){
			indication=new ArrayList<String>();
			 indication.add(AlimIndic.PHYS.nameToString());
		}
		 return indication.get(i);
		
	 }
	public void addIndicat(AlimIndic ind){
		indication.add(ind.nameToString());
	}
	public void setIndicat(List<String> ind){
		this.indication=(ArrayList<String>) ind;
	/*	this.indication.removeAll(this.indication);
		for (int i=0; i<ind.size(); i++){
		this.indication.add(ind.get(i));
		}*/
		
	}
	public void addIndicat(int i){
		   AlimIndic ind= AlimIndic.IntToGroup(i);
		indication.add(ind.nameToString());
	}
	public void removeAllIndicat(){
		indication.removeAll(indication);
	}
	public float []resize(float[] obj, int size) {
		float[] export= new float [size];
		for(int i=0; i<obj.length; i++){
			export[i]=obj[i];
		}
		
		return export;
		
	}
	public boolean []resize(boolean[] obj, int size) {
	boolean[] export= new boolean [size];
		for(int i=0; i<obj.length; i++){
			export[i]=obj[i];
		}
		for (int i=obj.length-1; i<size; i++){
			export[i]=false;
		}
		return export;
		
	}
	
	public void updateByNutrient(NutrientP np, float unitF, float ProtValue) {
	boolean present=false;
	float value=0F;
		if (np.getQuantity()=="") {
			present=false;
		}else {
			
			present=true;
			if (unitF==-1 & np.getMNE().equals(MainNutrientEnum.AMA)) {
				
				value=Float.parseFloat(np.getQuantity())*np.getConverter();
				
				
			}else if (unitF!=-1 & np.getMNE().equals(MainNutrientEnum.AMA)) {
				value=100*Float.parseFloat(np.getQuantity())*np.getConverter()/ProtValue;
			}
			else {
			value=Float.parseFloat(np.getQuantity())*np.getConverter()*unitF;}}
		
		
		switch (np.getMNE()) {
		case BASE:
		if (present) {
		this.setNutrientBase(value, NutrientBase.getByCoef(np.getKind().get()));}
		else {
			this.removeNutrientBase(NutrientBase.getByCoef(np.getKind().get()));
		}
			break;
		case MACRO:
			if (present) {
			this.setNutrientMacro(value, NutrientMacro.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientMacro(NutrientMacro.getByCoef(np.getKind().get()));
			}
				break;
		case MIN:
			if (present) {
			this.setNutrientMin(value, NutrientMin.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientMin(NutrientMin.getByCoef(np.getKind().get()));
			}
				break;
		case VITAM:
			if (present) {
			this.setNutrientVitam(value, NutrientVitam.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientVitam(NutrientVitam.getByCoef(np.getKind().get()));
			}
				break;
		case AMA:
			if (present) {
			this.setNutrientAcideAmine(value, AAEnum.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientAcideAmine(AAEnum.getByCoef(np.getKind().get()));
			}
				break;
		case LIPID:
			if (present) {
			this.setNutrientLipid(value, NutrientLipid.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientLipid(NutrientLipid.getByCoef(np.getKind().get()));
			}
				break;
		case OTHER:
			if (present) {
			this.setNutrientOther(value, NutrientOther.getByCoef(np.getKind().get()));}
			else {
				this.removeNutrientOther(NutrientOther.getByCoef(np.getKind().get()));
			}
				break;
		}
	}
	public ArrayList<String> getEspeces() {
		return Especes;
	}
	public void setEspeces(ArrayList<String> especes) {
		Especes = especes;
	}
	public void addEspeces(String Esp) {
		Especes.add(Esp);
	}
	
	public boolean isEspece(String Esp) {
		if (Esp.equals("ALL")) {
			return true;
		}else
		{
			for (String s:Especes) {
				if (s.equals(Esp)) { 
					return true;
				}
			}
		}
	return false;
	}
	public int getConsistent() {
		return consistent;
	}
	public void setConsistent(int consistent) {
		this.consistent = consistent;
	}
	 public ContEnum getCont() {
		 if(cont!=null) {
				return cont;
		 }else {
				return ContEnum.NO;
		 }
	
	}
	 public float getQuantInt() {
		return quantInt;
	}
	 public void setCont(ContEnum cont) {
		this.cont = cont;
	}
	 public void setCont(int cont) {

		this.cont = ContEnum.byId(cont);
	}
	 public void setQuantInt(float quantInt) {
		this.quantInt = quantInt;
	}
	 public boolean isDeprecated() {
		return deprecated;
	}
	 public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}
	 public void setDeprecated(int deprecated) {
		this.deprecated = (deprecated==1);
	}
	 public String getDataB() {
		return DataB;
	}
	 public void setDataB(String dataB) {
		DataB = dataB;
	}
	 
	
	 public AlimentUnif clone() {
	        AlimentUnif o = new AlimentUnif();
	  o.NutrientAcideAminePresence=this.NutrientAcideAminePresence;
	            o.setNom("(Dup) "+getNom());
	           o.group=this.getGroup();
	           o.foodKind=this.getTypeAliment();
	           o.ingredients=this.getIngredients();
	           o.prix=this.getPrix();
	           o.categoriePrix=this.getCategoriePrix();
	           o.marque=this.getMarque();
	           o.indication=this.indication;
	        		   o.espece=espece;
	        		   o.Especes=Especes;
	        		   o.gamme=gamme;
	        		   o.presentation=presentation;
	        		   o.NutrientAcideAminePresence=this.NutrientAcideAminePresence;
	        		   o.NutrientAcideAmineVar=NutrientAcideAmineVar;
	           o.NutrientBasePresence=this.NutrientBasePresence;
	           o.NutrientBaseVar=this.NutrientBaseVar;
	           o.NutrientLipidPresence=this.NutrientLipidPresence;
	           o.NutrientLipidVar=this.NutrientLipidVar;
	           o.NutrientMacroPresence=this.NutrientMacroPresence;
	           o.NutrientMacroVar=this.NutrientMacroVar;
	           o.NutrientMinPresence=this.NutrientMinPresence;
	           o.NutrientMinVar=this.NutrientMinVar;
	           o.NutrientOtherPresence=this.NutrientOtherPresence;
	           o.NutrientOtherVar=this.NutrientOtherVar;
	           o.NutrientVitamPresence=this.NutrientVitamPresence;
	           o.NutrientVitamVar=this.NutrientVitamVar;
	           o.quantInt=this.quantInt;
	           o.cont=this.cont;
	           o.deprecated=this.deprecated;
	           o.DataB=this.DataB;
	           
	           
	        
	        // on renvoie le clone
	        return o;
	    }
}
