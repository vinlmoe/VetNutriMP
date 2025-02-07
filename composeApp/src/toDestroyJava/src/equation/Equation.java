package equation;

import DataStruct.AlimP;
import org.mariuszgromada.math.mxparser.*;
import DataStruct.CoefP;
import DataStruct.SupplementalvariableP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.script.*;

import Enumerise.AllNutrient;
import Enumerise.EquationKind;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.VariableKind;
import javafx.collections.ObservableList;
import model.AlimentEv;
import model.AlimentRation;
import model.BiblioRef;
import model.Espece;
import model.Ration;
import model.RationCalculator;
import model.Species;

public class Equation implements Cloneable, Serializable  {
	private static final long serialVersionUID = 1L;
	private String Description="";
	
	private String equationScript="";
private boolean jvscript=false;
	private BiblioRef bib;
	private Espece Specie;
	private String name;
	private String UUID="";
	private EquationKind kind;
private AllNutrient alllNut= new AllNutrient(NutrientBase.PROTEINE);
private boolean consistent=true;
	
	private ArrayList<VariableKind> var=new ArrayList();
	
	


	/*private Bindings bindVar;
	private ScriptEngine script;*/
	
	
	
	public Equation() {
		
		UUID=java.util.UUID.randomUUID().toString();
		bib=new BiblioRef();
		Specie=Specie.CHIEN;
		name="";
		kind=EquationKind.ENERGYNEED;
	}
	public Equation(String uuid) {
		
		UUID=uuid;
		bib=new BiblioRef();
		Specie=Specie.CHIEN;
		name="";
		kind=EquationKind.ENERGYNEED;
	}

	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public EquationKind getKind() {
		return kind;
	}
	public void setKind(EquationKind kind) {
		this.kind = kind;
	}
	public double runAnim(RationCalculator calc, ObservableList<SupplementalvariableP>svp) {
	/*	if (jvscript) {
			return(runAnimjv(calc, svp));
		}*/
		
		if (kind.equals(EquationKind.ENERGYNEED)|kind.equals(EquationKind.MW)) {
	
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
			Expression exp=new Expression(equationScript);
			exp.disableImpliedMultiplicationMode();
			exp.defineArgument("BW", calc.getOptiPoids());
			for(SupplementalvariableP s:svp) {
				exp.defineArgument(s.getVariable().getVariable(), s.getValue());	
			}
			for(String a:exp.getMissingUserDefinedArguments()) {
				System.out.println(a);
			}
		Object rep	=exp.calculate();


	if (rep!=null) {
		return (double)  rep;
	}else {
		return 50 % 0.0;
	}
			
		}else {
			return 50 % 0.0;
		}
	}
	public double runNeed(RationCalculator calc, ObservableList<SupplementalvariableP>svp, Ration rat) {
/*	if (jvscript) {
		return(runNeedjv(calc, svp,rat));
	}*/
		
		if (kind.equals(EquationKind.NEED)) {
		
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
			 
			 Expression exp=new Expression(equationScript);
				exp.disableImpliedMultiplicationMode();
				exp.defineArgument("BW", calc.getOptiPoids());
				exp.defineArgument("BEE", calc.getBEE());
				exp.defineArgument("MW", calc.getPMOpti());
				for(SupplementalvariableP s:svp) {
					exp.defineArgument(s.getVariable().getVariable(), s.getValue());	
				}
				for(NutrientBase s:NutrientBase.values()) {
					exp.defineArgument(s.getLabel(), rat.getNutrient(s));	
				}
				for(NutrientLipid s:NutrientLipid.values()) {
					exp.defineArgument(s.getLabel(), rat.getNutrient(s));	
				}
				for(String a:exp.getMissingUserDefinedArguments()) {
					System.out.println(a);
				}
			
		Object rep=exp.calculate();

	if (rep!=null) {
		return (double)  rep;
	}else {
		return 50 % 0.0;
	}
			
		}else {
			return 50 % 0.0;
		}
	}
	public double runAlim(AlimentRation ar) {
		if(jvscript) {
			return(runAlim(ar));
		}
/*		if (script==null) {
		createEngine();}*/
		if (kind.equals(EquationKind.ENERGYDENSITY)) {
	
			 Expression exp=new Expression(equationScript);
				exp.disableImpliedMultiplicationMode();
for(NutrientBase s:NutrientBase.values()) {
	exp.defineArgument(s.getLabel(), ar.getNutrient(s));	
}


//script.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);
	
	
Object rep=exp.calculate();

if (rep!=null) {
	return (double)  rep;
}else {
	return 50 % 0.0;
}	
		}else {
			return 50 % 0.0;
		}
	}
	
	
	
	

	public ObservableList<AlimP> runAlim(ObservableList<AlimP> ol) {
	//	bindVar.put("Calc", calc);
		if(jvscript) {
			return(runAlim(ol));
		}
		AlimentRation ar;
		
		
		
		if (kind.equals(EquationKind.ENERGYDENSITY)) {
	
for (AlimP ap:ol) {

	float rep=0;
	if (!ap.isDE(equationScript)) {
	ar=(ap.getAlimR());
	
	
//bindVar.clear();
	 Expression exp=new Expression(equationScript);
		exp.disableImpliedMultiplicationMode();
for(NutrientBase s:NutrientBase.values()) {
exp.defineArgument(s.getLabel(), ar.getNutrient(s));	
}


//scriptscript.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);



 rep=(float)(double)exp.calculate();

	}else {
		
		rep=ap.getDE(equationScript);
	
	}

if (rep!=0) {
	ap.setDE(rep,equationScript );  
}else {
	ap.setDE(0,"");
}		
}
		
		}
		return ol;
	}
	
	
	/*
	
	
	public double runAnimjv(RationCalculator calc, ObservableList<SupplementalvariableP>svp) {
		if (script==null) {
		createEngine();}
		if (kind.equals(EquationKind.ENERGYNEED)|kind.equals(EquationKind.MW)) {
		synchronized(script) {
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
		   
		try {
		
//bindVar=script.createBindings();
script.put("engine.WarnInterpreterOnly", false);
script.put("BW", calc.getOptiPoids());
for(SupplementalvariableP s:svp) {
script.put(s.getVariable().getVariable(), s.getValue());	
}

script.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);
	
Object rep=script.eval(equationScript);

	if (rep!=null) {
		return (double)  rep;
	}else {
		return 50 % 0.0;
	}
			} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 50 % 0.0;
		} 
		}}else {
			return 50 % 0.0;
		}
	}
	public double runNeedjv(RationCalculator calc, ObservableList<SupplementalvariableP>svp, Ration rat) {
		if (script==null) {
		createEngine();}
		if (kind.equals(EquationKind.NEED)) {
		synchronized(script) {
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
		   
		try {
		
//bindVar=script.createBindings();
script.put("engine.WarnInterpreterOnly", false);
script.put("BW", calc.getOptiPoids());
script.put("BEE", calc.getBEE());
script.put("MW", calc.getPMOpti());
for(SupplementalvariableP s:svp) {
script.put(s.getVariable().getVariable(), s.getValue());	
}
for(NutrientBase s:NutrientBase.values()) {
	script.put(s.getLabel(), rat.getNutrient(s));	
}
for(NutrientLipid s:NutrientLipid.values()) {
	script.put(s.getLabel(), rat.getNutrient(s));	
}


script.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);
	
Object rep=script.eval(equationScript);

	if (rep!=null) {
		return (double)  rep;
	}else {
		return 50 % 0.0;
	}
			} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 50 % 0.0;
		} 
		}}else {
			return 50 % 0.0;
		}
	}
	public double runAlimjv(AlimentRation ar) {
		if (script==null) {
		createEngine();}
		if (kind.equals(EquationKind.ENERGYDENSITY)) {
		synchronized(script) {
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
		   
		try {

//bindVar=script.createBindings();
script.put("engine.WarnInterpreterOnly", false);
for(NutrientBase s:NutrientBase.values()) {
	script.put(s.getLabel(), ar.getNutrient(s));	
}


//script.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);
	
	
Object rep=script.eval(equationScript);

if (rep!=null) {
	return (double)  rep;
}else {
	return 50 % 0.0;
}		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 50 % 0.0;
		} 
		}}else {
			return 50 % 0.0;
		}
	}
	
	
	
	

	public ObservableList<AlimP> runAlimjv(ObservableList<AlimP> ol) {
	//	bindVar.put("Calc", calc);
		AlimentRation ar;
		if (script==null) {
		createEngine();}
		if (kind.equals(EquationKind.ENERGYDENSITY)) {
		synchronized(script) {
		    // synchronization is harmless for local thread engine,
		    // necessary for shared engine
		   
			bindVar=script.createBindings();
for (AlimP ap:ol) {			
	if (!ap.getEqHash().equals(equationScript)) {
	ar=(ap.getAlimR());
	
	try {
		
//bindVar.clear();
script.put("engine.WarnInterpreterOnly", false);
for(NutrientBase s:NutrientBase.values()) {
	script.put(s.getLabel(), ar.getNutrient(s));	
}



//scriptscript.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);



Object rep=script.eval(equationScript);



if (rep!=null) {
	ap.setDE((float)(double)rep,equationScript );  
}else {
	ap.setDE(0,"");
}		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ap.setDE(0,"");
		} 
}}
		}
		}
		return ol;
	}
	
	
	
	
	*/
	/*
	public void createEngine() {
		
		script =        new ScriptEngineManager().getEngineByName("js");


		    /*
	
	 bindVar=script.createBindings();


	 bindVar.put("PW", 12);
	 script.setBindings(bindVar, ScriptContext.GLOBAL_SCOPE);*/

	        /* Moteur Javascript 
	        ScriptEngine javascriptEngine =  new NashornScriptEngineFactory().getScriptEngine();
	        System.out.println(manager);
	        System.out.println(javascriptEngine);
	        Compilable compEngine = (Compilable) javascriptEngine;
	        
	        try {
	          CompiledScript script = compEngine.compile("130*PW**0.75");
	return script;
	        } catch (ScriptException e) {
	          System.err.println(e);
	          return null;
	          
	        }
	        
	     
	        
	}
*/

	public BiblioRef getBib() {
		return bib;
	}
public void setBib(BiblioRef bib) {
	this.bib = bib;
}



public String getEquationScript() {
	return equationScript;
}
	public void setEquationScript(String equationScript) {
		this.equationScript = equationScript;
	}
	public Espece getSpecie() {
		return Specie;
	}
	public void setSpecie(Espece espece) {
		Specie = espece;
	}
	public String getName() {
		return name;
	}
	 public void setName(String name) {
		this.name = name;
	}
	 public ArrayList<VariableKind> getVar() {
		return var;
	}
	 public void addVariable(VariableKind vk) {
		if (!var.contains(vk)) {
			var.add(vk);
		}
	 }
	 public void Update( Equation eq) {
		 this.bib=eq.getBib();
		 this.Description=eq.getDescription();
		 this.name=eq.getName();
		 this.kind=eq.getKind();
		 this.var=eq.getVar();
		 this.equationScript=eq.getEquationScript();
		 this.Specie=eq.getSpecie();
				 
		 
	 }
	public void removeAllvariable() {
		var.removeAll(var);
	}
	public String getUUID() {
		return UUID;
	}
	@Override
    public String toString() {
        return name;
    }
	
	
	
	 public Equation clone() {
	        Equation o = null;
	        try {
	            // On récupère l'instance à renvoyer par l'appel de la 
	            // méthode super.clone()
	            o = (Equation) super.clone();
	            o.newUUID();
	            
	        } catch(CloneNotSupportedException cnse) {
	            // Ne devrait jamais arriver, car nous implémentons 
	            // l'interface Cloneable
	            cnse.printStackTrace(System.err);
	        }
	        // on renvoie le clone
	        return o;
	    }
	 public Equation clone2() {
	        Equation o = null;
	        try {
	            // On récupère l'instance à renvoyer par l'appel de la 
	            // méthode super.clone()
	            o = (Equation) super.clone();
	          
	            
	        } catch(CloneNotSupportedException cnse) {
	            // Ne devrait jamais arriver, car nous implémentons 
	            // l'interface Cloneable
	            cnse.printStackTrace(System.err);
	        }
	        // on renvoie le clone
	        return o;
	    }
	 public void newUUID() {
		 this.UUID=java.util.UUID.randomUUID().toString();
	 }
	 public void UpdateEquation (Equation eq) {
		 this.equationScript=eq.getEquationScript();
		
		 this.kind=eq.getKind()
;	 }
	 
	 public void setAlllNut(AllNutrient alllNut) {
		this.alllNut = alllNut;
	}
	 public AllNutrient getAlllNut() {
		return alllNut;
	}
	 
	  public boolean isConsistent() {
		return consistent;
	}
	  public void setConsistent(boolean consistent) {
		this.consistent = consistent;
	}

	  
}
