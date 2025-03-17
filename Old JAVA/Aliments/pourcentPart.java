package Aliments;

public class pourcentPart {
private String name="";
private float pourcent=0;


	public pourcentPart(String name, float pourcent){
		this.name=name;
		this.pourcent=pourcent;
	}

	public pourcentPart(){
		
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setPourcent(float pourcent) {
		this.pourcent = pourcent;
	}
	public float getPourcent() {
		return pourcent;
	}
	
	
	public pourcentPart[] tri(pourcentPart pourPart[]){
		
		int min;
pourcentPart temp;

		for (int index = 0; index < pourPart.length - 1; index++)
		{
		min = index;
		for (int i = index + 1; i < pourPart.length; i++){
		
		if (pourPart[i].getPourcent() < pourPart[min].getPourcent()){
		min = i;
		}}
		temp = pourPart[min];
		pourPart[min] = pourPart[index];
		pourPart[index] = temp;
		
		}
		return pourPart;
		
	}
}
