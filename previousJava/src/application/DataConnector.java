package application;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.sql.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.AnimP;
import DataStruct.BiblioP;
import DataStruct.CoefP;
import DataStruct.EquationConsP;
import DataStruct.ReferenceP;
import DataStruct.SupplementalvariableP;
import Enumerise.AAEnum;
import Enumerise.AllNutrient;
import Enumerise.EquationKind;
import Enumerise.FoodKind;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import Enumerise.UnitReqEnum;
import Enumerise.VariableKind;
import controller.ProgressController;
import equation.Equation;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.AdjustSave;
import model.AdjustSaveEv;
import model.AlimDBList;
import model.AlimIndic;
import model.AlimSaver;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentUnif;
import model.Animal;
import model.AnimalEv;
import model.BiblioRef;
import model.Consultation;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.ListAdjust;
import model.ListRecette;
import model.RaceChat;
import model.RaceChien;
import model.Ration;
import model.Recette;
import model.Reference;
import model.ReferenceEv;
import model.TargetDefinition;
import model.TargetDefinitionEv;
import model.TypeAlim;
import model.WeightDate;
import model.alimDB;
import model.listAlim;
import model.listAnim;
import model.listConsultEv;
import model.targetAdjust;

public class DataConnector {
	   private  SimpleDoubleProperty progress = new SimpleDoubleProperty();

	    public double getProgress() {
	        return progressProperty().get();
	    }

	    public SimpleDoubleProperty progressProperty() {
	        return progress ;
	    }

	public static boolean MainUpdaterAlim() {
		FileInputStream fis ;
		ObjectInputStream ois ;
		boolean resp=false;
		 File file ;
		try{
			 fis = new FileInputStream((isWindows()?"":"../")+"Update/aliments-MAJ.vbrf");
			 ois = new ObjectInputStream(fis);
			ArrayList<AlimentEv> al=new ArrayList<AlimentEv>();
			try {
				AlimSaver as = (AlimSaver ) ois.readObject();
				
				for (alimDB db:as.getDb().values()) {
					addAlimDB(db, null);
				}
				
				UpdateListAlim(as.getListAl());	 	resp=true;	}  finally {
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

             file = new File((isWindows()?"":"../")+"Update/aliments-MAJ.vbrf");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
              
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }
		return(resp);
		
	}
	public static boolean MainUpdaterRecipe() {
		FileInputStream fis ;
		ObjectInputStream ois ;
		boolean resp=false;
		 File file ;
		 
		 
		try{
			 fis = new FileInputStream((isWindows()?"":"../")+"Update/recipe-MAJ.vbre");
			 ois = new ObjectInputStream(fis);
			ArrayList<Recette> l=new ArrayList<Recette>();
			try {
				l = (ArrayList<Recette>) ois.readObject();
			updateRecette(l);
				resp=true;	}  finally {
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

             file = new File((isWindows()?"":"../")+"Update/recipe-MAJ.vbre");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
              
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }
		return(resp);
		
	}
		public static void MainUpdaterRef() {
			
			FileInputStream fis ;
			ObjectInputStream ois ;
			 File file ;
		
		try{
				 fis = new FileInputStream((isWindows()?"":"../")+"Update/ref-MAJ.vbrr");
			 ois = new ObjectInputStream(fis);
			ArrayList<ReferenceEv> alm=new ArrayList<ReferenceEv>();
			try {
				alm = (ArrayList<ReferenceEv> ) ois.readObject();
				updateListReference(alm, null);			}  finally {
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

             file = new File( (isWindows()?"":"../")+"Update/ref-MAJ.vbrr");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
              
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }
        }
		
public static void MainUpdatermeth() {
			
			FileInputStream fis ;
			ObjectInputStream ois ;
			 File file ;
		
		try{
				 fis = new FileInputStream((isWindows()?"":"../")+"Update/meth-MAJ.vbrm");
			 ois = new ObjectInputStream(fis);
		      ArrayList<AdjustSaveEv>  alm=new       ArrayList<AdjustSaveEv> ();
			try {
				alm = (ArrayList<AdjustSaveEv> ) ois.readObject();
				updateListMethod(alm);			}  finally {
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

             file = new File( (isWindows()?"":"../")+"Update/meth-MAJ.vbrm");

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
              
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){

            e.printStackTrace();

        }
        }
		
	
	
	public static void eraseAll(ObservableList<AnimP>ol) {
		Connection 	conn = null;
		try {	
			conn=connectAnim();
			AnimalEv ani;
			String sql="";
			Statement stmt  = conn.createStatement();
			ResultSet rs  ;
			for (AnimP al:ol) {
				sql= "SELECT UUID, date, object, observation, cRendu, weight , idealWeight, water, bodyFat, methodAnalysis, BCS, k1Id, k1Value, k2Id, k2Value, k3Id, k3Value, k4Id, k4Value, k5Id, k5Value, nLittle, pAdult, coefGes, coefLact, idAnim, MCS FROM CONSULTATIONS WHERE idAnim=\""+al.getUUID()+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {

					DataConnector.DeleteConsultation(rs.getString("UUID"), conn);
				}
				sql = "DELETE FROM ANIMALS WHERE UUID =\""+al.getUUID()+"\"";

				stmt.executeUpdate(sql);
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {

			close(conn);}
	}

	
	public static void testAlim() {
		Connection conn=connect();
		boolean needCreat=false;
		ArrayList<String> sqlList=new ArrayList<String>();
		try {
			DatabaseMetaData dbm = conn.getMetaData();
			Statement	stmt = conn.createStatement();     
		
			
			// check if "employee" table is there
			ResultSet tables = dbm.getTables(null, null, "FOOD", null);
			if (tables.next()) {

			}
			else {
				needCreat=true;
				DBcreate.CreateFoodDB( conn);
		close(conn);
				  File src = new File(
							 (isWindows()?"":"../")
							+ "mdb/Data-Food.db"); 
				    File dest = new File(	 (isWindows()?"":"../")
							+ "db/Data-Food.db"); 
				    InputStream is = null;
				    OutputStream os = null;
				  
				    try {
				        is = new FileInputStream(src);
				        os = new FileOutputStream(dest);
				        byte[] buffer = new byte[1024];
				        int len;
				        while ((len = is.read(buffer)) > 0) {
				            os.write(buffer, 0, len);
				        }
				        is.close();
				        os.close();
				    }
				    catch(IOException e){
				        e.printStackTrace();
				    }


			
			}	
			if (!needCreat) {
				tables =  stmt.executeQuery("SELECT * FROM FOOD");


					if (	!	DataConnector.hasColumn(tables, "deprecated")) {
						
						sqlList.add(	"ALTER TABLE FOOD ADD \"deprecated\"	INTEGER default 0");
					}
			if (	!	DataConnector.hasColumn(tables, "DataB")) {
						sqlList.add(     "CREATE TABLE \"dataDef\" (\n"
		           		+ "	\"UUID\"	TEXT NOT NULL,\n"
		           		+ "	\"sNAME\"	TEXT,\n"
		           		+ "	\"compNAME\"	TEXT,\n"
		           		+ "	PRIMARY KEY(\"UUID\")\n"
		           		+ ")"  );
						sqlList.add(	"ALTER TABLE FOOD ADD \"DataB\"	TEXT default \"any\"");
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
									"VALUES (\"0\","
									+"\"CALNUT 2017 \","
									+"\"Anses. 2017. Table de composition nutritionnelle Ciqual pour le calcul des apport nutritionnels CALNUT \")"
									);
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
								"VALUES (\"1\","
								+"\"FCÉN 2015 \","
								+"\"Fichier canadien sur les éléments nutritifs, 2015\" )"
								);
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
								"VALUES (\"7\","
								+"\"USDA Food DataBase \","
								+"\"U.S. Department of Agriculture, Agricultural Research Service. FoodData Central, 2019. fdc.nal.usda.gov.\")"
								);
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
								"VALUES (\"2\","
								+"\"PetFood 2020 \","
								+"\"Main Pet Food Database Lefebvre, 2020 \")"
								);
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
								"VALUES (\"5\","
								+"\"BARF 2020 \","
								+"\"Main Barf Database, 2020\")"
								);
						sqlList.add(	 "INSERT OR REPLACE INTO dataDef (UUID, sNAME , compNAME) " +
								"VALUES (\"4\","
								+"\"Generic \","
								+"\"Generic Database \")"
								);
						
						sqlList.add("UPDATE FOOD \n"
								+ "SET DataB = \"0\" WHERE typeAlim = 0 "
								);
						sqlList.add("UPDATE FOOD  \n"
								+ "SET DataB = \"1\" WHERE typeAlim = 1" 
								);
						sqlList.add("UPDATE FOOD \n"
								+ "SET DataB = \"2\" WHERE typeAlim = 2 "
								);
						sqlList.add("UPDATE FOOD \n"
								+ "SET DataB = \"2\" WHERE typeAlim = 3 "
								);
						sqlList.add("UPDATE FOOD  \n"
								+ "SET DataB = \"4\" WHERE typeAlim = 4"
								);
						sqlList.add("UPDATE FOOD  \n"
								+ "SET DataB = \"5\" WHERE typeAlim = 5"
								);
					}
			}
	if (sqlList.size()>0) {
				
				for (String sq:sqlList) {
					 System.out.println(sq);
					 stmt.executeUpdate(sq);
				
				}}
		
		} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
						close(conn);
					}
				
			
	
	}
	
	public static void testAnim() {
		Connection conn=connectAnim();
		boolean needCreat=false;
		ArrayList<String> sqlList=new ArrayList<String>();
		try {
			DatabaseMetaData dbm = conn.getMetaData();
			Statement	stmt = conn.createStatement();     
			// check if "employee" table is there
			ResultSet tables = dbm.getTables(null, null, "ANIMALS", null);
			if (tables.next()) {

			}
			else {
				DBcreate.CreateAnimalDB(conn);
				DataConnector.UpdateRaceOld(conn);

				ArrayList<Recette> l;
				FileInputStream 	fis = null;
				ObjectInputStream 	ois = null;


				try {
					fis = new FileInputStream(	 (isWindows()?"":"../")+"mdb/recipeMain");
					ois = new ObjectInputStream(fis);
					l = (ArrayList<Recette>) ois.readObject();
					DataConnector.updateRecette(l);


				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					Alert alert = new Alert(AlertType.ERROR);

				} catch (ClassCastException e) {
					// TODO Auto-generated catch block

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}  finally {
					try {
						ois.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} finally {
						try {
							fis.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Alert alert = new Alert(AlertType.ERROR);

						}
					}
				}
			}
			
			
			if (!needCreat) {
			
				
				}
			if (sqlList.size()>0) {
				
				for (String sq:sqlList) {
					
					 stmt.executeUpdate(sq);
					 System.out.println(sq);
				}}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);

		}
		finally {
			close(conn);
		}
	}
	public static void testRef() {
		Connection conn=connectReferences();
		boolean needCreat=false;
		ArrayList<String> sqlList=new ArrayList<String>();
		try {
			Statement	stmt = conn.createStatement();     
			DatabaseMetaData dbm = conn.getMetaData();
			// check if "employee" table is there
			ResultSet tables = dbm.getTables(null, null, "equation", null);
			if (tables.next()) {

			
			}
			else {
needCreat=true;
			}
			if (!needCreat) {
			tables =  stmt.executeQuery("SELECT * FROM equation");


				if (	!	DataConnector.hasColumn(tables, "nutrient")) {
					System.out.println("pas nut");
					sqlList.add(	"ALTER TABLE equation ADD \"nutrient\"	INTEGER");
				}
			
			
			
			tables = dbm.getTables(null, null, "speReqEq", null);
			if (tables.next()) {			
			}
			else {
			
				sqlList.add(	"CREATE TABLE \"speReqEq\" (\"refRef\"	TEXT, \"refEq\"	TEXT,		UNIQUE(\"refEq\", \"refRef\"),"
				        			+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),"
				        			+ "	FOREIGN KEY(\"refEq\") REFERENCES \"equation\"(\"UUID\")"
				        		   + ")");    
			}
			

			if (sqlList.size()>0) {
		
			for (String sq:sqlList) {
				
				 stmt.executeUpdate(sq);
				 System.out.println(sq);
			}}}
			if (needCreat) {
				
				
				DBcreate.CreateRefDB(conn);
close(conn);

				ArrayList<ReferenceEv> l;
				FileInputStream 	fis = null;
				ObjectInputStream 	ois = null;


				try {
					conn=connectReferences();
					fis = new FileInputStream(	 (isWindows()?"":"../")+"mdb/refMain");
					ois = new ObjectInputStream(fis);
					l = (ArrayList<ReferenceEv>) ois.readObject();
					DataConnector.updateListReference(l, conn);


				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();


				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}  finally {
					try {
						ois.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					} finally {
						try {
							fis.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Alert alert = new Alert(AlertType.ERROR);

						}
					}
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
		//	Alert alert = new Alert(AlertType.ERROR);
System.out.println(e);
		}
		finally {
			close(conn);
		}
	}

	public static AlimentEv readAlim(String UUID, Connection conn)  {

		AlimentEv al=null;
		if (UUID!=null) {
			al=new AlimentEv(UUID);
			boolean isconn=false;


			try {	isconn=conn==null?false: true;
			conn=conn==null?connect(): conn;
			String sql = "SELECT UUID, groupAlim, typeAlim,  ingredients, price, categPrice, brand, gamme, nameDef, consistent, quantityPres, unitPres, deprecated , DataB FROM FOOD WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);

			// loop through the result set
			while (rs.next()) {


				al.setGroup(GroupAlim.setById(rs.getInt("groupAlim")));
				al.setTypeAliment(FoodKind.IntToType(rs.getInt("typeAlim")));
				al.setIngredients(rs.getString("ingredients"));
				al.setPrix(rs.getDouble("price"));
				al.setCategoriePrix(rs.getString("categPrice"));
				al.setMarque(rs.getString("brand"));
				al.setGamme(rs.getString("gamme"));
				al.setNom(rs.getString("nameDef"));
				al.setConsistent(rs.getInt("consistent"));
				al.setCont(rs.getInt("unitPres"));
				al.setQuantInt(rs.getFloat("quantityPres"));
				al.setDeprecated(rs.getInt("deprecated"));
				al.setDataB(rs.getString("DataB"));
			}
			sql = "SELECT reffood , value FROM ESPECE WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.addEspeces(rs.getString("value"));
			}

			sql = "SELECT reffood , kind, value FROM VALUEAA WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), AAEnum.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEAA WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), AAEnum.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEBASE WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientBase.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUELIPID WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientLipid.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEMACRO WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientMacro.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEMIN WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientMin.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEVITAM WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientVitam.getByCoef(rs.getInt("KIND")));
			}
			sql = "SELECT reffood , kind, value FROM VALUEOTHER WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("value"), NutrientOther.getByCoef(rs.getInt("kind")));
			}
			sql = "SELECT reffood , value FROM INDICATION WHERE reffood  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.addIndicat(rs.getInt("value"));
			}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}
		else return al;
		return al;
	}



	public static ObservableList<AlimP> getAlimList(Connection conn, String cond)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connect(): conn;


		String sql = "SELECT UUID, typeAlim, ingredients, brand, gamme, nameDef FROM FOOD"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set

		ObservableList<AlimP>	list= FXCollections.observableArrayList();
		while (rs.next()) {
			list.add(new AlimP(rs.getString("UUID"), rs.getString("nameDef"), rs.getString("brand"), rs.getString("gamme"), 0.0F));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}
	public static ObservableList<AnimP> getAnimList(Connection conn, String cond, ResourceBundle bun)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectAnim(): conn;


		String sql = "SELECT UUID, name, specie, ownername, race, id FROM ANIMALS"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set

		ObservableList<AnimP>	list= FXCollections.observableArrayList();
		while (rs.next()) {
			list.add(new AnimP(rs.getString("UUID"),
					rs.getString("name"),
					BreedName(rs.getString("specie"),bun.getLocale().getLanguage().toUpperCase() ,rs.getString("race"), conn),
					"",
					rs.getString("ownerName"),
					rs.getString("id"),
					rs.getString("specie")));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}

	public static ObservableList<AlimP> getAllAlim(Connection conn, String condition )  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connect(): conn;


		String sql = "SELECT UUID FROM FOOD"+condition;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set

		ObservableList<AlimP>	list= FXCollections.observableArrayList();
		while (rs.next()) {
		
			list.add(new AlimP(DataConnector.readAlim(   rs.getString("UUID"), conn)));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;



	}

	public static void Initiate() {
		Connection conn=null;
		try {
			conn=connectReferences();
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				sql = "INSERT OR REPLACE INTO Especes " +
						"VALUES (\""+0+"\",\"CARN\",\"CAT\")";
				stmt.executeUpdate(sql);
				sql = "INSERT OR REPLACE INTO Especes " +
						"VALUES (\""+1+"\",\"CARN\",\"DOG\")";
				stmt.executeUpdate(sql);


			}
		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {
			close(conn);
		}

	}

	
	
	public static Connection connectReferences() {
		Connection conn = null;
		try {
			// db parameters
			//String url = "jdbc:sqlite:db/Data-Food.db";
			// create a connection to the database
			//java.lang.ClassNotFoundException: SQLite.JDBCDriver
			String url="jdbc:sqlite:"
					+ (isWindows()?"":"../")
					+ "db/ref.db";
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to ref SQLite has been established.");
			return conn;
		} catch (SQLException e) {

			System.out.println(e.getMessage());

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText("applause "+e.getMessage());

			alert.show();
		}
		return conn; }



	public static Connection connect() {
		Connection conn = null;
		try {
			try {
				// The newInstance() call is a work around for some
				// broken Java implementations


			} catch (Exception ex) {
				// handle the error
			}
			// db parameters
			//String url = "jdbc:sqlite:db/Data-Food.db";
			// create a connection to the database

			String url="jdbc:sqlite:"
					+ (isWindows()?"":"../")
					+ "db/Data-Food.db";
			conn = DriverManager.getConnection(url);


			System.out.println("Connection to alim SQLite has been established.");
			return conn;
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
			System.out.println(e.getMessage());

		}
		return conn; }
	public static Connection connectAnim() {
		Connection conn = null;
		try {
			// db parameters
			//String url = "jdbc:sqlite:db/Data-Food.db";
			// create a connection to the database

			String url="jdbc:sqlite:"
					+ (isWindows()?"":"../")
					+ "db/Data-Anim.db";
			conn = DriverManager.getConnection(url);

			System.out.println("Connection to animal SQLite has been established.");
			return conn;
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
			System.out.println(e.getMessage());

		}
		return conn; }

	
	public static  AlimDBList getAlimDB() {
		Connection conn=null;
		AlimDBList dbList=new AlimDBList();
		conn=connect();
		if(conn!=null) {
			ResultSet rs=null;
			Statement stmt;
			try {
				stmt = conn.createStatement();
			
			String sql = "";
		
		sql = "SELECT UUID, sNAME, compNAME FROM dataDef";

			rs    = stmt.executeQuery(sql);
	
		Ration rat;
		// loop through the result set
		while (rs.next()) {
dbList.add(new alimDB(rs.getString("UUID"),
		rs.getString("sNAME"),
		rs.getString("compNAME")));
		}
		for (alimDB db:dbList.values()) {
			 rs = stmt.executeQuery("SELECT COUNT(*) FROM FOOD WHERE DataB = \""+db.getUUID()+"\"");
             while (rs.next()){
                dbList.setNumber(db.getUUID(), rs.getInt(1));
             }
		}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			close(conn);
		}
		return dbList;
		}
		return dbList;
	}
	
	
	public static void addAlimDB(alimDB db, Connection conn) {
	
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connect(): conn;

			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				sql = "INSERT OR REPLACE INTO dataDef " +
						"VALUES (\""+db.getUUID()+"\",\""+
						db.getsNom()+"\",\""+
						db.getCompNom()+"\")";
				stmt.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(!isconn) {close(conn);}
		}
			
	}
public static void UpdateListAlim(ArrayList<AlimentEv> mainListAlim) {
		Connection conn=null;
		try {
			conn=connect();
			
			if(conn!=null) {
				 conn.setAutoCommit(false);
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAlim=0;
				int totalAlim=mainListAlim.size();
				for (AlimentEv alim:mainListAlim) {
					sql = "DELETE FROM INDICATION WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);

					sql = "DELETE FROM ESPECE WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);
					System.out.println(""+numAlim+"/"+totalAlim+" "+alim.getNom()+", "+alim.getDataB());
					sql = "INSERT OR REPLACE INTO FOOD(UUID, groupAlim, typeAlim,Ingredients, price, categPrice, brand, gamme, unitPres, quantityPres, version, date, nameDef, consistent, deprecated, DataB) " +
						"VALUES (\""+alim.getUUID()+"\","
							+alim.getGroup().getId()+","
							+alim.getTypeAliment().getCoef()+",\""
							+alim.getIngredients().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getPrix()+",\""
							+alim.getCategoriePrix()+"\",\""
							+alim.getFamillyBrand().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\",\""
							+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getConsistent()+", "+(alim.isDeprecated()?1:0)+",\""+alim.getDataB()+"\""+		
							")";
					
					stmt.executeUpdate(sql);
					sql = "INSERT OR REPLACE INTO NAME ( reffood , lang, value)" +
							"VALUES (\""+alim.getUUID()+"\",'FR',\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\")";
					stmt.executeUpdate(sql);
					for( String s: alim.getEspeces()) {

						sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
								"VALUES (\""+alim.getUUID()+"\",\""
								+s+"\")";
						stmt.executeUpdate(sql);

					}

					for(AAEnum enu:AAEnum.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					}

					for(NutrientBase enu:NutrientBase.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientLipid enu:NutrientLipid.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMin enu:NutrientMin.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMacro enu:NutrientMacro.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientOther enu:NutrientOther.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientVitam enu:NutrientVitam.values()) {
						if(alim.isNutrient(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrient(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for( String s :alim.getIndicat()) {
						sql = "INSERT OR REPLACE INTO INDICATION (reffood , value)" +
								"VALUES (\""
								+alim.getUUID()+"\","
								+AlimIndic.StringToGroup(s).getCoef()+")";
						stmt.executeUpdate(sql);}

					numAlim++;
				}
				conn.commit();
				
				System.out.println(numAlim);}
		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			if (conn != null) {
		        try {
		          System.err.print("Transaction is being rolled back");
		          conn.rollback();
		        } catch (SQLException excep) {
		        
		        }
		      }
			System.out.println(ex.getMessage());
		}
		finally {
			close(conn);
		}

	}

	public boolean UpdateListAlimP(ArrayList<AlimentUnif> mainListAlim) {
		Connection conn=null;
		try {
			conn=connect();
			
			if(conn!=null) {
				 conn.setAutoCommit(false);
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAlim=0;
				int totalAlim=mainListAlim.size();
				for (AlimentUnif alim:mainListAlim) {
									try
									{
									
										progress.set(numAlim*1/totalAlim);
				
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
					sql = "DELETE FROM INDICATION WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);

					sql = "DELETE FROM ESPECE WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);
					
					sql = "INSERT OR REPLACE INTO FOOD " +
							"VALUES (\""+alim.getUUID()+"\","
							+alim.getGroup().getId()+","
							+alim.getTypeAliment().getCoef()+",\""
							+alim.getIngredients().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getPrix()+",\""
							+alim.getCategoriePrix()+"\",\""
							+alim.getFamillyBrand().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\",\""
							+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getConsistent()+", "+(alim.isDeprecated()?1:0)+",\""+alim.getDataB()+"\""+
						
							")";
					
					stmt.executeUpdate(sql);
					sql = "INSERT OR REPLACE INTO NAME ( reffood , lang, value)" +
							"VALUES (\""+alim.getUUID()+"\",'FR',\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\")";
					stmt.executeUpdate(sql);
					for( String s: alim.getEspeces()) {

						sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
								"VALUES (\""+alim.getUUID()+"\",\""
								+s+"\")";
						stmt.executeUpdate(sql);

					}

					for(AAEnum enu:AAEnum.values()) {
						if(alim.isNutrientAcideAmine(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientAcideAmine(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					}

					for(NutrientBase enu:NutrientBase.values()) {
						if(alim.isNutrientBase(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientBase(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientLipid enu:NutrientLipid.values()) {
						if(alim.isNutrientLipid(enu)) {
							sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientLipid(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMin enu:NutrientMin.values()) {
						if(alim.isNutrientMin(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientMin(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMacro enu:NutrientMacro.values()) {
						if(alim.isNutrientMacro(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientMacro(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientOther enu:NutrientOther.values()) {
						if(alim.isNutrientOther(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientOther(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientVitam enu:NutrientVitam.values()) {
						if(alim.isNutrientVitam(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientVitam(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for( String s :alim.getIndicat()) {
						sql = "INSERT OR REPLACE INTO INDICATION (reffood , value)" +
								"VALUES (\""
								+alim.getUUID()+"\","
								+AlimIndic.StringToGroup(s).getCoef()+")";
						stmt.executeUpdate(sql);}

					numAlim++;
				}
				conn.commit();
				
			return true;}
		}
		catch(SQLException ex) {
		
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			
			if (conn != null) {
		        try {
		          System.err.print("Transaction is being rolled back");
		          conn.rollback();
		        } catch (SQLException excep) {
		        
		        }
		      }
			System.out.println(ex.getMessage());
			return false;
		}
		finally {
			close(conn);
			
		}
		return false;

	}
	public static void UpdateListAlim(listAlim mainListAlim) {
		Connection conn=null;
		try {
			conn=connect();
			if(conn!=null) {
				 conn.setAutoCommit(false);
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAlim=0;
				int totalAlim=mainListAlim.size();
				for (AlimentUnif alim:mainListAlim.getAlim()) {
					sql = "DELETE FROM INDICATION WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);

					sql = "DELETE FROM ESPECE WHERE reffood  =\""+alim.getUUID()+"\"";

					stmt.executeUpdate(sql);
					System.out.println(""+numAlim+"/"+totalAlim+" "+alim.getNom()+", "+alim.getDataB());
					sql = "INSERT OR REPLACE INTO FOOD(UUID, groupAlim, typeAlim,Ingredients, price, categPrice, brand, gamme, unitPres, quantityPres, version, date, nameDef, consistent, deprecated, DataB) " +
							"VALUES (\""+alim.getUUID()+"\","
							+alim.getGroup().getId()+","
							+alim.getTypeAliment().getCoef()+",\""
							+alim.getIngredients().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getPrix()+",\""
							+alim.getCategoriePrix()+"\",\""
							+alim.getFamillyBrand().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\",\""
							+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","
							+alim.getConsistent()+", "+(alim.isDeprecated()?1:0)+",\""+alim.getDataB()+"\""+ ")";

					stmt.executeUpdate(sql);
					sql = "INSERT OR REPLACE INTO NAME ( reffood , lang, value)" +
							"VALUES (\""+alim.getUUID()+"\",'FR',\""
							+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\")";
					stmt.executeUpdate(sql);
					if (alim.getTypeAliment()==FoodKind.COMPLET| alim.getTypeAliment()==FoodKind.COMPLEMENTAIRE) {
						if (alim.getEspece()!=2) {
							sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
									"VALUES (\""+alim.getUUID()+"\",\""
									+alim.getEspece()+"\")";
							stmt.executeUpdate(sql);
						}else {
							sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
									"VALUES (\""+alim.getUUID()+"\",\""
									+0+"\")";
							stmt.executeUpdate(sql);
							sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
									"VALUES (\""+alim.getUUID()+"\",\""
									+1+"\")";
							stmt.executeUpdate(sql);
						}
					}

					for(AAEnum enu:AAEnum.values()) {
						if(alim.isNutrientAcideAmine(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientAcideAmine(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					}

					for(NutrientBase enu:NutrientBase.values()) {
						if(alim.isNutrientBase(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientBase(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientLipid enu:NutrientLipid.values()) {
						if(alim.isNutrientLipid(enu)) {
							sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientLipid(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMin enu:NutrientMin.values()) {
						if(alim.isNutrientMin(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientMin(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientMacro enu:NutrientMacro.values()) {
						if(alim.isNutrientMacro(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientMacro(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientOther enu:NutrientOther.values()) {
						if(alim.isNutrientOther(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientOther(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for(NutrientVitam enu:NutrientVitam.values()) {
						if(alim.isNutrientVitam(enu)) {
							sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
									"VALUES (\""+enu.getCoef()+"\",\""
									+alim.getUUID()+"\","
									+1+",\""
									+alim.getNutrientVitam(enu)+"\","
									+"\"2021-12-20\""+")";
							stmt.executeUpdate(sql);}
					} 
					for( String s :alim.getIndicat()) {
						sql = "INSERT OR REPLACE INTO INDICATION (reffood , value)" +
								"VALUES (\""
								+alim.getUUID()+"\","
								+AlimIndic.StringToGroup(s).getCoef()+")";
						stmt.executeUpdate(sql);}

					numAlim++;
				}
				System.out.println(numAlim);
				conn.commit();}
		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
		
				try {
					conn.rollback();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			System.out.println(ex.getMessage());
		}
		finally {
			close(conn);
		}

	}

	
	
	
	
	
	public static void UpdateAlim(AlimentEv nAlim, Connection conn) {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connect(): conn;

		
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAlim=0;
				sql = "DELETE FROM INDICATION WHERE reffood  =\""+nAlim.getUUID()+"\"";

				stmt.executeUpdate(sql);

				sql = "DELETE FROM ESPECE WHERE reffood  =\""+nAlim.getUUID()+"\"";

				stmt.executeUpdate(sql);
				sql = "INSERT OR REPLACE INTO FOOD (UUID, groupAlim, typeAlim,Ingredients, price, categPrice, brand, gamme, unitPres, quantityPres, version, date, nameDef, consistent, deprecated, DataB)" +
						"VALUES (\""+nAlim.getUUID()+"\","
						+nAlim.getGroup().getId()+","
						+nAlim.getTypeAliment().getCoef()+",\""
						+nAlim.getIngredients()+"\","
						+nAlim.getPrix()+",\""
						+nAlim.getCategoriePrix()+"\",\""
						+nAlim.getFamillyBrand().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\",\""
						+nAlim.getGamme().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\","+nAlim.getCont().getID()+","+nAlim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
						+nAlim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\", "
						+nAlim.getConsistent()+","+(nAlim.isDeprecated()?1:0)+",\""+nAlim.getDataB()+"\""+")";
System.out.println(sql);
				stmt.executeUpdate(sql);
				sql = "INSERT OR REPLACE INTO NAME ( reffood , lang, value)" +
						"VALUES (\""+nAlim.getUUID()+"\",'FR',\""
						+nAlim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "").replaceAll("'", "\'").replace('"', ' ')+"\")";
				stmt.executeUpdate(sql);
				for (String esp:nAlim.getEspeces()) {
					sql = "INSERT OR REPLACE INTO ESPECE ( reffood , value)" +
							"VALUES (\""+nAlim.getUUID()+"\",\""
							+esp+"\")";
					stmt.executeUpdate(sql);
				}

				for(AAEnum enu:AAEnum.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEAA WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				}

				for(NutrientBase enu:NutrientBase.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEBASE WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);}
				} 
				for(NutrientLipid enu:NutrientLipid.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUELIPID WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);}
				} 
				for(NutrientMin enu:NutrientMin.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEMIN WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);}
				} 
				for(NutrientMacro enu:NutrientMacro.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEMACRO WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
				for(NutrientOther enu:NutrientOther.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEOTHER WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);}
				} 
				for(NutrientVitam enu:NutrientVitam.values()) {
					if(nAlim.isNutrient(enu)) {
						sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
								"VALUES (\""+enu.getCoef()+"\",\""
								+nAlim.getUUID()+"\","
								+1+",\""
								+nAlim.getNutrient(enu)+"\","
								+"\"2021-12-20\""+")";
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEVITAM WHERE kind="+enu.getCoef()+" AND reffood =\"" +nAlim.getUUID()+"\"";
							stmt.executeUpdate(sql);}
				} 


				stmt.executeUpdate(sql);
				for( String s :nAlim.getIndicat()) {
					sql = "INSERT OR REPLACE INTO INDICATION (reffood , value)" +
							"VALUES (\""
							+nAlim.getUUID()+"\","
							+AlimIndic.StringToGroup(s).getCoef()+")";
					stmt.executeUpdate(sql);}
				numAlim++;
			}

		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setContentText(ex.getMessage());
			alert.show();	
		}
		finally {
			if(!isconn) {
				close(conn);}
		}

	}
	/**
	 * @param args the command line arguments
	 */


	public static void UpdateRaceOld(Connection conn) {

		try {
			conn=connectAnim();
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAnim=0;
				int totalAnim=0;


				for (RaceChien r:RaceChien.values())
				{

					System.out.println(r.nameToString());

					sql = "INSERT OR REPLACE INTO Breed " +
							"VALUES (\""+r.nameToID()+"\",\""

               		+0+"\")";

					stmt.executeUpdate(sql);
					sql = "INSERT OR REPLACE INTO breedName " +
							"VALUES (\""+r.nameToID()+"\","
							+"\"FR\",\""
							+r.nameToString()+"\")";

					stmt.executeUpdate(sql);
				}
				for (RaceChat r:RaceChat.values())
				{
					numAnim++;

					sql = "INSERT OR REPLACE INTO Breed " +
							"VALUES (\""+r.name()+"\",\""

               		+1+"\")";

					stmt.executeUpdate(sql);
					sql = "INSERT OR REPLACE INTO breedName " +
							"VALUES (\""+r.name()+"\","
							+"\"FR\",\""
							+r.nameToString()+"\")";

					stmt.executeUpdate(sql);
				}
			}

		}   catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {

		}
	}



	public static void UpdateConsultationOld(listAnim mainListAnim, ProgressController PC) {
		Connection conn=null;
		try {
			conn=connectAnim();
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAnim=0;
				int totalAnim=mainListAnim.size();
				PC.setMax(totalAnim);
				int year;
				int monthnum;
				String month="";
				String day=""; 
				int dead;
				int actual;

				for (Animal anim:mainListAnim.getListAnim())
				{
					numAnim++;
					PC.setProg(numAnim);
					year =anim.getDateNaiss().getYear()+1900;
					monthnum=anim.getDateNaiss().getMonth()+1;
					month=monthnum<10? "0"+monthnum:""+monthnum;
					day=anim.getDateNaiss().getDate()<10? "0"+anim.getDateNaiss().getDate(): anim.getDateNaiss().getDate()+"";
					dead=anim.isDead()? 1:0;
					sql = "INSERT OR REPLACE INTO ANIMALS " +
							"VALUES (\""+anim.getUUID()+"\",\""

               		+anim.getNom().replaceAll("'", "\'").replace('"', ' ')+"\","
               		+dead+",\""
               		+anim.getId()+"\","
               		+anim.getSex().getID()+","
               		+anim.getEspece().getCategorie()
               		+",\""
               		+anim.getNomProprio().replaceAll("'", "\'").replace('"', ' ')+"\",\""
               		+year+"-"+month+"-"+day+"\",\""
               		+anim.getRace().toString()+"\",\""
               		+(anim.getResume()!=null?anim.getResume().replaceAll("'", "\'").replace('"', ' '):"") +"\")";

					stmt.executeUpdate(sql);
					for (Consultation cons:anim.getList().getListConsult()) {
						year =cons.getDate().getYear()+1900;
						monthnum=cons.getDate().getMonth()+1;
						month=monthnum<10? "0"+monthnum:""+monthnum;
						day=cons.getDate().getDate()<10? "0"+cons.getDate().getDate(): cons.getDate().getDate()+"";
						sql = "INSERT OR REPLACE INTO CONSULTATIONS " +
								"VALUES (\""+cons.getUUID()+"\",\""
								+year+"-"+month+"-"+day+"\",\""
								+(cons.getObjet()!=null?cons.getObjet().replaceAll("'", "\'").replace('"', ' '):"")+"\",\""
								+cons.getObservation()+"\",\""
								+cons.getCRendu().replaceAll("'", "\'").replace('"', ' ')+"\","
								+cons.getPoids()+","
								+ cons.getPoidsIdeal()+","
								+cons.getBoisson()+","
								+cons.getTauxMG()+",\""
								+cons.getRefString()+"\","
								+cons.getNec().getNewID()+","
								+	"\"\""+","
								+cons.getK1value()+","
								+	"\"\""+","
								+cons.getK2value()+","
								+	"\"\""+","
								+cons.getK3value()+","
								+	"\"\""+","
								+cons.getK4value()+","
								+"\"\""+","
								+cons.getK5value()+","
								+cons.getNbPetit()+","
								+cons.getpMere()+","
								+cons.getGes().ordinal()+","
								+cons.getLac().ordinal()+",\""
								+anim.getUUID()
								+"\","
								+1
								+ ")";

						stmt.executeUpdate(sql);
						ArrayList<Ration> rats=(ArrayList<Ration>) cons.getNewRation().clone();
						cons.getPreviousRation().setActual(false);
						for(Ration r:rats) {
							r.setActual(true);
						}
						rats.add(cons.getPreviousRation());

						for(Ration rat:rats) {
							if (rat.getAlimentList().size()>0) {
								actual=rat.isActual()?1:0;
								sql = "INSERT OR REPLACE INTO RATION " +
										"VALUES (\""+rat.getUUID()+"\",\""
										+cons.getUUID()+"\",\""
										+(rat.isActual()?"Proposed ":"Old ") +rat.getNumber()+"\","
										+rat.getCoef()+","
										+actual+","
										+rat.getNumber()     +",\"ALL\","+0+",\"no\""                  							
										+")";

								stmt.executeUpdate(sql); 
								for (AlimentRation alimr:rat.getAlimentList()) {
									AlimentEv alim=alimr.getAlim();

									sql = "INSERT OR REPLACE INTO FOOD " +
											"VALUES (\""+alimr.getUUID()+"\","
											+alim.getGroup().getId()+","
											+alim.getTypeAliment().getCoef()+",\""
											+alim.getIngredients()+"\","
											+alim.getPrix()+",\""
											+alim.getCategoriePrix()+"\",\""
											+alim.getFamillyBrand()+"\",\""
											+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()
											+","+1+","+"\"2021-12-20\""+",\""
											+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\",\""
											+rat.getUUID()+"\","
											+alimr.getQuantite()+",\""
											+alim.getUUID() 
											+"\","
											+alimr.getTarget().getCoef()+ ")";

									stmt.executeUpdate(sql);
									sql = "INSERT OR REPLACE INTO NAMEFOOD ( reffood , lang, value)" +
											"VALUES (\""+alimr.getUUID()+"\",'FR',\""
											+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\")";
									stmt.executeUpdate(sql);
									for(AAEnum enu:AAEnum.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									}

									for(NutrientBase enu:NutrientBase.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientLipid enu:NutrientLipid.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientMin enu:NutrientMin.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientMacro enu:NutrientMacro.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientOther enu:NutrientOther.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientVitam enu:NutrientVitam.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 

								}
							}
						}
					}



				}


			}
		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {
			close(conn);
		}


	}


	public static void UpdateConsultationOldUnique(Animal anim, Connection conn) {
		boolean isconn=false;
		try {	isconn=conn==null?false: true;
		conn=conn==null?connectAnim(): conn;
			
			if(conn!=null) {
				
				Statement stt  = conn.createStatement();
				ResultSet rss  ;

Boolean tou=false;
				Statement stmt = conn.createStatement();
				String sql = "";

			
				int year;
				int monthnum;
				String month="";
				String day=""; 
				int dead;
				int actual;

				
					year =anim.getDateNaiss().getYear()+1900;
					monthnum=anim.getDateNaiss().getMonth()+1;
					month=monthnum<10? "0"+monthnum:""+monthnum;
					day=anim.getDateNaiss().getDate()<10? "0"+anim.getDateNaiss().getDate(): anim.getDateNaiss().getDate()+"";
					dead=anim.isDead()? 1:0;
					sql = "INSERT OR REPLACE INTO ANIMALS " +
							"VALUES (\""+anim.getUUID()+"\",\""

               		+anim.getNom().replaceAll("'", "\'").replace('"', ' ')+"\","
               		+dead+",\""
               		+anim.getId()+"\","
               		+anim.getSex().getID()+","
               		+anim.getEspece().getCategorie()
               		+",\""
               		+anim.getNomProprio().replaceAll("'", "\'").replace('"', ' ')+"\",\""
               		+year+"-"+month+"-"+day+"\",\""
               		+anim.getRace().toString()+"\",\""
               		+(anim.getResume()!=null?anim.getResume().replaceAll("'", "\'").replace('"', ' '):"")+"\")";

					stmt.executeUpdate(sql);
					for (Consultation cons:anim.getList().getListConsult()) {
						year =cons.getDate().getYear()+1900;
						monthnum=cons.getDate().getMonth()+1;
						month=monthnum<10? "0"+monthnum:""+monthnum;
						day=cons.getDate().getDate()<10? "0"+cons.getDate().getDate(): cons.getDate().getDate()+"";
						sql = "INSERT OR REPLACE INTO CONSULTATIONS " +
								"VALUES (\""+cons.getUUID()+"\",\""
								+year+"-"+month+"-"+day+"\",\""
								+(cons.getObjet()!=null?cons.getObjet().replaceAll("'", "\'").replace('"', ' '):"")+"\",\""
								+cons.getObservation()+"\",\""
								+cons.getCRendu().replaceAll("'", "\'").replace('"', ' ')+"\","
								+cons.getPoids()+","
								+ cons.getPoidsIdeal()+","
								+cons.getBoisson()+","
								+cons.getTauxMG()+",\""
								+cons.getRefString()+"\","
								+cons.getNec().getNewID()+","
								+	"\"\""+","
								+cons.getK1value()+","
								+	"\"\""+","
								+cons.getK2value()+","
								+	"\"\""+","
								+cons.getK3value()+","
								+	"\"\""+","
								+cons.getK4value()+","
								+"\"\""+","
								+cons.getK5value()+","
								+cons.getNbPetit()+","
								+cons.getpMere()+","
								+cons.getGes().ordinal()+","
								+cons.getLac().ordinal()+",\""
								+anim.getUUID()
								+"\","
								+1
								+ ")";

						stmt.executeUpdate(sql);
						ArrayList<Ration> rats=(ArrayList<Ration>) cons.getNewRation().clone();
						cons.getPreviousRation().setActual(false);
						for(Ration r:rats) {
							r.setActual(true);
						}
						rats.add(cons.getPreviousRation());
int i=1;
						for(Ration rat:rats) {
							if (rat.getAlimentList().size()>0) {
								actual=rat.isActual()?1:0;
								sql = "INSERT OR REPLACE INTO RATION " +
										"VALUES (\""+rat.getUUID()+"\",\""
										+cons.getUUID()+"\",\""
										+(rat.isActual()?"Proposed ":"Old ") +i+"\","
										+rat.getCoef()+","
										+actual+","
										+rat.getNumber()     +",\"ALL\","+0+",\"no\""                  							
										+")";
i++;
								stmt.executeUpdate(sql); 
								for (AlimentRation alimr:rat.getAlimentList()) {
								
									AlimentEv alim=alimr.getAlim();
									String    sl = "SELECT UUID FROM FOOD WHERE UUID =\""+alimr.getUUID()+"\" AND RefRation !=\""+rat.getUUID()+"\"";


									
								 rss  = stt.executeQuery(sl);

 tou=false;
									// loop through the result set
									while (rss.next()) {

										
tou=true;

									}
									if (tou) {
									alimr.UpUUID(rat.getUUID());
										
									}
									sql = "INSERT OR REPLACE INTO FOOD " +
											"VALUES (\""+alimr.getUUID()+"\","
											+alim.getGroup().getId()+","
											+alim.getTypeAliment().getCoef()+",\""
											+alim.getIngredients()+"\","
											+alim.getPrix()+",\""
											+alim.getCategoriePrix()+"\",\""
											+alim.getFamillyBrand()+"\",\""
											+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()
											+","+1+","+"\"2021-12-20\""+",\""
											+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\",\""
											+rat.getUUID()+"\","
											+alimr.getQuantite()+",\""
											+alim.getUUID() 
											+"\","
											+alimr.getTarget().getCoef()+ ")";

									stmt.executeUpdate(sql);
									sql = "INSERT OR REPLACE INTO NAMEFOOD ( reffood , lang, value)" +
											"VALUES (\""+alimr.getUUID()+"\",'FR',\""
											+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\")";
									stmt.executeUpdate(sql);
									for(AAEnum enu:AAEnum.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									}

									for(NutrientBase enu:NutrientBase.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientLipid enu:NutrientLipid.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientMin enu:NutrientMin.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientMacro enu:NutrientMacro.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientOther enu:NutrientOther.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 
									for(NutrientVitam enu:NutrientVitam.values()) {
										if(alim.isNutrient(enu)) {
											sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood , version, value, date)" +
													"VALUES (\""+enu.getCoef()+"\",\""
													+alimr.getUUID()+"\","
													+1+",\""
													+alim.getNutrient(enu)+"\","
													+"\"2021-12-20\""+")";
											stmt.executeUpdate(sql);}
									} 

								}
							}
						}
					}



				}


			
		}
		catch(SQLException ex) {
			System.out.println(ex.getMessage());
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			
		}
		finally {
			if (!isconn) {
				close(conn);}
		}


	}



	public static ArrayList <AlimentRation> readAlimRat(String UUID,  Connection conn)  {
		boolean isconn=false; 
		AlimentRation alr=null;
		AlimentEv al=null;
		ResultSet rs;
		int i=0;
		ArrayList <AlimentRation> list=new ArrayList<AlimentRation>();
		if (UUID!=null) {


			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;
			String sql = "SELECT UUID, groupAlim, typeAlim,  ingredients, price, categPrice, brand, gamme, nameDef, RefRation, quantity, RefAlimUnif, refTarget, quantityPres, unitPres FROM FOOD WHERE RefRation =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql);
			String UUIDRat="";
			Float quantite;
			targetAdjust targ;
			// loop through the result set
			while (rsmain.next()) {
				i++;
				System.out.println(i);
				al= new AlimentEv(rsmain.getString("RefAlimUnif"));
				al.setGroup(GroupAlim.setById(rsmain.getInt("groupAlim")));
				al.setTypeAliment(FoodKind.IntToType(rsmain.getInt("typeAlim")));
				al.setIngredients(rsmain.getString("ingredients"));
				al.setPrix(rsmain.getDouble("price"));
				al.setCategoriePrix(rsmain.getString("categPrice"));
				al.setMarque(rsmain.getString("brand"));
				al.setGamme(rsmain.getString("gamme"));
				al.setNom(rsmain.getString("nameDef"));
				al.setCont(rsmain.getInt("unitPres"));
				al.setQuantInt(rsmain.getFloat("quantityPres"));
				
				UUIDRat=rsmain.getString("UUID");
				quantite=rsmain.getFloat("quantity");
				targ=targetAdjust.getByCoef( rsmain.getInt("refTarget"));
				alr=new AlimentRation(al, quantite, UUIDRat);      
				alr.setTarget(targ);
				list.add(alr);
			}
			for (AlimentRation alri:list) {
				UUIDRat=alri.getUUID();
				sql = "SELECT reffood , kind, value FROM VALUEAA WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {

					alri.getAlim().setNutrient(rs.getFloat("VALUE"), AAEnum.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUEBASE WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("VALUE"), NutrientBase.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUELIPID WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("VALUE"), NutrientLipid.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUEMACRO WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("VALUE"), NutrientMacro.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUEMIN WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("VALUE"), NutrientMin.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUEVITAM WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("VALUE"), NutrientVitam.getByCoef(rs.getInt("KIND")));
				}
				sql = "SELECT reffood , kind, value FROM VALUEOTHER WHERE reffood  =\""+ UUIDRat+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					alri.getAlim().setNutrient(rs.getFloat("value"), NutrientOther.getByCoef(rs.getInt("kind")));
				}
			}




			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}

		}
		else return list;

		return list;
	}

	public static Collection<? extends String> readListBreed(String IDspecie, String lang)  {
		Connection conn=null;
		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;

		if (IDspecie!=null) {

			try {		conn=connectAnim();
			String sql1 = "SELECT ID FROM Breed WHERE refSpecie =\""+IDspecie+"\"";

			String sql="";

			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql1);
			String UUIDRat="";
			Float quantite;
			// loop through the result set

			while (rsmain.next()) {

				IDb.add(rsmain.getString("ID"));


			}
			for (String tempS:IDb) {
				sql = "SELECT value,lang FROM breedName WHERE refBreed =\""+tempS+"\"";


				rs    = stmt.executeQuery(sql);
				// loop through the result set
				String tempres="";
				while (rs.next()) {
					tempres=rs.getString("value");
					if (lang.equals(rs.getString("lang"))) {
						break;
					}

				}
				res.add(tempres);
			}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				close(conn);
			}}


		return res;
	}

	public static String []readListEspece()  {
		Connection conn=null;
		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;



		try {		conn=connectReferences();
		String sql1 = "SELECT ID, value FROM Especes";

		String sql="";

		Statement stmt  = conn.createStatement();
		ResultSet rsmain    = stmt.executeQuery(sql1);

		while (rsmain.next()) {

			IDb.add(rsmain.getString("value"));


		}



		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			close(conn);
		}
		String[] str=new String[IDb.size()];
		for(int i=0; i<IDb.size(); i++) {
			str[i]=IDb.get(i);
		}

		return str;
	}

	public static String BreedID(String IDspecie,  String searchName)  {
		Connection conn=null;
		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;

		if (IDspecie!=null) {

			try {		conn=connectAnim();
			String sql1 = "SELECT ID FROM Breed WHERE refSpecie =\""+IDspecie+"\"";

			String sql="";

			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql1);
			String UUIDRat="";
			Float quantite;
			// loop through the result set

			while (rsmain.next()) {

				IDb.add(rsmain.getString("ID"));


			}
			for (String tempS:IDb) {
				sql = "SELECT refBreed, value FROM breedName WHERE refBreed =\""+tempS+"\"";


				rs    = stmt.executeQuery(sql);
				// loop through the result set
				while (rs.next()) {
					if (searchName.compareToIgnoreCase( rs.getString("value"))==0) {
						return rs.getString("refBreed");
					}

				}

			}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				close(conn);
			}}
		String[] str=new String[res.size()];
		for(int i=0; i<res.size(); i++) {
			str[i]=res.get(i);
		}

		return "";
	}
	public static String BreedName(String IDspecie, String lang, String searchId, Connection conn)  {

		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;
		boolean touch=false;
		String resp="";




		if (IDspecie!=null) {
			boolean isconn=false;


			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;


			String    sql = "SELECT refBreed, lang, value FROM breedName WHERE refBreed =\""+searchId+"\"";


			Statement stmt  = conn.createStatement();
			rs    = stmt.executeQuery(sql);


			// loop through the result set
			while (rs.next()) {

				if (touch ==false) {
					resp=rs.getString("value");
					touch=rs.getString("lang").equals(lang)?true:false;
				}

			}


			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}

		return resp;
	}
	public static String SpecieName(String IDspecie, String lang, Connection conn)  {

		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;
		boolean touch=false;
		String resp="";
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectAnim(): conn;

		if (IDspecie!=null) {


			String    sql = "SELECT ID, LANG, Value FROM EspeceName WHERE ID =\""+IDspecie+"\"";


			Statement stmt  = conn.createStatement();
			rs    = stmt.executeQuery(sql);


			// loop through the result set
			while (rs.next()) {

				if (touch ==false) {
					resp=rs.getString("Value");
					touch=rs.getString("LANG").equals(lang)?true:false;
				}

			}


		}} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}

		return resp;
	}
	public static String AddBreed(String IDspecie, String lang, String Name, Connection conn)  {

		ArrayList<String> res=new ArrayList<String>();
		ArrayList<String> IDb=new ArrayList<String>();
		ResultSet rs;
		String UUIDRat="";
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectAnim(): conn;
		if (IDspecie!=null) {



			String sql="";

			Statement stmt  = conn.createStatement();

			UUIDRat=java.util.UUID.randomUUID().toString();
			Float quantite;
			// loop through the result set

			sql = "INSERT OR REPLACE INTO Breed " +
					"VALUES (\""+UUIDRat+"\",\""

		+IDspecie+"\")";

			stmt.executeUpdate(sql);
			sql = "INSERT OR REPLACE INTO breedName " +
					"VALUES (\""+UUIDRat+"\","
					+"\""+lang+"\",\""
					+Name+"\")";

			stmt.executeUpdate(sql);


		}	} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			close(conn);
		}
		String[] str=new String[res.size()];
		for(int i=0; i<res.size(); i++) {
			str[i]=res.get(i);
		}

		return UUIDRat;
	}

	public static void DeleteConsultation(String IDcons, Connection conn)  {
		ArrayList<String>list=new ArrayList<String>();


		if (IDcons!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;


			String sql1 = "SELECT UUID FROM RATION WHERE idConsult =\""+IDcons+"\"";
			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql1);
			while (rsmain.next()) {
				list.add(rsmain.getString("UUID"));

			}
			for (String s:list) {
				DeleteRation(s, conn);
			}
			sql1 = "DELETE FROM CONSULTATIONS WHERE UUID =\""+IDcons+"\"";

			stmt.executeUpdate(sql1);


			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if (!isconn) {
					close(conn);}
			}}


	}

	public static void DeleteRation(String IDration, Connection conn)  {
		ArrayList<String>list=new ArrayList<String>();



		if (IDration!=null) {

			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;


			String sql1 = "SELECT UUID FROM FOOD WHERE RefRation =\""+IDration+"\"";
			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql1);

			while (rsmain.next()) {
				list.add(rsmain.getString("UUID"));

			}
			for (String s:list) {
				DeleteFood(s, conn);
			}

			sql1 = "DELETE FROM RATION WHERE UUID =\""+IDration+"\"";
			stmt.executeUpdate(sql1);



			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if (!isconn) {
					close(conn);}
			}}


	}
	public static void DeleteFood(String IDFood, Connection conn)  {


		if (IDFood!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;


			String sql1 = "DELETE FROM VALUEAA WHERE refFood  =\""+IDFood+"\"";
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM VALUEBASE WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM VALUELIPID WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEMACRO WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEMIN WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEOTHER WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM VALUEVITAM WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM NAMEFOOD WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM INDICATION WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM ESPECEFOOD WHERE refFood  =\""+IDFood+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM FOOD WHERE UUID =\""+IDFood+"\"";


			stmt.executeUpdate(sql1);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}


	}
	public static void updateRecette(ListRecette lr) {
		ObservableList<Recette> ol=FXCollections.observableArrayList();
		for (Recette r:lr.getList()) {
			
				ol.add(r);
		}
		updateRecette(ol);
	}
	public static void updateRecette(Recette lr) {
		ObservableList<Recette> ol=FXCollections.observableArrayList();
	
			
				ol.add(lr);
		
		updateRecette(ol);
	}
	public static void updateRecette(ArrayList<Recette> lr) {
		ObservableList<Recette> ol=FXCollections.observableArrayList();
		for (Recette r:lr) {
			ol.add(r);
		}
		updateRecette(ol);
	}
	public static void updateRecette(ObservableList<Recette>ol) {
		String sql = "";

		Connection conn=null;
		try {
			conn=connectAnim();
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				int year;
				int monthnum;
				String month="";
				String day=""; 
				int dead;
				int actual;
				for(Recette rat:ol) {

					sql = "INSERT OR REPLACE INTO RATION " +
							"VALUES (\""+rat.getUUID()+"\",\""
							+"\",\""
							+rat.getName()+"\","
							+0+","
							+0+","
							+rat.getNumber() +","
							+"\""+(rat.getEspece()==null?Espece.CH.getUUID():rat.getEspece().getUUID())+"\","
							+"1,"
							+"\""+rat.getDescription()+"\""
							+")";

					stmt.executeUpdate(sql); 


					for (AlimentRation alimr:rat.getAlimentList()) {
						AlimentEv alim=alimr.getAlim();

						sql = "INSERT OR REPLACE INTO FOOD " +
								"VALUES (\""+alimr.getUUID()+"\","
								+alim.getGroup().getId()+","
								+alim.getTypeAliment().getCoef()+",\""
								+alim.getIngredients()+"\","
								+alim.getPrix()+",\""
								+alim.getCategoriePrix()+"\",\""
								+alim.getFamillyBrand()+"\",\""
								+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
								+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\",\""
								+rat.getUUID()+"\","
								+alimr.getQuantite()+",\""
								+alim.getUUID() +"\","
								+alimr.getTarget().getCoef()
								+")";

						stmt.executeUpdate(sql);
						sql = "INSERT OR REPLACE INTO NAMEFOOD ( reffood , lang, value)" +
								"VALUES (\""+alimr.getUUID()+"\",'FR',\""
								+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\")";
						stmt.executeUpdate(sql);
						for(AAEnum enu:AAEnum.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						}

						for(NutrientBase enu:NutrientBase.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 
						for(NutrientLipid enu:NutrientLipid.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 
						for(NutrientMin enu:NutrientMin.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 
						for(NutrientMacro enu:NutrientMacro.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 
						for(NutrientOther enu:NutrientOther.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 
						for(NutrientVitam enu:NutrientVitam.values()) {
							if(alim.isNutrient(enu)) {
								sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood, version, value, date)" +
										"VALUES (\""+enu.getCoef()+"\",\""
										+alimr.getUUID()+"\","
										+1+",\""
										+alim.getNutrient(enu)+"\","
										+"\"2021-12-20\""+")";
								stmt.executeUpdate(sql);}
						} 

					}
				}

			}}catch(SQLException ex) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(ex.getMessage()+" "+sql);

				alert.show();
				System.out.println(ex.getMessage());
			}
		finally {
			close(conn);
		}







	}

	public static void UpdateListConsult(ArrayList<AnimalEv> al) {
		Connection conn=null;
		try {
			conn=	connectAnim();

			for(AnimalEv anim:al) {
				UpdateConsultation( anim,conn) ;
			}}finally {
				close(conn);
			}

	}

	public static void UpdateConsultation(AnimalEv anim, Connection conn) {


		if( anim!=null) {
			boolean isconn=false;
			try {
				isconn=conn==null?false: true;
				conn=conn==null?connectAnim(): conn;
				Statement stmt = conn.createStatement();
				String sql = "";

				int year;
				int monthnum;
				String month="";
				String day=""; 
				int dead;
				int actual;



				dead=anim.isDead()? 1:0;

				sql = "INSERT OR REPLACE INTO ANIMALS " +
						"VALUES (\""+anim.getUUID()+"\",\""

	           		+anim.getNom()+"\","
	           		+dead+",\""
	           		+anim.getId()+"\","
	           		+anim.getSex()+",\""
	           		+anim.getEspece()
	           		+"\",\""
	           		+anim.getNomProprio()+"\",\""
	           		+anim.getDateNaiss().toString()+"\",\""
	           		+anim.getRace()+"\",\""
	           		+anim.getResume() +"\")";
				int i =0;
				stmt.executeUpdate(sql);
				for (WeightDate wd:anim.getListWeight()) {
					sql = "INSERT OR REPLACE INTO Weight " +
							"VALUES (\""+anim.getUUID()+"\",\""

	           		+wd.getDate().toString()+"\","
	           		+wd.getValue()+",\""

	           		+wd.getUUID()+"\")";

					stmt.executeUpdate(sql);

				}

				for (ConsultationEv cons:anim.getList().getListConsult()) {
					i++;

					sql = "INSERT OR REPLACE INTO CONSULTATIONS " +
							"VALUES (\""+cons.getUUID()+"\",\""
							+cons.getDate().toString()+"\",\""
							+cons.getObjet()+"\",\""
							+cons.getObservation()+"\",\""
							+cons.getCRendu()+"\","
							+cons.getPoids()+","
							+cons.getPoidsIdeal()+","
							+cons.getBoisson()+","
							+cons.getTauxMG()+",\""
							+cons.getRefString()+"\","
							+cons.getNewBCS()+",\""
							+	cons.getK1d()+"\","
							+cons.getK1value()+",\""
							+	cons.getK2d()+"\","
							+cons.getK2value()+",\""
							+	cons.getK3d()+"\","
							+cons.getK3value()+",\""
							+	cons.getK4d()+"\","
							+cons.getK4value()+",\""
							+	cons.getK5d()+"\","
							+cons.getK5value()+","
							+cons.getNbPetit()+","
							+cons.getpMere()+","
							+cons.getCoefIntG()+","
							+cons.getCoefIntL()+",\""
							+anim.getUUID()
							+"\", "
							+cons.getMCS() +")";

					stmt.executeUpdate(sql);


					for (SupplementalvariableP s:cons.getSuppVarp()) {
						sql = "INSERT OR REPLACE INTO SupVar " +
								"VALUES (\""+cons.getUUID()+"\","
								+s.getVariable().getUuid()+","
								+s.getValue()+")";
						stmt.executeUpdate(sql);
					}
					sql = "DELETE FROM ReferenceDisease WHERE idCons =\""+cons.getUUID()+"\"";
					stmt.executeUpdate(sql);
					for (String s:cons.getDiseaseRef()) {
						sql = "INSERT OR REPLACE INTO ReferenceDisease " +
								"VALUES (\""+cons.getUUID()+"\",\""
								+s+"\")";
						stmt.executeUpdate(sql);
					}
					ArrayList<Ration> rats=(ArrayList<Ration>) cons.getRationList();

					for(Ration rat:rats) {
						actual=rat.isActual()?1:0;
						sql = "INSERT OR REPLACE INTO RATION " +
								"VALUES (\""+rat.getUUID()+"\",\""
								+cons.getUUID()+"\",\""
								+rat.getNom()+"\","
								+rat.getCoef()+","
								+actual+","
								+rat.getNumber() +","
								+"\"ALL\","
								+"0,"
								+"\"\""
								+")";

						stmt.executeUpdate(sql); 
						for (AlimentRation alimr:rat.getAlimentList()) {
							AlimentEv alim=alimr.getAlim();

							sql = "INSERT OR REPLACE INTO FOOD " +
									"VALUES (\""+alimr.getUUID()+"\","
									+alim.getGroup().getId()+","
									+alim.getTypeAliment().getCoef()+",\""
									+alim.getIngredients()+"\","
									+alim.getPrix()+",\""
									+alim.getCategoriePrix()+"\",\""
									+alim.getFamillyBrand()+"\",\""
									+alim.getGamme()+"\","+alim.getCont().getID()+","+alim.getQuantInt()+","+1+","+"\"2021-12-20\""+",\""
									+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\",\""
									+rat.getUUID()+"\","
									+alimr.getQuantite()+",\""
									+alim.getUUID() +"\","
									+alimr.getTarget().getCoef()
									+")";

							stmt.executeUpdate(sql);
							sql = "INSERT OR REPLACE INTO NAMEFOOD ( reffood , lang, value)" +
									"VALUES (\""+alimr.getUUID()+"\",'FR',\""
									+alim.getNom().replaceAll("[^a-zA-Z0-9 ,]=éèàœ", "")+"\")";
							stmt.executeUpdate(sql);
							for(AAEnum enu:AAEnum.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEAA (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							}

							for(NutrientBase enu:NutrientBase.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEBASE (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 
							for(NutrientLipid enu:NutrientLipid.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUELIPID (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 
							for(NutrientMin enu:NutrientMin.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEMIN (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 
							for(NutrientMacro enu:NutrientMacro.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEMACRO (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 
							for(NutrientOther enu:NutrientOther.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, reffood , version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 
							for(NutrientVitam enu:NutrientVitam.values()) {
								if(alim.isNutrient(enu)) {
									sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, reffood, version, value, date)" +
											"VALUES (\""+enu.getCoef()+"\",\""
											+alimr.getUUID()+"\","
											+1+",\""
											+alim.getNutrient(enu)+"\","
											+"\"2021-12-20\""+")";
									stmt.executeUpdate(sql);}
							} 

						}
					}

				}







			}
			catch(SQLException ex) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(ex.getMessage());

				alert.show();
				System.out.println(ex.getMessage());
			}
			finally {
				if(!isconn) {
					close(conn);}
			}}


	}


	public static ObservableList<Recette> readAllRecette(){
		ObservableList<Recette>list=FXCollections.observableArrayList();
		Connection conn=null;
		Recette rat;
		try {		conn=connectAnim();
		String sql ;


		Statement stmt  = conn.createStatement();
		ResultSet rs ;
		sql = "SELECT UUID, idConsult, name, coef, actual, number, espece, description FROM ration WHERE recette =1";
		rs    = stmt.executeQuery(sql);
		System.out.print("Recette adder");
		// loop through the result set
		while (rs.next()) {
			System.out.print("inrs");
			rat=new Recette( rs.getString("name"),rs.getString("UUID"));

			rat.setEspece(Espece.getEnumFromStringId(rs.getString("espece")))
			;		
			rat.setDescription(rs.getString("description"));
			rat.setAlimentList(readAlimRat(rat.getUUID(), conn));

			list.add(rat);
		}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			close(conn);

		}
		System.out.println("size Recette "+list.size());
		return list;
	}

	public static AnimalEv readAnimal(String UUID)  {
		Connection conn=null;

		AlimentRation alr=null;
		String UUIDcons="";
		AlimentUnif al=null;
		listConsultEv listTrans= new listConsultEv();
		AnimalEv anim=new AnimalEv();
		ConsultationEv cons;
		ResultSet rs;
		if (UUID!=null) {

			try {		conn=connectAnim();

			String sql = "SELECT UUID, name, dead, id, sex, specie, ownerName, birthdate, race, summary FROM ANIMALS WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rsmain    = stmt.executeQuery(sql);

			// loop through the result set
			while (rsmain.next()) {
				anim=new AnimalEv(rsmain.getString("UUID"));
				anim.setNom(rsmain.getString("name"));
				anim.setDead(rsmain.getInt("dead")==1);
				anim.setId(rsmain.getString("id"));
				anim.setSex(rsmain.getInt("sex"));
				anim.setEspece(rsmain.getString("specie"));
				anim.setRace(rsmain.getString("race"));
				anim.setNomProprio(rsmain.getString("ownerName"));

				anim.setDateNaiss(LocalDate.parse(rsmain.getString("birthdate")));
				//anim.setRace(null);
				anim.setResume(rsmain.getString("summary"));
			}

			sql= "SELECT refAnimal, date, value, UUID FROM Weight WHERE refAnimal=\""+ UUID+"\"";
			rsmain    = stmt.executeQuery(sql);
			int i=0;
			// loop through the result set
			while (rsmain.next()) {

				anim.addWeight(new WeightDate(rsmain.getString("UUID"), LocalDate.parse(rsmain.getString("date")) , rsmain.getFloat("value")));

			}



			sql= "SELECT UUID, date, object, observation, cRendu, weight , idealWeight, water, bodyFat, methodAnalysis, BCS, k1Id, k1Value, k2Id, k2Value, k3Id, k3Value, k4Id, k4Value, k5Id, k5Value, nLittle, pAdult, coefGes, coefLact, idAnim, MCS FROM CONSULTATIONS WHERE idAnim=\""+ UUID+"\"";
			rsmain    = stmt.executeQuery(sql);
			// loop through the result set
			while (rsmain.next()) {

				cons=new ConsultationEv(rsmain.getString("UUID"));
				cons.setDate(LocalDate.parse(rsmain.getString("date")));

				cons.setObjet(rsmain.getString("object"));
				cons.setObservation(rsmain.getString("observation"));
				cons.setCRendu(rsmain.getString("cRendu"));
				cons.setPoids(rsmain.getFloat("weight"));
				cons.setPoidsIdeal(rsmain.getFloat("idealWeight"));
				cons.setBoisson(rsmain.getFloat("water"));
				cons.setTauxMG(rsmain.getFloat("bodyFat"));
				cons.setRefString( rsmain.getString("methodAnalysis"));
				cons.setNewBCS(rsmain.getInt("BCS"));
				cons.setK1d(rsmain.getString("k1Id"));
				cons.setK1value(rsmain.getFloat("k1Value"));
				cons.setK2d(rsmain.getString("k2Id"));
				cons.setK2value(rsmain.getFloat("k2Value"));
				cons.setK3d(rsmain.getString("k3Id"));
				cons.setK3value(rsmain.getFloat("k3Value"));
				cons.setK4d(rsmain.getString("k4Id"));
				cons.setK4value(rsmain.getFloat("k4Value"));
				cons.setK5d(rsmain.getString("k5Id"));
				cons.setK5value(rsmain.getFloat("k5Value"));
				cons.setNbPetit(rsmain.getInt("nLittle"));
				cons.setpMere(rsmain.getFloat("pAdult"));
				cons.setCoefIntG(rsmain.getInt("coefGes"));
				cons.setCoefIntL(rsmain.getInt("coefLact"));
				cons.setMCS(rsmain.getInt("MCS"));
				i++;
				anim.addConsult(cons);
				listTrans.addConsult(cons);

			}

			i=0;
			for (ConsultationEv con:listTrans.getListConsult()) {

				for (SupplementalvariableP s:con.getSuppVarp()) {

					sql = "SELECT idCons, idVar, value FROM SupVar WHERE idcons =\""+ con.getUUID() +"\" AND idVar ="+s.getVariable().getUuid();
					rs    = stmt.executeQuery(sql);
					while (rs.next()) {
						s.setValue(rs.getFloat("value"));
					}
				}

				sql = "SELECT idCons, refRef FROM ReferenceDisease WHERE idcons =\""+ con.getUUID() +"\"";
				rs    = stmt.executeQuery(sql);
				while (rs.next()) {
					con.addDiseaseRef(rs.getString("refRef"));
				}
				sql = "SELECT UUID, idConsult, name, coef, actual, number FROM ration WHERE idconsult =\""+ con.getUUID() +"\"";
				rs    = stmt.executeQuery(sql);
				Ration rat;
				// loop through the result set
				while (rs.next()) {
					rat=new Ration(rs.getString("UUID"));

					rat.setNom(rs.getString("name"));
					rat.setCoef(rs.getFloat("coef"));
					rat.setActual(rs.getInt("actual")==1);
					rat.setNumber(rs.getInt("number"));
					rat.setAlimentList(readAlimRat(rat.getUUID(), conn));
					con.addNewRation(rat);

				}
				anim.getList().replaceConsult(con);
				i++;


			}



			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				close(conn);

			}}
		else return anim;
		return anim;


	}

	public static void DeleteWeight(String IDweight, Connection conn)  {



		if (IDweight!=null) {

			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectAnim(): conn;


			String sql1 = "DELETE FROM Weight WHERE UUID =\""+IDweight+"\"";
			Statement stmt  = conn.createStatement();

			stmt.executeUpdate(sql1);



			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if (!isconn) {
					close(conn);}
			}}


	}

	

	   
	       
	

	public static Equation readEquation(String UUID, Connection conn)  {

		Equation al=null;
		if (UUID!=null) {
			al=new Equation(UUID);
			boolean isconn=false;
			String refBiblioStr="";

			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;
			String sql = "SELECT UUID, script, refBiblio,  name, description, speciesRef, kind, consistent,nutrient FROM equation WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);

			// loop through the result set
			while (rs.next()) {

				al.setEquationScript(rs.getString("script"));
				al.setName(rs.getString("name"));
				al.setDescription(rs.getString("description"));
				al.setSpecie(Espece.getEnumFromStringId((rs.getString("speciesRef"))));
				al.setKind(EquationKind.getById(rs.getInt("kind")));
				al.setConsistent(rs.getInt("consistent")==1);
al.setAlllNut((AllNutrient) AllNutrient.values().get(rs.getInt("nutrient")));
				refBiblioStr=rs.getString("refbiblio");
			}
			al.setBib(readBiblio(refBiblioStr,conn ));

			sql = "SELECT refEquation, VariableKind FROM SupplementVariable WHERE refEquation =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.addVariable(VariableKind.getById(rs.getInt("VariableKind")));
			}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}
		else return al;
		return al;
	}



	public static ObservableList<EquationConsP> getEquationList(Connection conn, String cond)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;


		String sql = "SELECT UUID FROM equation"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set
		List<String> strL=new ArrayList<String>();

		while (rs.next()) {
			strL.add(rs.getString("UUID"));
		}

		ObservableList<EquationConsP>	list= FXCollections.observableArrayList();
		for(String str:strL) {
			list.add(new EquationConsP(readEquation(str, conn)));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}

	public static ObservableList<BiblioP> getBiblioList(Connection conn, String cond)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;


		String sql = "SELECT UUID FROM Biblio"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set
		List<String> strL=new ArrayList<String>();

		while (rs.next()) {
			strL.add(rs.getString("UUID"));
		}

		ObservableList<BiblioP>	list= FXCollections.observableArrayList();
		for(String str:strL) {
			list.add(new BiblioP(readBiblio(str, conn)));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}

	public static ObservableList<AdjustSaveEv> getMethodList(Connection conn, String cond)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;


		String sql = "SELECT UUID FROM method"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set
		List<String> strL=new ArrayList<String>();

		while (rs.next()) {
			strL.add(rs.getString("UUID"));
		}

		ObservableList<AdjustSaveEv>	list= FXCollections.observableArrayList();
		for(String str:strL) {
			list.add(readMethod(str, conn));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}

	public static void UpdateEquation(Equation al, Connection  conn) {
		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;

		
		if(conn!=null) {
			Statement stmt = conn.createStatement();
			String sql = "";

			sql= "DELETE FROM SupplementVariable WHERE refEquation =\""+al.getUUID()+"\"";
			stmt.executeUpdate(sql);

			sql = "INSERT OR REPLACE INTO equation " +
					"VALUES (\""+al.getUUID()+"\",\""
					+al.getEquationScript()+"\",\""
					+al.getBib().getUUID()+"\",\""
					+al.getName()+"\",\""
					+al.getDescription()+"\",\""
					+al.getSpecie().getUUID()+"\","
					+al.getKind().getUuid()+","+(al.isConsistent()?1:0)+","
							+al.getAlllNut().getID()+ ")";

			stmt.executeUpdate(sql);
			for (VariableKind kv:al.getVar()) {
				sql = "INSERT OR REPLACE INTO SupplementVariable " +
						"VALUES (\""+al.getUUID()+"\","
						+kv.getUuid()+")";
				stmt.executeUpdate(sql);
			}
		}

		}
		catch(SQLException ex) {

			System.out.println(ex.getMessage());
		}
		finally {
			if(!isconn) {
				close(conn);
			}}

	}
	public static void UpdateMethod(AdjustSaveEv al, Connection  conn) {
		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;

		
		if(conn!=null) {
			Statement stmt = conn.createStatement();
			String sql = "";



			sql = "INSERT OR REPLACE INTO method " +
					"VALUES (\""+al.getUUID()+"\",\""
					+al.getName()+"\",\""
					+al.getEsp().getUUID()+"\",\""
					+al.getDescription()+"\")";

			stmt.executeUpdate(sql);
			int n=0;
			for (TargetDefinitionEv kv:al.getAll()) {
				sql = "INSERT OR REPLACE INTO targetMethod " +
						"VALUES (\""+kv.getUUID()+"\",\""
						+al.getUUID()+"\","
						+n+","
						+kv.getTarg().getCoef()+","
						+kv.getValue()+","
						+kv.getUre().getID()+","
						+kv.getPercentCompletion()+","
						+kv.getPas()+		
						")";
				stmt.executeUpdate(sql);
				n++;
			}
		}

		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {
			if(!isconn) {
				close(conn);
			}}

	}
	public static void UpdateMethod(AdjustSave al, Connection  conn) {
		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;

		
		if(conn!=null) {
			Statement stmt = conn.createStatement();
			String sql = "";



			sql = "INSERT OR REPLACE INTO method " +
					"VALUES (\""+al.getUUID()+"\",\""
					+al.getName()+"\",\"CH\",\" \")";

			stmt.executeUpdate(sql);
			int n=0;
			for (TargetDefinition kv:al.getAll()) {
				sql = "INSERT OR REPLACE INTO targetMethod " +
						"VALUES (\""+kv.getUUID()+"\",\""
						+al.getUUID()+"\","
						+n+","
						+kv.getTarg().getCoef()+","
						+kv.getValue()+","
						+0+","
						+kv.getPercentCompletion()+","
						+kv.getPas()+		
						")";
				stmt.executeUpdate(sql);
				n++;
			}
		}

		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {
			if(!isconn) {
				close(conn);
			}}

	}
	public static void UpdateBiblio(BiblioRef al, Connection conn) {
		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;

		
		if(conn!=null) {
			Statement stmt = conn.createStatement();
			String sql = "";


			sql = "INSERT OR REPLACE INTO Biblio " +
					"VALUES (\""+al.getUUID()+"\",\""
					+al.getFirstAuthor()+"\","
					+al.getYear()+",\""
					+al.getCompleteRef()+"\",\""
					+al.getComment()+"\","
					+al.getConsistent()+")";
		System.out.println ("before sql update bibli");
			stmt.executeUpdate(sql);

		}

		}
		catch(SQLException ex) {

			System.out.println(ex.getMessage());
		}
		finally {
			if(!isconn) {
				close(conn);
			}}

	}
	/**
	 * @param args the command line arguments
	 */
	public static void DeleteEquation(String UUID, Connection conn)  {


		if (UUID!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;


			String sql1 = "DELETE FROM equation WHERE UUID =\""+UUID+"\"";
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM SupplementVariable WHERE refEquation =\""+UUID+"\"";
			stmt.executeUpdate(sql1);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}


	}
	public static void DeleteMethod(String UUID, Connection conn)  {


		if (UUID!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;


			String sql1 = "DELETE FROM method WHERE UUID =\""+UUID+"\"";
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate(sql1);

			sql1 = "DELETE FROM targetMethod WHERE refMethod =\""+UUID+"\"";
			stmt.executeUpdate(sql1);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}


	}
	public static void DeleteBiblio(String UUID, Connection conn)  {


		if (UUID!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;


			String sql1 = "DELETE FROM Biblio WHERE UUID =\""+UUID+"\"";
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate(sql1);



			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}


	}

	public static void DeleteReference(String UUID, Connection conn)  {


		if (UUID!=null) {
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;


			String sql1 = "DELETE FROM dataRef WHERE UUID =\""+UUID+"\"";
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEAA WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEANA WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEBASE WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUELIPID WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEMACRO WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEMIN WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM VALUEOTHER WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM coef WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);
			sql1 = "DELETE FROM speReqEq WHERE refRef =\""+UUID+"\"";

			stmt.executeUpdate(sql1);


			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}


	}

	public static BiblioRef readBiblio(String UUID, Connection conn)  {

		BiblioRef al=null;
		if (UUID!=null) {
			al=new BiblioRef(UUID);
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;
			String sql = "SELECT UUID, fAuthor, year,  fullRef, comments, consistent FROM Biblio WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);

			// loop through the result set
			while (rs.next()) {

				al.setFirstAuthor(rs.getString("fAuthor"));
				al.setYear(rs.getString("year"));
				al.setCompleteRef(rs.getString("fullRef"));					
				al.setComment(rs.getString("comments"));
				al.setConsistent(rs.getInt("consistent"));
			}


			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}
		else return al;
		return al;
	}


	public static AdjustSaveEv readMethod(String UUID, Connection conn)  {

		AdjustSaveEv al=null;
		if (UUID!=null) {
			al=new AdjustSaveEv(UUID);
			boolean isconn=false;
			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;
			String sql = "SELECT UUID, name, species,description FROM method WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);

			// loop through the result set
			while (rs.next()) {

				al.setName(rs.getString("name"));
				al.setDescription(rs.getString("description"));
				al.setEsp(Espece.getEnumFromStringId(rs.getString("species")));

			}
			ObservableList<TargetDefinitionEv> olt=FXCollections.observableArrayList();
			for (int i=0; i<9; i++) {

				sql = "SELECT UUID, kind, value, unit, percent, measure FROM targetMethod WHERE ord ="+i+"  AND refMethod =\""+ UUID+"\"";

				stmt  = conn.createStatement();
				rs    = stmt.executeQuery(sql);

				// loop through the result set
				while (rs.next()) {

					olt.add(new TargetDefinitionEv(
							rs.getString("UUID"), 
							targetAdjust.getByCoef(rs.getInt("kind")),
							rs.getFloat("value"),
							UnitReqEnum.getById(rs.getInt("unit")),
							rs.getFloat("percent"),
							rs.getFloat("measure")
							));

				}
			}
			al.setList(olt);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}
		else return al;
		return al;
	}


	public static ObservableList<ReferenceP> getReferenceList(Connection conn, String cond)  {
		boolean isconn=false;


		try {	isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;


		String sql = "SELECT UUID FROM dataRef"+ cond;


		Statement stmt  = conn.createStatement();
		ResultSet rs    = stmt.executeQuery(sql);

		// loop through the result set
		List<String> strL=new ArrayList<String>();

		while (rs.next()) {
			strL.add(rs.getString("UUID"));
		}

		ObservableList<ReferenceP>	list= FXCollections.observableArrayList();
		for(String str:strL) {
			list.add(new ReferenceP(readReference(str, conn)));
		}

		return list;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(e.getMessage());

			alert.show();
		}finally {
			if(!isconn) {
				close(conn);}
		}
		return null;


	}



	public static ReferenceEv readReference(String UUID, Connection conn)  {

		ReferenceEv al=null;
		if (UUID!=null) {
			al=new ReferenceEv(UUID);
			boolean isconn=false;
			String[] ref=new String[5];
			String BWRef="";
			String SERRef="";
			String DEcomRef="";
			String DErawRef="";


			try {	isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;
			ObservableList<BiblioP> bib=getBiblioList(conn, "");

			String sql = "SELECT UUID, "
					+ "name, description,"
					+ " BWeqRef, SERName, SERRef,DEcomRef, DErawRef,"
					+ " k1Name, k1Ref,"
					+ " k2Name, k2Ref,"
					+ " k3Name, k3Ref,"
					+ " k4Name, k4Ref,"
					+ "k5Name, k5Ref,"
					+ " specie,"
					+ "consistent,"
					+ "disease FROM dataRef WHERE UUID =\""+ UUID+"\"";


			Statement stmt  = conn.createStatement();
			ResultSet rs    = stmt.executeQuery(sql);

			// loop through the result set
			while (rs.next()) {


				al.setName(rs.getString("name"));
				al.setDescription(rs.getString("description"));
				al.setNameEnergy(rs.getString("SERName"));
				BWRef=rs.getString("BWeqRef");
				SERRef=rs.getString("SERRef");
				DEcomRef=rs.getString("DEcomRef");
				DErawRef=rs.getString("DErawRef");
				al.setNamek1(rs.getString("k1Name"));
				al.setNamek2(rs.getString("k2Name"));
				al.setNamek3(rs.getString("k3Name"));
				al.setNamek4(rs.getString("k4Name"));
				al.setNamek5(rs.getString("k5Name"));
				al.setConsistent(rs.getInt("Consistent"));
				al.setDisease(rs.getInt("disease")==1);



				al.setSpecies(Espece.getEnumFromString(rs.getString("specie")));


			}
			if(!SERRef.isBlank()) {
				al.setBEEqu(readEquation(SERRef,conn));}
			if(!BWRef.isBlank()) {
				al.setBWEqu(readEquation(BWRef,conn));}
			if(!DEcomRef.isBlank()) {
				al.setDEcomEqu(readEquation(DEcomRef,conn));}
			if(!DErawRef.isBlank()) {
				al.setDErawEqu(readEquation(DErawRef,conn));}

			ObservableList<CoefP>list;
			for(int i=0;i<5;i++) {
				list=FXCollections.observableArrayList();
				sql = "SELECT coefName, value, UUID, groupUUID FROM coef WHERE groupUUID  ="+i+" AND refRef =\""+UUID+"\"";
				rs    = stmt.executeQuery(sql);
				// loop through the result set

				while (rs.next()) {
					CoefP coef=new CoefP(rs.getString("coefName"), rs.getFloat("value"), i, rs.getString("UUID"));
					list.add(coef);

				}
				if (list.isEmpty()) {
					list.add(new CoefP("Normal", 1F, i));
				}
				al.setModk(i,list);
			}


			sql = "SELECT  kind, kindrelative, value, unitkind, refBiblio FROM VALUEAA WHERE refRef =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), AAEnum.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEANA WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientAnalysis.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kindrelative , kind, unitkind,value, refBiblio FROM VALUEBASE WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientBase.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUELIPID WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientLipid.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEMACRO WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientMacro.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEMIN WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientMin.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEVITAM WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("VALUE"), NutrientVitam.getByCoef(rs.getInt("KIND")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEOTHER WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("value"), NutrientOther.getByCoef(rs.getInt("kind")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			sql = "SELECT kind, kindrelative, value, unitkind, refBiblio FROM VALUEOTHER WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				al.setNutrient(rs.getFloat("value"), NutrientOther.getByCoef(rs.getInt("kind")),Reflevel.getById(rs.getInt("kindrelative")), UnitReqEnum.getById(rs.getInt("unitKind")), getByUUID(bib,rs.getString("refBiblio")));
			}
			ArrayList<String> uidlist=new ArrayList<String>();
			sql = "SELECT refEq FROM speReqEq WHERE refRef  =\""+ UUID+"\"";
			rs    = stmt.executeQuery(sql);
			// loop through the result set
			while (rs.next()) {
				uidlist.add(rs.getString("refEq"));
		
				}
			for(String uid:uidlist) {
				al.addNutEqu(DataConnector.readEquation(uid, conn));
	
			}
			
		
			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}finally {
				if(!isconn) {
					close(conn);}
			}}
		else return al;

		return al;
	}
	public static void UpdateModificator(ObservableList<CoefP>ol, String refRef, Connection conn) {

		if (!ol.isEmpty()){
			boolean isconn=false;

			try {isconn=conn==null?false: true;
			conn=conn==null?connectReferences(): conn;

			if(conn!=null) {
				Statement stmt = conn.createStatement();

				String sql = "";

				sql = "DELETE FROM coef WHERE groupUUID  ="+ol.get(0).getGroupUUID()+" AND refRef= \""+refRef+"\"";

				stmt.executeUpdate(sql);
				for (CoefP co:ol) {
					sql = "INSERT OR REPLACE INTO coef ( UUID, coefName , value, groupUUID, refRef)" +
							"VALUES (\""+co.getUUID()+"\",\""
							+co.getDescription()+"\","
							+co.getCoef()+",\""
							+co.getGroupUUID()+"\",\""
							+ refRef+"\")";
					stmt.executeUpdate(sql);

				}

			}	
			}
			catch(SQLException ex) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(ex.getMessage());

				alert.show();
				System.out.println(ex.getMessage());
			}
			finally {
				if(!isconn) {
					close(conn);}
			}
		}
	}

	public static void updateListReference(ObservableList<ReferenceP>ol) {
		Connection conn=connectReferences();
		if(conn!=null) {
			for (ReferenceP r:ol) {
				UpdateReference(r.getReference(), conn);
			}
			
				System.out.println("CloseDB");
				close(conn);
		}

	}
	public static void updateListReference(ArrayList<ReferenceEv>ol, Connection conn) {
		boolean isconn=false;
		ArrayList<BiblioRef>bib=new ArrayList<BiblioRef>();
		ArrayList<Equation>equ=new ArrayList<Equation>();
		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		
	

			for (ReferenceEv r:ol) {
				bib=addUniqBiblioRef(r, bib);
				equ=addUniqEquation(r, equ);
			}
			System.out.println("before biliref");
			updateListBiblioRef((ObservableList<BiblioRef>)FXCollections.observableArrayList(bib), conn);
			updateListEquations((ObservableList<Equation>)FXCollections.observableArrayList(equ), conn);
			for (ReferenceEv r:ol) {
				System.out.println(r.getName());
				UpdateReference(r, conn);}

			
			} finally {
				if(!isconn) {
					close(conn);}
			}
			
		

	}

	public static void updateListBiblio(ObservableList<BiblioP>ol, Connection conn) {

		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		if(conn!=null) {
			for (BiblioP r:ol) {
				UpdateBiblio(r.getBiblio(), conn);
			}
			}}finally {
				if(!isconn) {
					close(conn);}
			}

	}
	public static void updateListBiblioRef(ObservableList<BiblioRef>ol, Connection conn) {

		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		if(conn!=null) {
			for (BiblioRef r:ol) {
				System.out.println("before UpBibli");
				UpdateBiblio(r, conn);
			}
			}}finally {
				if(!isconn) {
					close(conn);}
			}

	}
	public static void updateListMethod(ObservableList<AdjustSaveEv>ol) {
		Connection conn=connectReferences();
		if(conn!=null) {
			for (AdjustSaveEv r:ol) {
				UpdateMethod(r, conn);
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}}

	}
	public static void updateListMethod(ListAdjust ol) {
		Connection conn=connectReferences();
		if(conn!=null) {
			for (AdjustSave r:ol.getList()) {
				UpdateMethod(new AdjustSaveEv(r), conn);
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}}

	}
	public static void updateListMethod(ArrayList<AdjustSaveEv> ol) {
		Connection conn=connectReferences();
		if(conn!=null) {
			for (AdjustSaveEv r:ol) {
				UpdateMethod(r, conn);
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(e.getMessage());

				alert.show();
			}}

	}

	public static void updateListEquation(ObservableList<EquationConsP>ol, Connection conn) {

		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		if(conn!=null) {
			for (EquationConsP r:ol) {
				UpdateEquation(r.getEquation(), conn);
			}
		}}finally {
				if(!isconn) {
					close(conn);}
			}

	}
	public static void updateListEquations(ObservableList<Equation>ol, Connection conn) {

		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		if(conn!=null) {
			for (Equation r:ol) {
				UpdateEquation(r, conn);
			}
	}}finally {
				if(!isconn) {
					close(conn);}
			}

	}


	public static void UpdateReference(ReferenceEv ref, Connection conn) {
		boolean isconn=false;

		try {isconn=conn==null?false: true;
		conn=conn==null?connectReferences(): conn;
		if(conn!=null) {
			Statement stmt = conn.createStatement();
			String sql = "";
			int numAlim=0;
			int disease=(ref.isDisease())?1:0;
			UpdateModificator(ref.getModk1(),ref.getUUID(), conn);
			UpdateModificator(ref.getModk2(),ref.getUUID(), conn);
			UpdateModificator(ref.getModk3(),ref.getUUID(), conn);
			UpdateModificator(ref.getModk4(), ref.getUUID(),conn);
			UpdateModificator(ref.getModk5(), ref.getUUID(),conn);
			sql = "INSERT OR REPLACE INTO dataRef " +
					"VALUES (\""+ref.getUUID()+"\",\""
					+ref.getName()+"\",\""
					+ref.getDescription()+"\","
					+(ref.isDisease()?1:0)+",\""
					+ref.getBWEqu().getUUID()+"\",\""
					+ref.getNameEnergy()+"\",\""
					+ref.getBEEqu().getUUID()+"\",\""
					+ref.getDEcomEqu().getUUID()+"\",\""
					+ref.getDErawEqu().getUUID()+"\",\""
					+ref.getNamek1()+"\","
					+0+",\""
					+ref.getNamek2()+"\","
					+1+",\""
					+ref.getNamek3()+"\","
					+2+",\""
					+ref.getNamek4()+"\",\""
					+3+"\",\""
					+ref.getNamek5()+"\",\""
					+4+"\",\""
					+ref.getSpecies()+"\","
					+ ref.getConsistent()+
					")";

			stmt.executeUpdate(sql);


			sql = "DELETE FROM speReqEq WHERE refRef =\""+ref.getUUID()+"\"";
			stmt.executeUpdate(sql);
			for (Equation eq: ref.getNutEqu()) {
				System.out.println("inr ref esdzedz");
				sql = "INSERT OR REPLACE INTO speReqEq ( refRef, refEq)" +
						"VALUES ( \""+
						ref.getUUID()+"\",\""+
						eq.getUUID()+"\")";
				
				stmt.executeUpdate(sql);
			}

			stmt.executeUpdate(sql);

			for(AAEnum enu:AAEnum.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEAA (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEAA  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				}
			}

			for(NutrientBase enu:NutrientBase.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEBASE (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);
					}else {
						sql="DELETE FROM VALUEBASE WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
						stmt.executeUpdate(sql);
					}
				} 
			}	
			for(NutrientLipid enu:NutrientLipid.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUELIPID(kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUELIPID  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
			}	System.out.print("A");
			for(NutrientMin enu:NutrientMin.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEMIN (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEMIN  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
			}	
			for(NutrientMacro enu:NutrientMacro.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEMACRO(kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEMACRO  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
			}	
			for(NutrientOther enu:NutrientOther.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";

						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEOTHER  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
			}System.out.print(4);
			for(NutrientVitam enu:NutrientVitam.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)&ref.getNutrient(enu, rl)>0) {	
						sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";
						System.out.print(1);
						stmt.executeUpdate(sql);}else {
							sql="DELETE FROM VALUEVITAM  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
				} 
			}System.out.print(5);
			for(NutrientAnalysis enu:NutrientAnalysis.values()) {
				for(Reflevel rl:Reflevel.values()) {
					if(ref.isNutrient(enu, rl)& !Double.isNaN(ref.getNutrient(enu, rl))) {	
						sql = "INSERT OR REPLACE INTO VALUEANA (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
								"VALUES ("
								+enu.getCoef()+","
								+rl.getCoef()+",\""
								+ref.getNutrientBib(enu, rl).getUUID()+"\","
								+1+","
								+ref.getNutrient(enu, rl)+","
								+"\"2021-12-20\","
								+ref.getNutrientUnit(enu, rl)+",\""
								+ref.getUUID()
								+"\")";
						
						stmt.executeUpdate(sql);
					}else {
							
							sql="DELETE FROM VALUEANA  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
						
							stmt.executeUpdate(sql);
						}
				} 
			}	

		}
System.out.println("ENDER");
		}
		catch(SQLException ex) {
			
			System.out.println(ex.getMessage());
		}
		finally {
			if(!isconn) {
				close(conn);}
		}
	}



	
	public static void UpdateReference(Reference ref) {
		Connection conn=null;
		try {
			conn=connectReferences();
			if(conn!=null) {
				Statement stmt = conn.createStatement();
				String sql = "";
				int numAlim=0;
				int disease=0;

				sql = "INSERT OR REPLACE INTO dataRef " +
						"VALUES (\""+ref.getUUID()+"\",\""
						+ref.getNom()+"\",\""
						+ref.getDescriprion()+"\","
						+disease+",\""
						+"\",\""
						+"\",\""
						+"\",\""
						+"\",\""
						+"\",\""
						+"Breed"+"\",\""
						+"\",\""
						+"Activity"+"\",\""
						+"\",\""
						+"Physiology"+"\",\""
						+"\",\""
						+"Pathology"+"\",\""
						+"\",\""
						+"Other"+"\",\""
						+"\",\""
						+ref.getEspece()+"\","
						+ 1+")";

				stmt.executeUpdate(sql);


				for(AAEnum enu:AAEnum.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEAA (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEAA  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					}
				}

				for(NutrientBase enu:NutrientBase.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEBASE (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);
						}else {
							sql="DELETE FROM VALUEBASE WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
							stmt.executeUpdate(sql);
						}
					} 
				}	
				for(NutrientLipid enu:NutrientLipid.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUELIPID(kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUELIPID  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}	
				for(NutrientMin enu:NutrientMin.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEMIN (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEMIN  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}	
				for(NutrientMacro enu:NutrientMacro.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEMACRO(kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEMACRO  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}	
				for(NutrientOther enu:NutrientOther.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEOTHER (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEOTHER  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}	
				for(NutrientVitam enu:NutrientVitam.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEVITAM (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEVITAM  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}
				for(NutrientAnalysis enu:NutrientAnalysis.values()) {
					for(Reflevel rl:Reflevel.values()) {
						if(ref.isNutrient(enu, rl)) {
							sql = "INSERT OR REPLACE INTO VALUEANA (kind, kindrelative, refBiblio, version, value, date, unitKind, refRef)" +
									"VALUES ("
									+enu.getCoef()+","
									+rl.getCoef()+",\""
									+ref.getNutrientBib(enu, rl)+"\","
									+1+","
									+ref.getNutrient(enu, rl)+","
									+"\"2021-12-20\","
									+0+",\""
									+ref.getUUID()
									+"\")";

							stmt.executeUpdate(sql);}else {
								sql="DELETE FROM VALUEANA  WHERE kind="+enu.getCoef()+" AND kindrelative="+rl.getCoef()+" AND refRef =\"" +ref.getUUID()+"\"";
								stmt.executeUpdate(sql);
							}
					} 
				}	

			}

		}
		catch(SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}
		finally {
		}
	}


	/**
	 * @param args the command line arguments
	 */
	public static void close(Connection conn) {
		try {
			System.out.println("DB close");
			if (conn != null ) {
				conn.close();
			}
		} catch (SQLException ex) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(ex.getMessage());

			alert.show();
			System.out.println(ex.getMessage());
		}}

	private static BiblioRef getByUUID(ObservableList<BiblioP>l, String UUID) {
		for (BiblioP e:l) {
			if (e.getBiblio().getUUID().equals(UUID)) {
				return e.getBiblio();
			}
		}
		return new BiblioRef();
	}
	public static String getOsName()
	{
		String OS = System.getProperty("os.name"); 
		return OS;
	}
	public static boolean isWindows()
	{
		return true;
//return getOsName().startsWith("Windows");
	}


	public static ArrayList<BiblioRef> addUniqBiblioRef(ReferenceEv re, ArrayList<BiblioRef> br){
		for (BiblioRef b:re.getAllBibli()) {
			
			if (!br.contains(b)) {
				br.add(b);
			}
		}
		return br;
	}


	public static  ArrayList<Equation> addUniqEquation(ReferenceEv re, ArrayList<Equation> br){
		for (Equation b:re.getAllEquation()) {
			if (!br.contains(b)) {
				br.add(b);
			}
		}
		return br;
	}
	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			
			if (columnName.equals(rsmd.getColumnName(x))) {
				
				return true;
			}
		}
		return false;
	}
}

