package Enumerise;

import java.util.HashMap;
import java.util.Map;

public enum Reflevel {
MIN("MIN",0),
MAX("MAX",1),
OPTIMIN("OPTIMIN",2),

OPTIMAX("OPTIMAX",3)
			;
			


		private String name = "";
		private int coef = 0;

		//Constructeur
		Reflevel(String name, int coef){
		this.name = name;
		this.coef=coef;
		}
		public static Reflevel getById(int i){
			
			return (Reflevel) map.get(i);
		}
		 private static Map map = new HashMap<>();
		static {
	        for (Reflevel pageType : Reflevel.values()) {
	            map.put(pageType.coef, pageType);
	        }
	    }

		public String nameToString(){
		return name;
		}
		public int getCoef(){
			  return coef;
			}
		public static int size(){
			  return 4;
			}
		}

