package model;

import java.io.Serializable;

import Enumerise.Nutrient;

public class NutrientQuantity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Nutrient nut;
	private float value;
	
	public NutrientQuantity(Nutrient n, float val) {
		nut=n;
		value=val;
		// TODO Auto-generated constructor stub
	}
	
 public Nutrient getNut() {
	return nut;
}
 public float getValue() {
	return value;
}
 public void setValue(float value) {
	this.value = value;
}
}
