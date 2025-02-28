package application;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBcreate {

	
	public static void CreateAnimalDB(Connection conn) {
		   Statement stmt;
		   String sql ;
		try {
			stmt = conn.createStatement();

	          
	           sql = "CREATE TABLE \"ANIMALS\" ("
	           		+ "\"UUID\"	TEXT NOT NULL UNIQUE,"
	           		+ "\"name\"	TEXT NOT NULL,"
	           		+ "	        			\"dead\"	INTEGER,	        			\"id\"	TEXT NOT NULL,"
	        					+ "	        			\"sex\"	INTEGER,	        "
	        					+ "			\"specie\"	INTEGER,	        "
	        					+ "			\"ownerName\"	TEXT,	        "
	        					+ "			\"birthdate\"	TEXT,"
	        	+"	\"race\"	TEXT,	        	"
	        	+ "		\"summary\"	TEXT,"
	        	+"		PRIMARY KEY(\"UUID\")"
	        	+"	) WITHOUT ROWID";

	        			
	        			
	         stmt.executeUpdate(sql);
	         
	     	stmt = conn.createStatement();

	           sql = "CREATE TABLE \"Breed\" (\n"
	           		+ "	\"ID\"	TEXT UNIQUE,\n"
	           		+ "	\"refSpecie\"	TEXT,\n"
	           		+ "	PRIMARY KEY(\"ID\")"
	           		+ ") WITHOUT ROWID";
	
	         stmt.executeUpdate(sql);
	         
	         
	     	stmt = conn.createStatement();
	        
	           sql = "CREATE TABLE CONSULTATIONS (UUID TEXT NOT NULL UNIQUE, date TEXT NOT NULL, object TEXT, observation TEXT, cRendu TEXT, weight REAL, idealWeight REAL, water REAL, bodyFat REAL, methodAnalysis TEXT, BCS INTEGER, k1Id TEXT, k1Value REAL, k2Id TEXT, k2Value REAL, k3Id TEXT, k3Value REAL, k4Id TEXT, k4Value REAL, k5Id TEXT, k5Value REAL, nLittle INTEGER, pAdult REAL, coefGes INTEGER, coefLact INTEGER, idAnim TEXT NOT NULL, MCS INTEGER, PRIMARY KEY (UUID)) WITHOUT ROWID";
	
	         stmt.executeUpdate(sql);
	         
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"ESPECEFOOD\"  (\n"
	           		+ "	\"reffood\"	INTEGER,\n"
	           		+ "	\"value\"	INTEGER,\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"Espece\" (\n"
	           		+ "	\"ID\"	TEXT UNIQUE,\n"
	           		+ "	\"feedRegimen\"	TEXT,\n"
	           		+ "	PRIMARY KEY(\"ID\")\n"
	           		+ ") WITHOUT ROWID";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"EspeceName\" (\n"
	           		+ "	\"ID\"	TEXT,\n"
	           		+ "	\"LANG\"	TEXT,\n"
	           		+ "	\"Value\"	TEXT,\n"
	           		+ "	UNIQUE(\"ID\",\"LANG\")\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE FOOD (UUID TEXT NOT NULL UNIQUE, groupAlim INTEGER, typeAlim INTEGER, ingredients TEXT, price REAL, categPrice TEXT, brand TEXT, gamme TEXT, unitPres INTEGER, quantityPres REAL, version INTEGER, date TEXT, nameDef TEXT, RefRation TEXT, quantity REAL, RefAlimUnif TEXT, refTarget INTEGER DEFAULT (1), FOREIGN KEY (RefRation) REFERENCES RATION (UUID), PRIMARY KEY (UUID)) WITHOUT ROWID";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"INDICATION\" (\n"
	           		+ "	\"reffood\"	INTEGER,\n"
	           		+ "	\"value\"	INTEGER,\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"NAMEFOOD\" (\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"lang\"	TEXT,\n"
	           		+ "	\"VALUE\"	TEXT,\n"
	           		+ "	UNIQUE(\"reffood\",\"lang\"),\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE RATION (UUID TEXT NOT NULL UNIQUE, idConsult TEXT, name TEXT, coef REAL, actual INTEGER NOT NULL, number INTEGER, espece TEXT NOT NULL DEFAULT \"ALL\", recette INTEGER NOT NULL DEFAULT (0), description TEXT, PRIMARY KEY (UUID))";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE ReferenceDisease (idCons TEXT REFERENCES CONSULTATIONS (UUID), refRef TEXT, UNIQUE (idCons, refRef))";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"SupVar\" ( \"IdCons\" TEXT, \"IdVar\" INTEGER, \"value\" NUMERIC, UNIQUE(\"IdCons\",\"IdVar\"), FOREIGN KEY(\"IdCons\") REFERENCES \"CONSULTATIONS\"(\"UUID\") )";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEAA\" ( \"ID\" INTEGER NOT NULL UNIQUE, \"kind\" INTEGER NOT NULL, \"reffood\" TEXT NOT NULL, \"version\" INTEGER, \"value\" REAL, \"date\" TEXT, UNIQUE(\"kind\",\"reffood\"), PRIMARY KEY(\"ID\" AUTOINCREMENT), FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\") )";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEBASE\" ( \"ID\" INTEGER NOT NULL UNIQUE, \"kind\" INTEGER NOT NULL, \"reffood\" TEXT NOT NULL, \"version\" INTEGER, \"value\" REAL, \"DATE\" TEXT, PRIMARY KEY(\"ID\" AUTOINCREMENT), UNIQUE(\"reffood\",\"kind\") )";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUELIPID\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEMACRO\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEMIN\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEOTHER\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"VALUEVITAM\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE \"Weight\" (\n"
	           		+ "	\"refAnimal\"	TEXT,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"UUID\"	TEXT UNIQUE,\n"
	           		+ "	PRIMARY KEY(\"UUID\")\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE TABLE breedName (refBreed TEXT, lang TEXT, value TEXT)";
	
	         stmt.executeUpdate(sql);
	        
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"AlimIndex\" ON \"FOOD\" (\n"
	           		+ "	\"RefRation\"	ASC,\n"
	           		+ "	\"UUID\"\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX CONSINDEX ON CONSULTATIONS (\"idAnim\", \"UUID\")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"RATIONINDEX\" ON \"RATION\" (\n"
	           		+ "	\"idConsult\"	ASC,\n"
	           		+ "	\"UUID\"\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"VLipidI\" ON \"VALUELIPID\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"animIndex\" ON \"ANIMALS\" (\n"
	           		+ "	\"UUID\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"breedindex\" ON \"Breed\" (\n"
	           		+ "	\"ID\"	ASC,\n"
	           		+ "	\"refSpecie\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"indexWeight\" ON \"Weight\" (\n"
	           		+ "	\"refAnimal\"	ASC,\n"
	           		+ "	\"UUID\"\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX nameBreedIndex ON breedName (\"refBreed\" ASC)";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vAAin\" ON \"VALUEAA\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vBaseIndex\" ON \"VALUEBASE\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vMacroI\" ON \"VALUEMACRO\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vOtherI\" ON \"VALUEOTHER\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vVitamI\" ON \"VALUEVITAM\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	         stmt.executeUpdate(sql);
	         stmt = conn.createStatement();
		        
	           sql = "CREATE INDEX \"vmlinI\" ON \"VALUEMIN\" (\n"
	           		+ "	\"reffood\"	ASC\n"
	           		+ ")";
	
	      
	         
	         
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}};
	   }
	   
	public static void CreateFoodDB(Connection conn) {
		   Statement stmt;
		   String sql ;
		try {
			stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"ESPECE\" (\n"
	           		+ "	\"reffood\"	TEXT,\n"
	           		+ "	\"value\"	TEXT,\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"dataDef\" (\n"
		           		+ "	\"UUID\"	TEXT NOT NULL,\n"
		           		+ "	\"sNAME\"	TEXT,\n"
		           		+ "	\"compNAME\"	TEXT,\n"
		           		+ "	PRIMARY KEY(\"UUID\")\n"
		           		+ ")";    
		           stmt.executeUpdate(sql);
		           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"FOOD\" (\n"
	           		+ "	\"UUID\"	TEXT NOT NULL UNIQUE,\n"
	           		+ "	\"groupAlim\"	INTEGER,\n"
	           		+ "	\"typeAlim\"	INTEGER,\n"
	           		
	           		+ "	\"ingredients\"	TEXT,\n"
	           		+ "	\"price\"	REAL,\n"
	           		+ "	\"categPrice\"	TEXT,\n"
	           		+ "	\"brand\"	TEXT,\n"
	           		+ "	\"gamme\"	TEXT,\n"
	           		+ "	\"unitPres\"	INTEGER,\n"
	           		+ "	\"quantityPres\"	REAL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"nameDef\"	TEXT,\n"
	           		+ "	\"consistent\"	INTEGER NOT NULL DEFAULT 1,\n"
	           		+ "	\"DataB\" 	TEXT,\n"
	           		+ "	FOREIGN KEY(\"DataB\") REFERENCES \"dataDef\"(\"UUID\")\n"
	           		+ "	PRIMARY KEY(\"UUID\")\n"
	           		+ ") WITHOUT ROWID";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"INDICATION\" (\n"
	           		+ "	\"reffood\"	TEXT,\n"
	           		+ "	\"value\"	INTEGER,\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"NAME\" (\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"lang\"	TEXT,\n"
	           		+ "	\"VALUE\"	TEXT,\n"
	           		+ "	UNIQUE(\"reffood\",\"lang\"),\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"UnitDef\" (\n"
	           		+ "	\"ID\"	INTEGER UNIQUE,\n"
	           		+ "	\"refIDPrimary\"	INTEGER,\n"
	           		+ "	\"Name\"	TEXT,\n"
	           		+ "	\"ToPrimary\"	REAL,\n"
	           		+ "	\"specialUnit\"	TEXT,\n"
	           		+ "	PRIMARY KEY(\"ID\"),\n"
	           		+ "	FOREIGN KEY(\"refIDPrimary\") REFERENCES \"UnitDef\"(\"ID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEAA\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	FOREIGN KEY(\"reffood\") REFERENCES \"FOOD\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEBASE\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	UNIQUE(\"reffood\",\"kind\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUELIPID\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEMACRO\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"Field7\"	INTEGER,\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEMIN\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEOTHER\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEVITAM\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"reffood\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"DATE\"	TEXT,\n"
	           		+ "	\"RefUnit\"	INTEGER,\n"
	           		+ "	UNIQUE(\"kind\",\"reffood\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT)\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEAAINDEX\" ON \"VALUEAA\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEBASEINDEX\" ON \"VALUEBASE\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUELIPIDINDEX\" ON \"VALUELIPID\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEMACROINDEX\" ON \"VALUEMACRO\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEMININDEX\" ON \"VALUEMIN\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEOTHERINDEX\" ON \"VALUEOTHER\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE INDEX \"VALUEVITAMINDEX\" ON \"VALUEVITAM\" (\n"
	           		+ "	\"reffood\"\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}};
	}
	
	public static void CreateRefDB(Connection conn) {
		   Statement stmt;
		   String sql ;
		try {
	
	         stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"Biblio\" (\n"
	           		+ "	\"UUID\"	TEXT UNIQUE,\n"
	           		+ "	\"fAuthor\"	TEXT,\n"
	           		+ "	\"year\"	INTEGER,\n"
	           		+ "	\"fullRef\"	TEXT,\n"
	           		+ "	\"comments\"	TEXT\n"
	           		+ ", \"consistent\"	INTEGER NOT NULL DEFAULT 1)";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"SupplementVariable\" (\n"
	           		+ "	\"refEquation\"	TEXT,\n"
	           		+ "	\"VariableKind\"	INTEGER,\n"
	           		+ "	FOREIGN KEY(\"refEquation\") REFERENCES \"equation\"(\"UUID\"),\n"
	           		+ "	UNIQUE(\"refEquation\",\"VariableKind\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEAA\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEANA\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEBASE\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUELIPID\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEMACRO\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEMIN\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEOTHER\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"VALUEVITAM\" (\n"
	           		+ "	\"ID\"	INTEGER NOT NULL UNIQUE,\n"
	           		+ "	\"kind\"	INTEGER NOT NULL,\n"
	           		+ "	\"kindrelative\"	INTEGER NOT NULL,\n"
	           		+ "	\"refBiblio\"	TEXT NOT NULL,\n"
	           		+ "	\"version\"	INTEGER,\n"
	           		+ "	\"value\"	REAL,\n"
	           		+ "	\"date\"	TEXT,\n"
	           		+ "	\"unitKind\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	UNIQUE(\"kind\",\"kindrelative\",\"refRef\"),\n"
	           		+ "	PRIMARY KEY(\"ID\" AUTOINCREMENT),\n"
	           		+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),\n"
	           		+ "	FOREIGN KEY(\"refBiblio\") REFERENCES \"Biblio\"(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"coef\" (\n"
	           		+ "	\"UUID\"	TEXT UNIQUE,\n"
	           		+ "	\"coefName\"	INTEGER,\n"
	           		+ "	\"value\"	NUMERIC,\n"
	           		+ "	\"groupUUID\"	INTEGER,\n"
	           		+ "	\"refRef\"	TEXT,\n"
	           		+ "	PRIMARY KEY(\"UUID\")\n"
	           		+ ")";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE dataRef (UUID TEXT UNIQUE, name TEXT, description TEXT, disease INTEGER, BWeqRef TEXT, SERname TEXT, SERRef TEXT, DEcomRef TEXT, DErawRef TEXT, k1Name TEXT, k1Ref TEXT, k2Name TEXT, k2Ref TEXT, k3Name TEXT, k3Ref TEXT, k4Name TEXT, k4Ref TEXT, k5Name TEXT, k5Ref TEXT, specie TEXT, consistent INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (UUID), FOREIGN KEY (SERRef) REFERENCES equation (UUID), FOREIGN KEY (DErawRef) REFERENCES equation (UUID), FOREIGN KEY (DEcomRef) REFERENCES equation (UUID))";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE \"equation\" (\n"
	           		+ "	\"UUID\"	TEXT UNIQUE,\n"
	           		+ "	\"script\"	TEXT,\n"
	           		+ "	\"refBiblio\"	TEXT,\n"
	           		+ "	\"name\"	TEXT,\n"
	           		+ "	\"description\"	TEXT,\n"
	           		+ "	\"speciesRef\"	TEXT\n"
	           		+ ", \"kind\"	INTEGER, \"consistent\"	INTEGER NOT NULL DEFAULT 1, \"nutrient\"	INTEGER)";    
	           stmt.executeUpdate(sql);
	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE method (UUID TEXT PRIMARY KEY UNIQUE NOT NULL, name TEXT, species TEXT NOT NULL DEFAULT \"ALL\", description TEXT)";    
	           stmt.executeUpdate(sql);
	           sql = "CREATE TABLE \"speReqEq\" (\"refRef\"	TEXT, \"refEq\"	TEXT,		UNIQUE(\"refEq\", \"refRef\"),"
	        			+ "	FOREIGN KEY(\"refRef\") REFERENCES \"dataRef\"(\"UUID\"),"
	        			+ "	FOREIGN KEY(\"refEq\") REFERENCES \"equation\"(\"UUID\")"
	        		   + ")";    
	           stmt.executeUpdate(sql);
	           
	           

	           stmt = conn.createStatement();     
	           sql = "CREATE TABLE targetMethod (UUID TEXT PRIMARY KEY UNIQUE NOT NULL, refMethod TEXT REFERENCES method (UUID), ord INT, kind INT, value REAL DEFAULT (0), unit INT, percent REAL DEFAULT (0), measure REAL DEFAULT (1), UNIQUE (refMethod, kind))";    
	           stmt.executeUpdate(sql);
	          
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}};
	}
}
