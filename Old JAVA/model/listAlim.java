package model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;





public class listAlim implements Serializable, Observable{
	static private final long serialVersionUID = 1L;
	private List<AlimentUnif> alim= new ArrayList<AlimentUnif>();

	public listAlim() {
		// TODO Auto-generated constructor stub
	}
	public List<AlimentUnif> getAlim() {
		return alim;
	}
	public AlimentUnif getAlim(String nom) {
		int a=0;
		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getNom().equals(nom)){
				a=i;

			}
		}
		return alim.get(a);



	}
	public AlimentUnif getAlimByUUID(String UUID) {
		int a=0;

		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(UUID)){
				a=i;


			}
		}

		return alim.get(a);




	}
	public boolean isAlimByUUID(String UUID) {
		boolean a=false;

		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(UUID)){
				a=true;


			}
		}

		return a;




	}
	public listAlim keepOnly(GroupAlim groupAl){
		listAlim clone=new listAlim();
		for (AlimentUnif al:alim){
			if (al.getGroup().nameToString().equals(groupAl.nameToString())){
				clone.addAlim(al);
			}
		}
		return clone;
	}
	public listAlim keepOnly(AlimIndic groupAl){
		listAlim list2=new listAlim();
		for (AlimentUnif al:alim){
			for (String Indic:al.getIndicat()){
				if (Indic.equals(groupAl.nameToString())){
					list2.addAlim(al);
				}
			}
		}
		return list2;
	}
	public listAlim keepOnlyByType(TypeAlim groupAl){
		listAlim list2=new listAlim();
		for (AlimentUnif al:alim){

			if (groupAl.equals(al.getTypeAliment())){
				list2.addAlim(al);
			}

		}
		return list2;
	}
	public listAlim keepOnlyByEspece(Espece groupAl){
		listAlim list2=new listAlim();
		for (AlimentUnif al:alim){

			if (al.getEspece()==groupAl.getCategorie()){
				list2.addAlim(al);
			}

		}
		return list2;
	}
	public listAlim keepOnlyByMarque(String groupAl){
		listAlim list2=new listAlim();
		for (AlimentUnif al:alim){

			if (al.getMarque().equals(groupAl)){
				list2.addAlim(al);
			}

		}
		return list2;
	}
	public boolean getAlimByUUIDExist(String UUID) {
		boolean spy=false;

		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(UUID)){

				spy=true;

			}
		}

		return spy;




	}
	public void replaceAlim(AlimentUnif aliment) {
		int a=0;
		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(aliment.getUUID())){
				a=i;

			}
		}
		alim.remove(a);
		alim.add(aliment);



	}
	public void updateAlim(AlimentUnif aliment) {
		int a=0;
		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(aliment.getUUID())){
				a=i;

			}
		}
		AlimentUnif tempalim=alim.get(a);
		tempalim.update(aliment);
		alim.remove(a);
		alim.add(tempalim);



	}

	public AlimentUnif getAlim(int a) {

		return alim.get(a);



	}
	public int size(){
		return this.alim.size();
	}
	public void addAlim(AlimentUnif a){
		if (getAlimByUUIDExist(a.getUUID())){
			this.replaceAlim(a);
		}else{
			this.alim.add(a);}
	}
	public void removeAlim(String nom){
		int a=0;
		boolean touch=false;
		System.out.println(a +" a in");
		for(int i=0;i<alim.size();i++){
			if (alim.get(i).getUUID().equals(nom)){
				a=i;
				touch=true;
				System.out.println(a +" a sup");
			}
		}
		if (touch){
			this.alim.remove(a);
		}
	}
	public ArrayList<String> getMarque(TypeAlim type){
		ArrayList<String> listMarque= new ArrayList<String>();
		listMarque.add("Toutes Marques");
		boolean exist=false;
		List<AlimentUnif> alim= this.keepOnlyByType(type).getAlim();
		if (type.equals(TypeAlim.COMPLET)||type.equals(TypeAlim.COMPLEMENTAIRE)){
			for (AlimentUnif ali:alim){
				exist=false;

				for (String str:listMarque){
					if (str.equals(ali.getMarque())){
						exist=true;
					}
				}
				if (! exist){

					listMarque.add(ali.getMarque());
				}
			}

		}
		return listMarque;
	}
	public listAlim subList(String Type, String especeStr, String marqueStr, String IndicatOrGroup, String wordSearch){
		listAlim nsubList=new listAlim();
		TypeAlim type=TypeAlim.StringToType(Type);
		switch (TypeAlim.StringToType(Type)){
		case CIQUAL:
		case USDA:
		case FUSION:
		case BARF:
			for (AlimentUnif al:alim){

				if (type.equals(al.getTypeAliment())){
					if (!IndicatOrGroup.equals("Tous les aliments")){
						if (al.getGroup().nameToString().equals(IndicatOrGroup)){
							if (!wordSearch.equals("")){
								String [] words=wordSearch.split(" ");
								boolean prs=true;
								for (String word: words){
									if ((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
										prs=prs && true;}
									else {prs=false;}}
								if (prs){
									nsubList.addAlim(al);
								}					

							}
							else{
								nsubList.addAlim(al);
							}

						}
					}
					else{
						if (!wordSearch.equals("")){
							String [] words=wordSearch.split(" ");
							boolean prs=true;
							for (String word: words){
								if ((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
									prs=prs && true;}
								else {prs=false;}}
							if (prs){
								nsubList.addAlim(al);
							}					

						}
						else{
							nsubList.addAlim(al);
						}
					}

				}


			}
			break;



		case COMPLET:
		case COMPLEMENTAIRE:
	
			for (AlimentUnif al:alim){

				if (type.equals(al.getTypeAliment())){
					if (!IndicatOrGroup.equals("Tous les aliments")){
						boolean indicp=false;
						for (String Indic:al.getIndicat()){
							if (Indic.equals(IndicatOrGroup)){
								indicp=true;
							}}
						if (indicp){
							if (!especeStr.equals("Tous les aliments")){

								if (al.getEspece()==Espece.getEnumFromString(especeStr).getCategorie()){
									if (!marqueStr.equals("Toutes Marques")){
										if (al.getMarque().equals(marqueStr)){
											if (!wordSearch.equals("")){
												String [] words=wordSearch.split(" ");
												boolean prs=true;
												for (String word: words){
													if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
															prs=prs && true;}
													else {prs=false;}}
												if (prs){
													nsubList.addAlim(al);
												}					

											}
											else{
												nsubList.addAlim(al);
											}
										}

									}

									else{
										if (!wordSearch.equals("")){
											String [] words=wordSearch.split(" ");
											boolean prs=true;
											for (String word: words){
												if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
															prs=prs && true;}
												else {prs=false;}}
											if (prs){
												nsubList.addAlim(al);
											}					

										}
										else{
											nsubList.addAlim(al);
										}
									}

								}


							}else{
								if (!marqueStr.equals("Toutes Marques")){
									if (al.getMarque().equals(marqueStr)){
										if (!wordSearch.equals("")){
											String [] words=wordSearch.split(" ");
											boolean prs=true;
											for (String word: words){
												if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
															prs=prs && true;}
												else {prs=false;}}
											if (prs){
												nsubList.addAlim(al);
											}					

										}
										else{
											nsubList.addAlim(al);
										}
									}

								}

								else{
									if (!wordSearch.equals("")){
										String [] words=wordSearch.split(" ");
										boolean prs=true;
										for (String word: words){
											if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
														prs=prs && true;}
											else {prs=false;}}
										if (prs){
											nsubList.addAlim(al);
										}					

									}
									else{
										nsubList.addAlim(al);
									}
								}
							}
						}
					}else{
						if (!especeStr.equals("Tous les aliments")){

							if (al.getEspece()==Espece.getEnumFromString(especeStr).getCategorie()){
								if (!marqueStr.equals("Toutes Marques")){
									if (al.getMarque().equals(marqueStr)){
										if (!wordSearch.equals("")){
											String [] words=wordSearch.split(" ");
											boolean prs=true;
											for (String word: words){
												if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
														prs=prs && true;}
												else {prs=false;}}
											if (prs){
												nsubList.addAlim(al);
											}					

										}
										else{
											nsubList.addAlim(al);
										}
									}

								}

								else{
									if (!wordSearch.equals("")){
										String [] words=wordSearch.split(" ");
										boolean prs=true;
										for (String word: words){
											if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
														prs=prs && true;}
											else {prs=false;}}
										if (prs){
											nsubList.addAlim(al);
										}					

									}
									else{
										nsubList.addAlim(al);
									}
								}

							}


						}else{
							if (!marqueStr.equals("Toutes Marques")){
								if (al.getMarque().equals(marqueStr)){
									if (!wordSearch.equals("")){
										String [] words=wordSearch.split(" ");
										boolean prs=true;
										for (String word: words){
											if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
														prs=prs && true;}
											else {prs=false;}}
										if (prs){
											nsubList.addAlim(al);
										}					

									}
									else{
										nsubList.addAlim(al);
									}
								}

							}

							else{
								if (!wordSearch.equals("")){
									String [] words=wordSearch.split(" ");
									boolean prs=true;
									for (String word: words){
										if (((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)&&(GroupAlim.isPresent(al.getGroup()))){
												prs=prs && true;}
										else {prs=false;}}
									if (prs){
										nsubList.addAlim(al);
									}					

								}
								else{
									nsubList.addAlim(al);
								}
							}
						}
					}
				}
			}
			break;
		}


	
		return nsubList;

	}
	@Override
	public void addListener(InvalidationListener listener) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeListener(InvalidationListener listener) {
		// TODO Auto-generated method stub
		
	}
}
