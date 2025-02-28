package model;

import java.io.Serializable;

public class Preferences implements Serializable{
	private int equationDog = 1;
	private int equationDogCroiss= 0; 
	private int equationDens=0;
	
	public int getEquationDens() {
		return equationDens;
	}
	public int getEquationDog() {
		return equationDog;
	}
	public void setEquationDens(int equationDens) {
		this.equationDens = equationDens;
	}
	public void setEquationDog(int equationDog) {
		this.equationDog = equationDog;
	}
	public void setEquationDogCroiss(int equationDogCroiss) {
		this.equationDogCroiss = equationDogCroiss;
	}
	public int getEquationDogCroiss() {
		return equationDogCroiss;
	}
	

	
			

}
