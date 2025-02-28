package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import DataStruct.AlimP;
import DataStruct.AnimP;
import Enumerise.AAEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import application.DataConnector;
import javafx.collections.ObservableList;

public class DataAccess {

	public DataAccess(){

	}

	public static void exportAliment(ObservableList<AlimP> list, File file, Vet vet)throws IOException{
		DataAccess dat= new DataAccess();
		modelImportTable table=new modelImportTable();
		Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

	
		CreationHelper createHelper = workbook.getCreationHelper();

		// Create a Sheet
		Sheet sheet = workbook.createSheet("aliments complets");
		Row headerRow = sheet.createRow(0);
		// Create a Font for styling header cells


		String [] header= getHeader();

		for(int i = 0; i < header.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(header[i]);
		}
		String[] coef =new String[header.length];
		int rowNum = 1;
		Row coefRow= sheet.createRow(1);
		for(int i = 0; i < header.length; i++) {
			Cell cell = coefRow.createCell(i);
			cell.setCellValue(""+1);
			coef[i]=""+1;}

		
		for(int i=0; i<list.size(); i++){
			rowNum++;
			Row alimRow= sheet.createRow(rowNum);
			for(int j=0; j<header.length; j++){
				Cell cell = alimRow.createCell(j);

				cell.setCellValue(table.getData(header[j], list.get(i).getAlim(), vet));
			}

		}
	    FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        

        // Closing the workbook
        workbook.close();




	}
	/*
	public void exportAliment(TypeAlim type)throws IOException{
		DataAccess dat= new DataAccess();
		modelImportTable table=new modelImportTable();
		Workbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

		
		CreationHelper createHelper = workbook.getCreationHelper();

		// Create a Sheet
		Sheet sheet = workbook.createSheet("aliments complets");
		Row headerRow = sheet.createRow(0);
		// Create a Font for styling header cells


		String [] header= getHeader();

		for(int i = 0; i < header.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(header[i]);
		}
		String[] coef =new String[header.length];
		int rowNum = 1;
		Row coefRow= sheet.createRow(1);
		for(int i = 0; i < header.length; i++) {
			Cell cell = coefRow.createCell(i);
			cell.setCellValue(""+1);
			coef[i]=""+1;}
		listAlim lal=this.readAlim();
		lal=lal.keepOnlyByType(type);

		
		for(int i=0; i<lal.size(); i++){
			rowNum++;
			Row alimRow= sheet.createRow(rowNum);
			for(int j=0; j<header.length; j++){
				Cell cell = alimRow.createCell(j);


				cell.setCellValue(table.getData(header[j], lal.getAlim(i)));
			}

		}
	    FileOutputStream fileOut = new FileOutputStream("export-alimentcat.xlsx");
        workbook.write(fileOut);
        fileOut.close();
        

        // Closing the workbook
        workbook.close();




	}*/
	
	public void writeAliment(listAlim d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/aliments.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public void writeAlimentBack(listAlim d) throws IOException{
		String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis()));
		FileOutputStream fos = new FileOutputStream("resources/aliments-"+date+".vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public void writeAlimentsecours(listAlim d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/aliments-secours.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	
	public static void writeAliments(ObservableList<AlimP> d,Vet vet, File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<AlimentEv> al=new ArrayList<AlimentEv>();
		AlimSaver as=new AlimSaver();
		for(AlimP a:d) {
			al.add(a.getAlim());
		}
		as.setListAl(al);

		try {
			if(	as.setDb(vet)) {
			oos.writeObject(as);
			oos.flush();}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public static void writeAnimals(ObservableList<AnimP> d, File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<AnimalEv> al=new ArrayList<AnimalEv>();
		AnimalEv aa;
		for(AnimP a:d) {
			aa=a.getAnial();
			System.out.print(aa.getNom());
			al.add(aa);
		}
		System.out.print("En ecroina" + al.size());
		for (AnimalEv an:al) {
			System.out.println(an.getNom());
			an.describe();
		}
		try {
			oos.writeObject(al);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public static void writeAnimal(AnimalEv d, String file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<AnimalEv> al=new ArrayList<AnimalEv>();
		
	al.add(d);
	
		try {
			oos.writeObject(al);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public static void writeReferences(ObservableList<ReferenceEv> d, File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<ReferenceEv> al=new ArrayList<ReferenceEv>();
		for(ReferenceEv a:d) {
			al.add(a);
		}
		try {
			oos.writeObject(al);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public static void writeRecettes(ObservableList<Recette> d, File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<Recette> al=new ArrayList<Recette>();
		for(Recette a:d) {
			al.add(a);
		}
		try {
			oos.writeObject(al);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public static void writeMethods(ObservableList<AdjustSaveEv> d, File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		ArrayList<AdjustSaveEv> al=new ArrayList<AdjustSaveEv>();
		for(AdjustSaveEv a:d) {
			al.add(a);
		}
		try {
			oos.writeObject(al);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public listAlim readAlim(){
		listAlim l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/aliments.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (listAlim) ois.readObject();
				l.getAlim(2).getNom();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException |NullPointerException| ClassNotFoundException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try{
				FileInputStream fis = new FileInputStream("resources/aliments-secours.vbr");
				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					l = (listAlim) ois.readObject();
					l.getAlim(2).getNom();
					 	}  finally {
					try {
						ois.close();
					} finally {
						fis.close();
					}
				}}
			catch (IOException |NullPointerException| ClassNotFoundException f ) {
				// TODO Auto-generated catch block
				
				 JOptionPane jop2 = new JOptionPane();
					jop2.showMessageDialog(null, "Impossible de lire la base de donn??es. Lecture de la base de r??f??rence. ", "Echec", JOptionPane.INFORMATION_MESSAGE);
		
				f.printStackTrace();
				try{
					FileInputStream fis = new FileInputStream("resources/aliments-ref.vbr");
					ObjectInputStream ois = new ObjectInputStream(fis);
					try {
						l = (listAlim) ois.readObject();
						l.getAlim(2).getNom();
						 	}  finally {
						try {
							ois.close();
						} finally {
							fis.close();
						}
					}}
				catch (IOException | NullPointerException| ClassNotFoundException g ) {
					// TODO Auto-generated catch block
					
					 JOptionPane jop3 = new JOptionPane();
						jop3.showMessageDialog(null, "Impossible de lire la base de r??f??rence", "Echec", JOptionPane.INFORMATION_MESSAGE);
			
					g.printStackTrace();
				} 
			} 
		} 
		
		try {
		
		}catch(NullPointerException e) {
			
		}

		return l;
	}


	public listAlim readAlimImport(String path){
		listAlim l=null;
		try{
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (listAlim) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return l;
	}
	public listAlim readAlimMAJ(){
		listAlim l=null;
		try{
			FileInputStream fis = new FileInputStream("Update/aliments-MAJ.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (listAlim) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{

            File file = new File("Update/aliments-MAJ.vbr");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
                JOptionPane jop3 = new JOptionPane();
				jop3.showMessageDialog(null, "Votre base des aliments a bien ??t?? mise ?? jour", "Mise ?? jour", JOptionPane.INFORMATION_MESSAGE);
	
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }

		return l;
	}
	public ListAdjust readAdjustMAJ(){
		ListAdjust l=null;
		try{
			FileInputStream fis = new FileInputStream("Update/adj-MAJ.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (ListAdjust) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{

            File file = new File("Update/adj-MAJ.vbr");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
                JOptionPane jop3 = new JOptionPane();
				jop3.showMessageDialog(null, "Votre base des m??thodes de calcul a bien ??t?? mise ?? jour", "Mise ?? jour", JOptionPane.INFORMATION_MESSAGE);
	
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }

		return l;
	}
	public void writeAnimal(listAnim d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/animaux.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public listAnim readAnim(){
		listAnim l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/animaux.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (listAnim) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return l;
	}
	public void writeRef(ListReference d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/reference.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public ListReference readReference(){
		ListReference l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/reference.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (ListReference) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		if (l==null){
			l=new ListReference();
		}

		return l;
	}
	
	public void writeListAdjust(ListAdjust d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/adj.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public ListAdjust readListAdjust(){
		ListAdjust l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/adj.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (ListAdjust) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		if (l==null){
			l=new ListAdjust();
		}

		return l;
	}
	
	public void writeListRecette(ListRecette d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/recettes.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	
	public ListRecette readListRecette(){
		ListRecette l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/Recettes.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (ListRecette) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		if (l==null){
			l=readListRecetteBack();
			try {
				writeListRecette(l);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return l;
	}
	public ListRecette readListRecetteBack(){
		ListRecette l=null;
		try{
			FileInputStream fis = new FileInputStream("Update/Recettes.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (ListRecette) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		}
		if (l==null){
			l=new ListRecette();
		}

		return l;
	}
	
	
	
	public void writePref(Preferences d) throws IOException{
		FileOutputStream fos = new FileOutputStream("resources/pref.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public Preferences readPref(){
		Preferences l=null;
		try{
			FileInputStream fis = new FileInputStream("resources/pref.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (Preferences) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch(FileNotFoundException e){
			try {
				writePref(new Preferences()) ;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (l==null){
			l=new Preferences();
		}

		return l;
	}
	public void writeVet(Vet d) throws IOException{
		FileOutputStream fos = new FileOutputStream((DataConnector.isWindows()?"":"../")+"db/vet.vbr");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(d);
			oos.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				fos.close();
			}
		}
	}
	public Vet readVet(){
		Vet l=null;
		try{
			FileInputStream fis = new FileInputStream((DataConnector.isWindows()?"":"../")+"db/vet.vbr");
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				l = (Vet) ois.readObject();
			}  finally {
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		return l;
	}

	private static String[] getHeader(){
		int i=16;
		i=i+22+NutrientBase.size()+NutrientOther.size()+NutrientMacro.size()+NutrientMin.size()+NutrientVitam.size()+NutrientLipid.size();
		String[] he=new String[i];
		he[0]="ID";
		he[1]="ESPECE";
		he[2]="MARQUE";
		he[3]="GAMME";
		he[4]="NAME";
		he[5]="LABEL";
		he[6]="TYPEALIM";

		he[7]="PRIX";
		he[8]="CATEGPRIX";
		he[9]="INDICAT";
		he[10]="PRES";
		he[11]="PRESQUANT";
		he[12]="PRESTYPE";
		he[13]="DEPRECATED";
		he[14]="DBID";
		he[15]="DBNOM";
		he[16]="DBDESC";
		int j=17;
		for(NutrientBase enu:NutrientBase.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(NutrientMacro enu:NutrientMacro.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(NutrientMin enu:NutrientMin.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(NutrientLipid enu:NutrientLipid.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(NutrientVitam enu:NutrientVitam.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(NutrientOther enu:NutrientOther.values()){
			he[j]=enu.getLabel();
			j++;
		}
		for(AAEnum enu:AAEnum.values()){
			he[j]=enu.getLabel();
			j++;
		}
		return he;


	}
}
