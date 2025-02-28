package model;

import java.io.Serializable;

public class TargetDefinition implements Serializable, Cloneable{
	static private final long serialVersionUID = 101L;
	private String UUID;
	
	private float value=1;
	private float percentCompletion=100;
	private targetAdjust targ;
private float pas=5;
	
	public TargetDefinition (targetAdjust targ, float value, float percentCompletion, float pas){
		UUID=java.util.UUID.randomUUID().toString();
		this.targ=targ;
		this.value=value;
		this.percentCompletion=percentCompletion;
		this.pas=pas;
		
		
	}
	public void setPercentCompletion(float percentCompletion) {
		this.percentCompletion = percentCompletion;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public void setPas(float pas) {
		this.pas = pas;
	}
	public float getPas() {
		return pas;
	}
	
public float getPercentCompletion() {
	return percentCompletion;
}
public targetAdjust getTarg() {
	return targ;
}
public String getUUID() {
	return UUID;
}
public float getValue() {
	return value;
}
public Object clone() {
	Object o = null;
	try {
		// On r??cup??re l'instance ?? renvoyer par l'appel de la 
		// m??thode super.clone()
		o = super.clone();
	} catch(CloneNotSupportedException cnse) {
		// Ne devrait jamais arriver car nous impl??mentons 
		// l'interface Cloneable
		cnse.printStackTrace(System.err);
	}
	// on renvoie le clone
	return o;
}
	
}
