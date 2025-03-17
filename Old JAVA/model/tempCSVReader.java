package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class tempCSVReader {
    public static String getResourcePath(String fileName) {
        final File f = new File("");
        final String dossierPath = f.getAbsolutePath() + File.separator + fileName;
        return dossierPath;
    }

    public static File getResource(String fileName) {
        final String completeFileName = getResourcePath(fileName);
        File file = new File(completeFileName);
        return file;
    }	
	   public static List<String> readFile(File file) {

	        if (file == null) {
	            throw new IllegalArgumentException("Le fichier ne peut pas ??tre null");
	        }

	        if (!file.exists()) {
	            throw new IllegalArgumentException("Le fichier " + file.getName() + " n'existe pas.");
	        }

	        final List<String> result = new ArrayList<String>();

	        FileReader fr = null;
	        BufferedReader br = null;

	        try {
	            fr = new FileReader(file);
	            br = new BufferedReader(fr);

	            for (String line = br.readLine(); line != null; line = br.readLine()) {
	                result.add(line);
	            }

	        } catch (Exception e) {
	            e.printStackTrace();

	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            if (fr != null) {
	                try {
	                    fr.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }

	        }

	        return result;
	    }
			
	      
	    }


