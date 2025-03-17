package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum UnitReqEnum {
	MCAL("Mcal", 0),
	KGBW("kg BW", 1),
	KGMW("kg MW", 2),
	NO("", 3),
	PERC("%", 4)
	;
	
	


private String name = "";
private int ID=0;

private float conv=1.F;
private
 
//Constructeur
UnitReqEnum(String name, int id){
  this.name = name;
this.ID=id;

}
 

 
public String nameToString(){
  return name;
}
public float getConv() {
	return conv;
}
public int getID() {
	return ID;
}

public String getName() {
	return name;
}
public static UnitReqEnum getById(int i){
	
	return (UnitReqEnum) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (UnitReqEnum pageType : UnitReqEnum.values()) {
        map.put(pageType.ID, pageType);
    }
} 
@Override
 public String toString() {
	  return name;
 }

}
