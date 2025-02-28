package DataStruct;

import java.time.LocalDate;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;

public class LineWeight {
	private float weightInit=0;
	private float weightObj=0;
	private LocalDate dateInit;
	private String UUID;
	private float upAlpha=0;
	private float downAlpha=0;
	private int kind=1;
	private int section=1;
public LineWeight( float weight, float upAlph, float downAlph, float weightobj, LocalDate d, int k, int sec) {
	this.weightInit=weight;
	this.dateInit=d;
	this.upAlpha=upAlph;
	this.downAlpha= downAlph;
	this.weightObj=weightobj;
	this.kind=k;
	this.section=sec;
	
}
public LocalDate getDateInit() {
	return dateInit;
}

public boolean isOK() {
	if (dateInit!=null&
			upAlpha!=0&
			downAlpha!=0) {
		return true;
	}
	else {
		return false;
	}
}
public float getDownAlpha() {
	return downAlpha;
}
public int getKind() {
	return kind;
}
public int getSection() {
	return section;
}
public float getUpAlpha() {
	return upAlpha;
}
public String getUUID() {
	return UUID;
}
public float getWeightInit() {
	return weightInit;
}
public float getWeightObj() {
	return weightObj;
}

public float minChangDuration() {
	return Math.abs(weightInit-weightObj)/ (weightInit*upAlpha/100);
}
public float maxChangDuration() {
	return Math.abs(weightInit-weightObj)/(weightInit*downAlpha/100);
}
}
