package model;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Importeur {
	DataAccess data=new DataAccess();
	DataPack packe=new DataPack();
	private boolean touch =false;
	public Importeur(){
		UIManager.put("FileChooser.saveButtonText","Enregistrer"); 
		UIManager.put("FileChooser.openButtonText","Ouvrir"); 
		UIManager.put("FileChooser.cancelButtonText","Annuler"); 
		UIManager.put("FileChooser.updateButtonText","Actualiser"); 
		UIManager.put("FileChooser.helpButtonText","Aide"); 
		UIManager.put("FileChooser.saveButtonToolTipText","Enregistre le fichier");
	}
	public void importerAlimPet() throws IOException{
		JFileChooser chooser = new JFileChooser();//cr??ation dun nouveau filechosser
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Fichier VetBrain", "vbr"));
		//affiche la boite de dialogue
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{	
			listAlim list=	data.readAlimImport(chooser.getSelectedFile().getAbsolutePath()); //si un fichier est selectionn??, r??cup??rer le fichier puis sont path et l'afficher dans le champs de texte
			listAlim listorig= data.readAlim();
			if (list!=null){
	
				for (int i=0; i<list.size(); i++){
					touch =false;
					if (list.getAlim(i).getTypeAliment().equals(TypeAlim.COMPLET)||list.getAlim(i).getTypeAliment().equals(TypeAlim.COMPLEMENTAIRE)||list.getAlim(i).getTypeAliment().equals(TypeAlim.BARF)){
						listorig.addAlim(list.getAlim(i));

					}

				}
				data.writeAliment(listorig);


			}
		}
	}
	public void importerAlimExcel(Vet vet) throws IOException{
		JFileChooser chooser = new JFileChooser();//cr??ation dun nouveau filechosser
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel", "xlsx"));
		//affiche la boite de dialogue
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{	
			packe.createFileEvolve(chooser.getSelectedFile().getAbsolutePath(), vet); //si un fichier est selectionn??, r??cup??rer le fichier puis sont path et l'afficher dans le champs de texte



		}
	}
}


