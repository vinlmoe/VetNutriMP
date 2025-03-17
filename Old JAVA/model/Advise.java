package model;

import java.io.Serializable;

public class Advise implements Serializable{
private static final long serialVersionUID = 1L;
private String name="";
private String text="";

public Advise(String name, String text) {
	this.name=name;
	this.text=text;
}
public String getName() {
	return name;
}
public String getText() {
	return text;
}
public void setName(String name) {
	this.name = name;
}
public void setText(String text) {
	this.text = text;
}
@Override
public String toString() {
	return getName();
}
}
