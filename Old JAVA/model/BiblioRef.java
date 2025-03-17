package model;

import java.io.Serializable;

public class BiblioRef implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String firstAuthor="";
	private int year=1800;
	private String completeRef="";
	private String comment="";
	private String UUID;
	private int consistent=0;
	
	public BiblioRef() {
		UUID=java.util.UUID.randomUUID().toString();
	}
	public BiblioRef(String uuid) {
		UUID=uuid;
	}
	public String getFirstAuthor() {
		return firstAuthor;
	}

	public int getYear() {
		return year;
	}
	public void setFirstAuthor(String firstAuthor) {
		this.firstAuthor = firstAuthor;
	}
	
	public String getCompleteRef() {
		return completeRef;
	}
	public String getUUID() {
		return UUID;
	}
	public void setCompleteRef(String completeRef) {
		this.completeRef = completeRef;
	}
	public void setYear(String year) {
		int f;
		try {
		 f=Integer.parseInt(year);
		}
		catch(NumberFormatException r) {
			f=1800;
		}
	
		this.year = f;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getConsistent() {
		return consistent;
	}
	public void setConsistent(int consistent) {
		this.consistent = consistent;
	}
	@Override
	public String toString() {
    return firstAuthor+", " +year;
    		}
	
}

