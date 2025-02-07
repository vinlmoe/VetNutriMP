package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;

import Enumerise.AAEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import application.DataConnector;

public class DataPack {
	public final static char SEPARATOR = ';';

	private File file;
	private List<String> lines;
	private List<String[] > data;

	public DataPack() {
	}

	public DataPack(File file) {
		this.file = file;

		// Init
		init();
	}

	private void init() {
		lines = tempCSVReader.readFile(file);

		data = new ArrayList<String[] >(lines.size());
		String sep = new Character(SEPARATOR).toString();
		for (String line : lines) {
			String[] oneData = line.split(sep);
			data.add(oneData);
		}
	}
	public List<String[]> getData() {
		return data;
	}
	/*    private static void MAJ(DataAccess da){

			listAlimPet lal=new listAlimPet();
				lal=da.readAlimPet(ListeData.HILLS);
			for(int i=0; i<lal.size(); i++){
				lal.getAlim(i).maj();
			}
			try {
				da.writeAliment(lal, ListeData.HILLS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	/*	private static void createFilePetRC(){
			String FILE_NAME = "src/alimRChumide.csv";
			file = tempCSVReader.getResource(FILE_NAME);
	    final DataPack csvFile = new DataPack(file);
	       final List<String[] > data = csvFile.getData();
	       listAlimPet lal=new listAlimPet();
	       DataAccess da=new DataAccess();
		//	lal=da.readAlimPet(ListeData.HILLS);
	for (int i=1;   i<15;i++){
	AlimentPet al=new AlimentPet();
	al.setNom(data.get(0)[i]);
	al.setMarque("Royal Canin");
	al.setGamme("Di??t??tique");
	System.out.println(i);

	if (!data.get(42)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(42)[i]));
	}

	if(i<6){
		if (!data.get(43)[i].equals("")){
			al.addIndicat(Integer.parseInt(data.get(43)[i]));
		}
		if (!data.get(44)[i].equals("")){
			al.addIndicat(Integer.parseInt(data.get(44)[i]));
		}
	if (!data.get(45)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(45)[i]));
	}
	if (i<5){
	if (!data.get(46)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(46)[i]));
	}
	if (!data.get(47)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(47)[i]));
	}
	if (!data.get(48)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(48)[i]));
	}
	if (!data.get(49)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(49)[i]));
	}
	if (!data.get(50)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(50)[i]));
	}
	if (!data.get(51)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(51)[i]));
	}

	}
	}
	al.setScanCode(0);
	for  (int j=0;   j<45;j++){
	System.out.println(j+" "+data.get(j)[2]);}


	al.setEspece(Espece.CHIEN.getCategorie());

	al.setProteine(Float.parseFloat(data.get(3)[i]));
	al.setProteinep(true);
	al.setGraisse(Float.parseFloat(data.get(4)[i]));
	al.setLipidep(true);
	al.setENA(Float.parseFloat(data.get(6)[i]));

	al.setAmidon(Float.parseFloat(data.get(6)[i])-Float.parseFloat(data.get(5)[i]));
	al.setAmidonp(true);
	al.setCellulose(Float.parseFloat(data.get(8)[i]));
	al.setCellulosep(true);
	al.setHumidite(Float.parseFloat(data.get(2)[i]));
	al.setHumiditep(true);
	al.setEPADHA(Float.parseFloat(data.get(12)[i]));
	al.setEPADHAp(true);
	al.setCa(Float.parseFloat(data.get(13)[i]));
	al.setCap(true);
	al.setP(Float.parseFloat(data.get(14)[i]));
	al.setPp(true);
	al.setNa(Float.parseFloat(data.get(16)[i]));
	al.setNap(true);
	al.setK(Float.parseFloat(data.get(17)[i]));
	al.setKp(true);
	al.setMg(Float.parseFloat(data.get(15)[i]));
	al.setMgp(true);
	al.setO3(Float.parseFloat(data.get(40)[i]));
	al.setOm3p(true);
	al.setO6(Float.parseFloat(data.get(41)[i]));
	al.setOm6p(true);
	al.setFer(Float.parseFloat(data.get(19)[i]));
	al.setFep(true);
	al.setCuivre(Float.parseFloat(data.get(20)[i]));
	al.setCup(true);
	al.setZinc(Float.parseFloat(data.get(21)[i]));
	al.setZnp(true);
	al.setSelenium(Float.parseFloat(data.get(22)[i]));
	al.setSep(true);

	al.setVitA(Float.parseFloat(data.get(23)[i])/10F);
	al.setVitAp(true);
	al.setVitD(Float.parseFloat(data.get(24)[i])/10F);
	al.setVitDp(true);
	al.setVitE(Float.parseFloat(data.get(25)[i])/10F);
	al.setVitEp(true);
	al.setVitC(Float.parseFloat(data.get(26)[i])/10F);
	al.setVitCp(true);
	al.setVitB1(Float.parseFloat(data.get(27)[i])/10F);
	al.setVitB1p(true);
	al.setVitB2(Float.parseFloat(data.get(28)[i])/10F);
	al.setVitB2p(true);
	al.setVitB6(Float.parseFloat(data.get(29)[i])/10F);
	al.setVitB6p(true);
	al.setVitB5(Float.parseFloat(data.get(31)[i])/10F);
	al.setVitB5p(true);
	al.setPP(Float.parseFloat(data.get(30)[i])/10F);
	al.setVitPPp(true);
	al.setVitB12(Float.parseFloat(data.get(32)[i])*100F);
	al.setVitB12p(true);
	al.setBiotine(Float.parseFloat(data.get(34)[i])/10F);
	al.setBiotinep(true);
	al.setFol(Float.parseFloat(data.get(33)[i])/10F);
	al.setFolatep(true);


	al.setCendre(Float.parseFloat(data.get(9)[i]));
	al.setIngredients((data.get(1)[i]));
	lal.addAlim(al);

	}
	System.out.println(lal.getAlim(1).getNom());
	try {
	dat.writeAliment(lal, ListeData.HILLS);
	} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	}
		}

		private static void createFilePetHills(){
			String FILE_NAME = "src/hills.csv";
			file = tempCSVReader.getResource(FILE_NAME);
	    final DataPack csvFile = new DataPack(file);
	       final List<String[] > data = csvFile.getData();
	       listAlimPet lal=new listAlimPet();
	       DataAccess da=new DataAccess();
		//	lal=da.readAlimPet(ListeData.HILLS);
	for (int i=2;   i<121;i++){
	AlimentPet al=new AlimentPet();
	al.setMarque("Hill's");
	if (data.get(0)[i].indexOf("Prescription Diet")!=-1){
		al.setGamme("Prescription Diet");

	}else if(data.get(0)[i].indexOf("VetEssentials")!=-1){
		al.setGamme("VetEssentials");

	}
	else if(data.get(0)[i].indexOf("Science Plan")!=-1){
		al.setGamme("Science Plan");

	}




	/*for  (int j=0;   j<42;j++){
	System.out.println(j+" "+data.get(j)[0]);}*/

	/**al.setNom(data.get(0)[i].replaceAll(al.getGamme(), ""));
	if (data.get(0)[i].toLowerCase().indexOf("canine")!=-1 ||data.get(0)[i].toLowerCase().indexOf("dog")!=-1 
	||data.get(0)[i].toLowerCase().indexOf("puppy")!=-1 || data.get(0)[i].toLowerCase().indexOf("chien")!=-1){
		al.setEspece(Espece.CHIEN.getCategorie());
	}else{
	al.setEspece(Espece.CHAT.getCategorie());}
	System.out.println(i);
	if (!data.get(1)[i].equals("")){
	al.setProteine(Float.parseFloat(data.get(1)[i]));
	al.setProteinep(true);}
	else al.setProteinep(false);
	if (!data.get(2)[i].equals("")){
	al.setGraisse(Float.parseFloat(data.get(2)[i]));
	}
	if (!data.get(3)[i].equals("")){
	al.setENA(Float.parseFloat(data.get(3)[i]));
	al.setGlucidep(true);}
	else al.setGlucidep(false);
	if (!data.get(4)[i].equals("")){
	al.setCellulose(Float.parseFloat(data.get(4)[i]));
	al.setCellulosep(true);}
	else al.setCellulosep(false);
	if (!data.get(5)[i].equals("")){
	al.setCendre(Float.parseFloat(data.get(5)[i]));
	al.setCendrep(true);}
	else al.setCendrep(false);
	if (!data.get(6)[i].equals("")){
	al.setHumidite(Float.parseFloat(data.get(6)[i]));
	al.setHumiditep(true);}
	else al.setHumiditep(false);
	if (!data.get(7)[i].equals("")){
	al.setCa(Float.parseFloat(data.get(7)[i]));
	al.setCap(true);}
	else al.setCap(false);
	if (!data.get(8)[i].equals("")){
	al.setP(Float.parseFloat(data.get(8)[i]));
	al.setPp(true);}
	else al.setPp(false);
	if (!data.get(9)[i].equals("")){
	al.setNa(Float.parseFloat(data.get(9)[i]));
	al.setNap(true);}
	else al.setNap(false);
	if (!data.get(10)[i].equals("")){
	al.setK(Float.parseFloat(data.get(10)[i]));
	al.setKp(true);}
	else al.setKp(false);
	if (!data.get(11)[i].equals("")){
	al.setMg(Float.parseFloat(data.get(11)[i]));
	al.setMgp(true);}
	else al.setMgp(false);
	if (!data.get(16)[i].equals("")){
	al.setO3(Float.parseFloat(data.get(16)[i]));
	al.setOm3p(true);}
	else al.setOm3p(false);
	if (!data.get(17)[i].equals("")){
	al.setO6(Float.parseFloat(data.get(17)[i]));
	al.setOm6p(true);}
	else al.setOm6p(false);
	if (!data.get(18)[i].equals("")){
	al.setFer(Float.parseFloat(data.get(18)[i]));
	al.setFep(true);}
	else al.setFep(false);
	if (!data.get(19)[i].equals("")){
	al.setZinc(Float.parseFloat(data.get(19)[i]));
	al.setZnp(true);}
	else al.setZnp(false);
	if (!data.get(20)[i].equals("")){
	al.setCuivre(Float.parseFloat(data.get(20)[i]));
	al.setCup(true);}
	else al.setCup(false);
	if (!data.get(26)[i].equals("")){
	al.setVitA(Float.parseFloat(data.get(26)[i])/10);
	al.setVitAp(true);}
	else al.setVitAp(false);
	if (!data.get(27)[i].equals("")){
	al.setVitD(Float.parseFloat(data.get(27)[i])/10);
	al.setVitDp(true);}
	else al.setVitDp(false);
	if (!data.get(28)[i].equals("")){
	al.setVitE(Float.parseFloat(data.get(28)[i])/10);
	al.setVitEp(true);}
	else al.setVitEp(false);
	if (!data.get(29)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(29)[i]));
	}
	if (!data.get(30)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(30)[i]));
	}
	if (!data.get(31)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(31)[i]));
	}
	if (!data.get(32)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(32)[i]));
	}
	if (!data.get(33)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(33)[i]));
	}
	if (!data.get(34)[i].equals("")){
		al.addIndicat(Integer.parseInt(data.get(34)[i]));
	}





	al.setIngredients("");
	lal.addAlim(al);

	}
	System.out.println(lal.getAlim(1).getNom());
	try {
	dat.writeAliment(lal, ListeData.HILLS);
	} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
	}
		}

	 *//*
	public void createFileCiqual(){
		String FILE_NAME = "src/ciqual1.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=new listAlim ();
		for (int i=2;   i<data.size();i++){
			AlimentUnif al=new AlimentUnif();
			System.out.println(data.get(i)[4]);
			al.setTypeAliment(TypeAlim.CIQUAL);
			al.setGroup(data.get(i)[1]);

			al.setNom(data.get(i)[3]);


			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam(Float.parseFloat(data.get(i)[elem.getCoef()+22]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}
			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin(Float.parseFloat(data.get(i)[elem.getCoef()+59]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}

			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			al.removeNutrientBase(NutrientBase.CENDRE);

			lal.addAlim(al);}
		System.out.println(lal.getAlim(1).getNom());
		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void createFileComp(){
		String FILE_NAME = "src/comrevimp.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=dat.readAlim();
		for (int i=3;   i<data.size();i++){
			AlimentUnif al=new AlimentUnif(data.get(i)[4]);
			System.out.println(data.get(i)[4]);
			al.setTypeAliment(TypeAlim.COMPLEMENTAIRE);
			if(data.get(i)[0].trim().equals("1")){al.setEspece(Espece.CHIEN.getCategorie());}
			else{al.setEspece(Espece.CHAT.getCategorie());}

			al.setIngredients(data.get(i)[3]);
			al.setMarque(data.get(i)[1]);
			al.setNom(data.get(i)[2]);


			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam((Float.parseFloat(data.get(i)[elem.getCoef()+22]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}
			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin((Float.parseFloat(data.get(i)[elem.getCoef()+59]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}
			for(AAEnum elem:AAEnum.values()){
				try{
					al.setNutrientAcideAmine(100F*
							Float.parseFloat(data.get(i)[elem.getCoef()+65])
							/al.getNutrientBase(NutrientBase.PROTEINE)
							, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientAcideAmine(elem);
				}
			}
			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);

				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[87])/10000F, NutrientOther.TAURINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.TAURINE);
			}

			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[90])/10F, NutrientOther.MOS);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.MOS);
			}
			lal.addAlim(al);

		}
		System.out.println(lal.getAlim(1).getNom());
		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFileHills(){
		String FILE_NAME = "src/hillrevimp.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=dat.readAlim();
		for (int i=3;   i<data.size();i++){
			AlimentUnif al=new AlimentUnif();
			System.out.println(data.get(i)[3]);
			al.setTypeAliment(TypeAlim.COMPLET);
			if(data.get(i)[0].trim().equals("1")){al.setEspece(Espece.CHIEN.getCategorie());}
			else{al.setEspece(Espece.CHAT.getCategorie());}

			al.setGamme(data.get(i)[1]);
			al.setMarque("Hill's");
			al.setNom(data.get(i)[3]);


			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam((Float.parseFloat(data.get(i)[elem.getCoef()+22]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}
			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin((Float.parseFloat(data.get(i)[elem.getCoef()+59]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}

			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);

				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[87])/10000F, NutrientOther.TAURINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.TAURINE);
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[88])/10000F, NutrientOther.CARNITINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.CARNITINE);
			}

			lal.addAlim(al);

		}
		System.out.println(lal.getAlim(1).getNom());
		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createFileOther(){
		String FILE_NAME = "src/Otherrev-corc.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=dat.readAlim();
		for (int i=3;   i<data.size();i++){

			AlimentUnif al=new AlimentUnif(data.get(i)[6]);
			System.out.println(data.get(i)[2]);
			al.setTypeAliment(TypeAlim.COMPLET);
			if(data.get(i)[0].trim().equals("1")){al.setEspece(Espece.CHIEN.getCategorie());}
			else{al.setEspece(Espece.CHAT.getCategorie());}

			al.setGamme(data.get(i)[1]);
			al.setMarque(data.get(i)[4]);
			al.setNom(data.get(i)[2]);
			al.setIngredients(data.get(i)[3]);

			if (!data.get(i)[5].isEmpty()){	String [] indicats=data.get(i)[5].split("_");

			for (String indicat: indicats){
				al.addIndicat(AlimIndic.IntToGroup(Integer.parseInt(indicat)));}}

			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam((Float.parseFloat(data.get(i)[elem.getCoef()+22]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}

			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin((Float.parseFloat(data.get(i)[elem.getCoef()+59]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}

			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);

				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[87]), NutrientOther.TAURINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.TAURINE);
			}
			try{
				al.setNutrientOther((Float.parseFloat(data.get(i)[88])/10F), NutrientOther.CARNITINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.CARNITINE);
			}
			lal.addAlim(al);

		}

		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


*/	public static void createFileEvolve(String fileName, Vet vet){
		FileInputStream file =null;

		String sheetName = "aliments complets";
		try{	
	 file = new FileInputStream(fileName);
			Workbook workbook = WorkbookFactory.create(file);
			final Sheet sheet = workbook.getSheet(sheetName);
			int top = sheet.getFirstRowNum();
			int bottom = sheet.getLastRowNum();
			Row line = sheet.getRow(top);
			int start = line.getFirstCellNum();
			int end = line.getLastCellNum();    
			int length = end - start;
			while(length == 0)
			{
				top++;
				line = sheet.getRow(top);
				start = line.getFirstCellNum();
				end = line.getLastCellNum();    
				length = end - start;
			}
			int hight = bottom - top-1;
			String [] header =  new String[length];
			String [] coeff =  new String[length];
			String[][]body = new String[hight][length];

			for (int i = 0; i < length; i++)
			{
				header[i] = line.getCell(start + i).getStringCellValue();    
			}
			for (int i = 0; i < length; i++)
			{
				Row lineCoef = sheet.getRow(top+1);
				if(lineCoef.getCell(start + i).getCellType()== CellType.NUMERIC) {
					coeff[i] =  NumberToTextConverter.toText(lineCoef.getCell(start + i).getNumericCellValue()); 
				}else{
					coeff[i] = lineCoef.getCell(start + i).getStringCellValue();    }
			}
			for (int index = 0; index < hight; index++) 
			{
				line = sheet.getRow(index + top + 2);
				for (int i = 0; i < length; i++)
				{

					if ( (line.getCell(start + i)!=null )){
						Cell cellule = line.getCell(start + i);

						if(cellule.getCellType() == CellType.NUMERIC) {
							body[index][i] = NumberToTextConverter.toText(cellule.getNumericCellValue());
						}else {
						
							body[index][i] = cellule.getStringCellValue();
							}

					} else {  body[index][i]="";}
				}
			}
			workbook.close();


			modelImportTable mod=new modelImportTable();
			mod.importAdress(header, coeff);
			AlimentEv al;
			ArrayList<AlimentEv>lal=new ArrayList<AlimentEv>();
			if (mod.isPresent("NAME")){
				for (int i=0; i<hight; i++){



					al=mod.createAlim(body[i], vet);
System.out.println(al.DataB);
					lal.add(al);
				}

				DataConnector.UpdateListAlim(lal);

			}




		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if( file!=null){
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
		}
	}

/*
	public void createFileRC(){
		String FILE_NAME = "src/RCrev-corb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=dat.readAlim();
		for (int i=3;   i<data.size();i++){
			AlimentUnif al=new AlimentUnif();

			al.setTypeAliment(Type.COMPLET);
			if(data.get(i)[0].trim().equals("1")){al.setEspece(Espece.CHIEN.getCategorie());}
			else{al.setEspece(Espece.CHAT.getCategorie());}

			al.setGamme(data.get(i)[1]);
			al.setMarque("Royal Canin");
			al.setNom(data.get(i)[2]);
			al.setIngredients(data.get(i)[3]);

			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam((Float.parseFloat(data.get(i)[elem.getCoef()+22]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}
			try{
				al.setNutrientVitam((Float.parseFloat(data.get(i)[NutrientVitam.VITB12.getCoef()+22]))*100F, NutrientVitam.VITB12);


			}catch(NumberFormatException ex){
				al.removeNutrientVitam(NutrientVitam.VITB12);
			}
			try{
				al.setNutrientVitam((Float.parseFloat(data.get(i)[NutrientVitam.VITB9.getCoef()+22]))*100F, NutrientVitam.VITB9);


			}catch(NumberFormatException ex){
				al.removeNutrientVitam(NutrientVitam.VITB9);
			}
			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin((Float.parseFloat(data.get(i)[elem.getCoef()+59]))/10F, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}
			try{
				al.setNutrientMin((Float.parseFloat(data.get(i)[NutrientMin.SE.getCoef()+59]))*100F, NutrientMin.SE);


			}catch(NumberFormatException ex){
				al.removeNutrientMin(NutrientMin.SE);
			}
			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);

				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[87]), NutrientOther.TAURINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.TAURINE);
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(i)[88])/10000F, NutrientOther.CARNITINE);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(NutrientOther.CARNITINE);
			}
			lal.addAlim(al);

		}

		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}/*
	public void createFileFcen(){
		String FILE_NAME = "src/fcen-exp-pre-imp.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		final DataPack csvFile = new DataPack(file);
		final List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		listAlim lal=dat.readAlim();
		for (int i=2;   i<data.size();i++){
			AlimentUnif al=new AlimentUnif();

			al.setTypeAliment(TypeAlim.USDA);


			al.setGroup(data.get(i)[1]);

			al.setNom(data.get(i)[2]);


			for(NutrientBase elem:NutrientBase.values()){
				try{
					al.setNutrientBase(Float.parseFloat(data.get(i)[elem.getCoef()+7]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientBase(elem);
				}
			}
			for(NutrientVitam elem:NutrientVitam.values()){
				try{
					al.setNutrientVitam((Float.parseFloat(data.get(i)[elem.getCoef()+22])), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientVitam(elem);
				}
			}
			for(NutrientMacro elem:NutrientMacro.values()){
				try{
					al.setNutrientMacro(Float.parseFloat(data.get(i)[elem.getCoef()+16]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMacro(elem);
				}
			}



			for(NutrientMin elem:NutrientMin.values()){
				try{
					al.setNutrientMin((Float.parseFloat(data.get(i)[elem.getCoef()+59])), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientMin(elem);
				}
			}

			for(NutrientLipid elem:NutrientLipid.values()){
				try{
					al.setNutrientLipid(Float.parseFloat(data.get(i)[elem.getCoef()+38]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientLipid(elem);
				}
			}
			for(AAEnum elem:AAEnum.values()){
				try{
					al.setNutrientAcideAmine(100F*
							Float.parseFloat(data.get(i)[elem.getCoef()+65])
							/al.getNutrientBase(NutrientBase.PROTEINE)
							, elem);


				}catch(NumberFormatException ex){
					al.removeNutrientAcideAmine(elem);
				}
			}
			for(NutrientOther elem:NutrientOther.values()){
				try{
					al.setNutrientOther(Float.parseFloat(data.get(i)[elem.getCoef()+87]), elem);


				}catch(NumberFormatException ex){
					al.removeNutrientOther(elem);
				}
			}

			lal.addAlim(al);

		}

		try {
			dat.writeAliment(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
	public void createFileRef(){
		String FILE_NAME = "src/chien-phys-9kgb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		DataPack csvFile = new DataPack(file);
		List<String[] > data = csvFile.getData();


		DataAccess dat=new DataAccess();
		Reference al=new Reference ();


		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.ADULTE);

		al.setNom("Adulte <9kg");
		al=setRef(al, data);

		ListReference lal= new ListReference();
		lal.addReference(al);

		FILE_NAME = "src/chien-phys-30kgb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();

		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.ADULTE);


		al.setNom("Adulte >25kg");
		al=setRef(al, data);



		lal.addReference(al);
		FILE_NAME = "src/chien-phys-15kgb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.ADULTE);


		al.setNom("Adulte 10-25kg");
		al=setRef(al, data);



		lal.addReference(al);


		//hospit


		FILE_NAME = "src/chien-phys-15kgb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.HOSPIT);


		al.setNom("Hospitalisation");
		al=setRef(al, data);



		lal.addReference(al);

		FILE_NAME = "src/chien-gesb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.GESTATION);


		al.setNom("Gestation");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);
		FILE_NAME = "src/chien-gesb.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.LACTATION);


		al.setNom("Lactation");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);
		FILE_NAME = "src/chien-crois-presev.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHIEN);
		al.setsPhysio(StadePhysio.CROISSANCE);


		al.setNom("Avant sevrage");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);
		FILE_NAME = "src/chien-crois-b.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();
		al.setsPhysio(StadePhysio.CROISSANCE);


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHIEN);


		al.setNom("Croissance ");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));




		lal.addReference(al);

		FILE_NAME = "src/chien-crois-b-60.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();

		al.setsPhysio(StadePhysio.CROISSANCE);

		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHIEN);


		al.setNom("Croissance grandes races <7 mois");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));




		lal.addReference(al);


		FILE_NAME = "src/chat-adulte.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHAT);
		al.setsPhysio(StadePhysio.ADULTE);


		al.setNom("Adulte");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);

		FILE_NAME = "src/chat-adulte.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();


		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHAT);
		al.setsPhysio(StadePhysio.HOSPIT);


		al.setNom("Hospitalisation");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);

		FILE_NAME = "src/kitten.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();

		al.setsPhysio(StadePhysio.CROISSANCE);

		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHAT);


		al.setNom("Croissance");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);
		FILE_NAME = "src/chat-ges.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();

		al.setsPhysio(StadePhysio.GESTATION);

		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHAT);


		al.setNom("Gestation");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);

		FILE_NAME = "src/chat-ges.csv";
		file = tempCSVReader.getResource(FILE_NAME);
		csvFile = new DataPack(file);
		data = csvFile.getData();



		al=new Reference ();

		al.setsPhysio(StadePhysio.LACTATION);

		System.out.println(data.get(5)[59]);
		al.setEspece(Espece.CHAT);


		al.setNom("Lactation");
		al=setRef(al, data);


		System.out.println(al.getNutrientBib(NutrientBase.LIPIDE, Reflevel.OPTIMAX));
		System.out.println((String)(data.get(8)[NutrientBase.LIPIDE.getCoef()+7]));

		lal.addReference(al);

		try {
			dat.writeRef(lal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Reference setRef(Reference al, List<String[]> data){
		for(NutrientBase elem:NutrientBase.values()){
			if (elem!=NutrientBase.FIBRETOT) {
				try{
					al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+7]), elem, Reflevel.MIN);
					al.setNutrientBib((String)(data.get(6)[elem.getCoef()+7]), elem, Reflevel.MIN);

				}catch(NumberFormatException ex){
					al.removeNutrient(elem, Reflevel.MIN);
				}
				try{
					al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+7]), elem, Reflevel.OPTIMIN);
					al.setNutrientBib((String)(data.get(7)[elem.getCoef()+7]), elem, Reflevel.OPTIMIN);

				}catch(NumberFormatException ex){
					al.removeNutrient(elem, Reflevel.OPTIMIN);
				}
				try{
					al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+7]), elem, Reflevel.OPTIMAX);
					al.setNutrientBib((String)(data.get(8)[elem.getCoef()+7]), elem, Reflevel.OPTIMAX);

				}catch(NumberFormatException ex){
					al.removeNutrient(elem, Reflevel.OPTIMAX);
				}
				try{
					al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+7]), elem, Reflevel.MAX);
					al.setNutrientBib((String)(data.get(9)[elem.getCoef()+7]), elem, Reflevel.MAX);

				}catch(NumberFormatException ex){
					al.removeNutrient(elem, Reflevel.MAX);
				}}
		}
		for(NutrientVitam elem:NutrientVitam.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+22]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+22]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+22]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+22]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+22]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+22]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+22]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+22]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}

		}
		for(NutrientMacro elem:NutrientMacro.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+16]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+16]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+16]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+16]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+16]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+16]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+16]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+16]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}

		}


		for(NutrientMin elem:NutrientMin.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+59]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+59]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+59]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+59]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+59]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+59]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{

				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+59]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+59]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}

		}

		for(NutrientLipid elem:NutrientLipid.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+38]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+38]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+38]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+38]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+38]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+38]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+38]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+38]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}}
		for(NutrientAnalysis elem:NutrientAnalysis.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+91]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+91]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+91]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+91]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+91]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+91]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+91]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+91]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}
		}
		for(AAEnum elem:AAEnum.values()){
			try{
				al.setNutrient(Float.parseFloat(data.get(2)[elem.getCoef()+65]), elem, Reflevel.MIN);
				al.setNutrientBib((String)(data.get(6)[elem.getCoef()+65]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(3)[elem.getCoef()+65]), elem, Reflevel.OPTIMIN);
				al.setNutrientBib((String)(data.get(7)[elem.getCoef()+65]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(4)[elem.getCoef()+65]), elem, Reflevel.OPTIMAX);
				al.setNutrientBib((String)(data.get(8)[elem.getCoef()+65]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrient(Float.parseFloat(data.get(5)[elem.getCoef()+65]), elem, Reflevel.MAX);
				al.setNutrientBib((String)(data.get(9)[elem.getCoef()+65]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrient(elem, Reflevel.MAX);
			}}
		/*for(NutrientOther elem:NutrientOther.values()){
			try{
				al.setNutrientOther(Float.parseFloat(data.get(2)[elem.getCoef()+87]), elem, Reflevel.MIN);
				al.setNutrientOtherBib((String)(data.get(6)[elem.getCoef()+87]), elem, Reflevel.MIN);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(elem, Reflevel.MIN);
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(3)[elem.getCoef()+87]), elem, Reflevel.OPTIMIN);
				al.setNutrientOtherBib((String)(data.get(7)[elem.getCoef()+87]), elem, Reflevel.OPTIMIN);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(elem, Reflevel.OPTIMIN);
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(4)[elem.getCoef()+87]), elem, Reflevel.OPTIMAX);
				al.setNutrientOtherBib((String)(data.get(8)[elem.getCoef()+87]), elem, Reflevel.OPTIMAX);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(elem, Reflevel.OPTIMAX);
			}
			try{
				al.setNutrientOther(Float.parseFloat(data.get(5)[elem.getCoef()+87]), elem, Reflevel.MAX);
				al.setNutrientOtherBib((String)(data.get(9)[elem.getCoef()+87]), elem, Reflevel.MAX);

			}catch(NumberFormatException ex){
				al.removeNutrientOther(elem, Reflevel.MAX);
			}}*/
	/*	return al;
	}*/
	/*	public void createFileCiqual(){
			File file;
			DataAccess dat=new DataAccess();
			String FILE_NAME = "src/ciqual.csv";
			file = tempCSVReader.getResource(FILE_NAME);
			final DataPack csvFile = new DataPack(file);
			final List<String[] > data = csvFile.getData();
			listAlim lal=new listAlim ();
			for (int i=1;   i<data.size();i++){
				AlimentUnif al=new AlimentUnif();
				al.setGroup(GroupAlim.StringToGroup(data.get(i)[1]));

				al.setNom(data.get(i)[3]);
				try{
					al.setHumidite(Float.parseFloat(data.get(i)[8]));
				}
				catch(NumberFormatException ex){
					al.setHumiditep(false);
					al.setHumidite(0.0F);
				}
				try{
					al.setProteine(Float.parseFloat(data.get(i)[9]));
				}
				catch(NumberFormatException ex){
					al.setProteine(0);
					al.setProteinep(false);
				}
				try{
					al.setGlucide(Float.parseFloat(data.get(i)[11]));
				}
				catch(NumberFormatException ex){
					al.setGlucidep(false);
					al.setGlucide(0);
				}
				try{
					al.setLipide(Float.parseFloat(data.get(i)[12]));
				}
				catch(NumberFormatException ex){
					al.setLipidep(false);
					al.setLipide(0);
				}
				try{
					al.setSucre(Float.parseFloat(data.get(i)[13]));
				}
				catch(NumberFormatException ex){
					al.setSucrep(false);
					al.setSucre(0);
				}
				try{
					al.setAmidon(Float.parseFloat(data.get(i)[14]));
				}
				catch(NumberFormatException ex){
					al.setAmidonp(false);
					al.setAmidon(0);
				}
				try{
					al.setCellulose(Float.parseFloat(data.get(i)[15]));
				}
				catch(NumberFormatException ex){
					al.setCellulosep(false);
					al.setCellulose(0);
				}
				try{
					al.setCendre(Float.parseFloat(data.get(i)[17]));
				}
				catch(NumberFormatException ex){
					al.setCendrep(false);
					al.setCendre(0);
				}
				try{
					al.setGsature(Float.parseFloat(data.get(i)[20]));
				}
				catch(NumberFormatException ex){
					al.setGsaturep(false);
					al.setGsature(0);
				}
				try{
					al.setGmonoinsat(Float.parseFloat(data.get(i)[21]));
				}
				catch(NumberFormatException ex){
					al.setGmonoinsatp(false);
					al.setGmonoinsat(0);
				}
				try{
					al.setGpolyinsat(Float.parseFloat(data.get(i)[22]));
				}
				catch(NumberFormatException ex){
					al.setGpolyinsatp(false);
					al.setGpolyinsat(0);
				}
				try{
					al.setAg40(Float.parseFloat(data.get(i)[23]));
				}
				catch(NumberFormatException ex){
					al.setAg40p(false);
					al.setAg40(0);
				}
				try{
					al.setAg60(Float.parseFloat(data.get(i)[24]));
				}
				catch(NumberFormatException ex){
					al.setAg60p(false);
					al.setAg60(0);
				}
				try{
					al.setAg80(Float.parseFloat(data.get(i)[25]));
				}
				catch(NumberFormatException ex){
					al.setAg80p(false);
					al.setAg80(0);
				}
				try{
					al.setAg100(Float.parseFloat(data.get(i)[26]));
				}
				catch(NumberFormatException ex){
					al.setAg100p(false);
					al.setAg100(0);
				}
				try{
					al.setAg120(Float.parseFloat(data.get(i)[27]));
				}
				catch(NumberFormatException ex){
					al.setAg120p(false);
					al.setAg120(0);
				}
				try{
					al.setAg140(Float.parseFloat(data.get(i)[28]));
				}
				catch(NumberFormatException ex){
					al.setAg140p(false);
					al.setAg140(0);
				}
				try{
					al.setAg160(Float.parseFloat(data.get(i)[29]));
				}
				catch(NumberFormatException ex){
					al.setAg160p(false);
					al.setAg160(0);
				}
				try{
					al.setAg180(Float.parseFloat(data.get(i)[30]));
				}
				catch(NumberFormatException ex){
					al.setAg180p(false);
					al.setAg180(0);
				}
				try{
					al.setAg181(Float.parseFloat(data.get(i)[31]));
				}
				catch(NumberFormatException ex){
					al.setAg181p(false);
					al.setAg181(0);
				}
				try{
					al.setAg182(Float.parseFloat(data.get(i)[32]));
				}
				catch(NumberFormatException ex){
					al.setAg182p(false);
					al.setAg182(0);
				}
				try{
					al.setAg183(Float.parseFloat(data.get(i)[33]));
				}
				catch(NumberFormatException ex){
					al.setAg183p(false);
					al.setAg183(0);
				}
				try{
					al.setAg204(Float.parseFloat(data.get(i)[34]));
				}
				catch(NumberFormatException ex){
					al.setAg204p(false);
					al.setAg204(0);
				}
				try{
					al.setAg205(Float.parseFloat(data.get(i)[35]));
				}
				catch(NumberFormatException ex){
					al.setAg205p(false);
					al.setAg205(0);
				}
				try{
					al.setAg226(Float.parseFloat(data.get(i)[36]));
				}
				catch(NumberFormatException ex){
					al.setAg226p(false);
					al.setAg226(0);
				}
				try{
					al.setCholesterol(Float.parseFloat(data.get(i)[37]));
				}
				catch(NumberFormatException ex){
					al.setCholesterolp(false);
					al.setCholesterol(0);
				}
				try{
					al.setCa(Float.parseFloat(data.get(i)[39]));
				}
				catch(NumberFormatException ex){
					al.setCap(false);
					al.setCa(0);
				}
				try{
					al.setCl(Float.parseFloat(data.get(i)[40]));
				}
				catch(NumberFormatException ex){
					al.setClp(false);
					al.setCl(0);
				}
				try{
					al.setCu(Float.parseFloat(data.get(i)[41]));
				}
				catch(NumberFormatException ex){
					al.setCup(false);
					al.setCu(0);
				}
				try{
					al.setFe(Float.parseFloat(data.get(i)[42]));
				}
				catch(NumberFormatException ex){
					al.setFep(false);
					al.setFe(0);
				}
				try{
					al.setI(Float.parseFloat(data.get(i)[43]));
				}
				catch(NumberFormatException ex){
					al.setIp(false);
					al.setI(0);
				}
				try{
					al.setMg(Float.parseFloat(data.get(i)[44]));
				}
				catch(NumberFormatException ex){
					al.setMgp(false);
					al.setMg(0);
				}
				try{
					al.setMn(Float.parseFloat(data.get(i)[44]));
				}
				catch(NumberFormatException ex){
					al.setMnp(false);
					al.setMn(0);
				}
				try{
					al.setP(Float.parseFloat(data.get(i)[46]));
				}
				catch(NumberFormatException ex){
					al.setPp(false);
					al.setP(0);
				}
				try{
					al.setK(Float.parseFloat(data.get(i)[47]));
				}
				catch(NumberFormatException ex){
					al.setKp(false);
					al.setK(0);
				}
				try{
					al.setSe(Float.parseFloat(data.get(i)[48]));
				}
				catch(NumberFormatException ex){
					al.setSep(false);
					al.setSe(0);
				}
				try{
					al.setNa(Float.parseFloat(data.get(i)[49]));
				}
				catch(NumberFormatException ex){
					al.setNap(false);
					al.setNa(0);
				}
				try{
					al.setZn(Float.parseFloat(data.get(i)[50]));
				}
				catch(NumberFormatException ex){
					al.setZnp(false);
					al.setZn(0);
				}
				try{
					al.setRetinol(Float.parseFloat(data.get(i)[51]));
				}
				catch(NumberFormatException ex){
					al.setRetinolp(false);
					al.setRetinol(0);
				}
				try{
					al.setBetacar(Float.parseFloat(data.get(i)[52]));
				}
				catch(NumberFormatException ex){
					al.setBetacarp(false);
					al.setBetacar(0);
				}
				try{
					al.setVitD(Float.parseFloat(data.get(i)[53]));
				}
				catch(NumberFormatException ex){
					al.setVitDp(false);
					al.setVitD(0);
				}
				try{
					al.setVitE(Float.parseFloat(data.get(i)[54]));
				}
				catch(NumberFormatException ex){
					al.setVitEp(false);
					al.setVitE(0);
				}
				try{
					al.setVitC(Float.parseFloat(data.get(i)[57]));
				}
				catch(NumberFormatException ex){
					al.setVitCp(false);
					al.setVitC(0);
				}
				try{
					al.setVitB1(Float.parseFloat(data.get(i)[58]));
				}
				catch(NumberFormatException ex){
					al.setVitB1p(false);
					al.setVitB1(0);
				}
				try{
					al.setVitB2(Float.parseFloat(data.get(i)[59]));
				}
				catch(NumberFormatException ex){
					al.setVitB2p(false);
					al.setVitB2(0);
				}
				try{
					al.setVitB3(Float.parseFloat(data.get(i)[60]));
				}
				catch(NumberFormatException ex){
					al.setVitB3p(false);
					al.setVitB3(0);
				}
				try{
					al.setVitB5(Float.parseFloat(data.get(i)[61]));
				}
				catch(NumberFormatException ex){
					al.setVitB5p(false);
					al.setVitB5(0);
				}
				try{
					al.setVitB6(Float.parseFloat(data.get(i)[62]));
				}
				catch(NumberFormatException ex){
					al.setVitB6p(false);
					al.setVitB6(0);
				}
				try{
					al.setVitB12(Float.parseFloat(data.get(i)[63]));
				}
				catch(NumberFormatException ex){
					al.setVitB12p(false);
					al.setVitB12(0);
				}

				lal.addAlim(al);

			}
			System.out.println(lal.getAlim(1).getNom());
			try {
				dat.writeAliment(lal, ListeData.CIQUAL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/


}
