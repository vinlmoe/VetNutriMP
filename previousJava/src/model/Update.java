package model;

import java.io.IOException;

import application.TextConstant;

public class Update {
	DataAccess data=new DataAccess();
	public Update () throws IOException{
		//21=>22 test
	
		listAnim lisa= data.readAnim();
		if (lisa!=null){
			if (lisa.getAnim(0).getVersion()==null){
				for (int i=0; i<lisa.size(); i++){
					lisa.getAnim(i).setEspece(Espece.CHIEN);
					lisa.getAnim(i).setVersion(TextConstant.VERSION.nameToString());
					//System.out.println(lisa.size()+lisa.getAnim(i).getNom()+lisa.getAnim(i).getVersion());
				}
			}
			
			data.writeAnimal(lisa);
		}
		
	}
}

