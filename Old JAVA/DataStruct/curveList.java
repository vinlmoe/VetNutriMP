package DataStruct;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.BiblioRef;
import model.Espece;

public class curveList {

	
	public static ObservableList<CurveP> getList(Espece esp){
		ObservableList<CurveP> l= FXCollections.observableArrayList();
		List<CurveParamP> param=new ArrayList<CurveParamP>();
		switch (esp) {
		case CH:
			break;
		case CHAT:
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",	2.621383,	18.16063,	2.310782	));
			 param.add(new CurveParamP(	"2%",	3.138453,	18.40044,	2.191952	));
			 param.add(new CurveParamP(	"9%",	3.760312,	18.46435,	2.085706));
			 param.add(new CurveParamP(	"25%",4.349757,	18.41921,	2.004995	));
			 param.add(new CurveParamP(	"50%",	4.947434,	18.29311	,1.936083	));
			 param.add(new CurveParamP(	"75%",		5.548362,	18.05176,	1.876363	));
			 param.add(new CurveParamP(	"91%",	6.128000	,17.57646,	1.831757	));
			 param.add(new CurveParamP(	"98%",	6.747282,	16.90209,	1.778598));
			 param.add(new CurveParamP(	"99.6%",	7.266061,	16.24581,	1.733618	));
			 l.add(new CurveP ( "Male", param, new BiblioRef(), esp,"1",8));
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",	2.177195,	16.09012	,1.736256	));
			 param.add(new CurveParamP(	"2%",	2.449580	,15.96038,	1.709933	));
			 param.add(new CurveParamP(	"9%",	2.850394,	16.07073	,1.629348));
			 param.add(new CurveParamP(	"25%",3.304537,	16.46494	,1.540884	));
			 param.add(new CurveParamP(	"50%",	3.889990,	17.46718,	1.445390	));
			 param.add(new CurveParamP(	"75%",	4.597917	,18.81171,	1.351468	));
			 param.add(new CurveParamP(	"91%",	5.364967,	19.83963,	1.287748	));
			 param.add(new CurveParamP(	"98%",	6.523333	,22.34698	,1.179895));
			 param.add(new CurveParamP(	"99.6%",	7.582508	,24.24353,	1.121170	));
			 l.add(new CurveP ( "Female", param, new BiblioRef(), esp,"1",8));
				
			break;
		case CHIEN:
			
			param.add(new CurveParamP("0.4%",	1.109854	,	12.08458	,	1.810753	));
			param.add(new CurveParamP("2%",	1.535427	,	14.0034	,	1.597385	));
			param.add(new CurveParamP("9%",	2.039161	,	15.27291	,	1.562812	));
			param.add(new CurveParamP(	"25%",2.616284	,	16.01972	,	1.537392	));
			param.add(new CurveParamP(	"50%",3.211721	,	15.877	,	1.579648	));
			param.add(new CurveParamP(	"75%",3.866406	,	15.38784	,	1.625081	));
			param.add(new CurveParamP(	"91%",4.578398	,	14.65672	,	1.682141	));
			param.add(new CurveParamP(	"98%",5.349884	,	13.75693	,	1.760424	));
			param.add(new CurveParamP(	"99.6%",6.165964	,	12.8673	,	1.828332	));
			l.add(new CurveP ( "Female < 6.5kg", param, new BiblioRef(), esp,"0",12));
			
		 param=new ArrayList<CurveParamP>();
			param.add(new CurveParamP(	"0.4%",1.272559	,	12.78178	,	2.038467	));
			param.add(new CurveParamP(	"2%",1.687415	,	14.07996	,	1.904777	));
			param.add(new CurveParamP("9%",	2.255764	,	15.48813	,	1.754783	));
			param.add(new CurveParamP("25%",	2.91251	,	16.3563	,	1.731087	));
			param.add(new CurveParamP(	"50%",3.699072	,	16.83234	,	1.719296	));
			param.add(new CurveParamP(	"75%",4.561009	,	16.77394	,	1.749617	));
			param.add(new CurveParamP("91%",	5.447244	,	16.18983	,	1.775277	));
			param.add(new CurveParamP(	"98%",6.330286	,	15.13836	,	1.820627	));
			param.add(new CurveParamP(	"99.6%",7.250097	,	14.11965	,	1.82745	));
			l.add(new CurveP ( "Male< 6.5kg", param, new BiblioRef(), esp,"0",12));
			
			
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP("0.4%",	2.863651	,	16.3876	,	1.835619	));
			 param.add(new CurveParamP(	"2%",3.459252	,	16.58917	,	1.845262	));
			 param.add(new CurveParamP("9%",	4.189704	,	16.83913	,	1.823969	));
			 param.add(new CurveParamP(	"25%",4.971365	,	16.67714	,	1.835692	));
			 param.add(new CurveParamP(	"50%",5.885028	,	16.33739	,	1.786134	));
			 param.add(new CurveParamP("75%",	6.76091	,	15.82528	,	1.882438	));
			 param.add(new CurveParamP(	"91%",7.816347	,	15.42912	,	1.900119	));
			 param.add(new CurveParamP(	"98%",8.969031	,	15.0315	,	1.936451	));
			 param.add(new CurveParamP(	"99.6%",10.213104	,	14.70955	,	1.943391	));
			 l.add(new CurveP ( "Female [6.5-9]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",3.935947	,	19.21075	,	1.68409	));
			 param.add(new CurveParamP(	"2%",5.565112	,	20.72705	,	1.629216	));
			 param.add(new CurveParamP(	"9%",7.121573	,	20.5551	,	1.66364	));
			 param.add(new CurveParamP("25%",	8.647994	,	19.88941	,	1.686283	));
			 param.add(new CurveParamP("50%",	10.151493	,	18.98618	,	1.717085	));
			 param.add(new CurveParamP(	"75%",11.604435	,	18.076	,	1.743606	));
			 param.add(new CurveParamP(	"91%",12.938343	,	17.19162	,	1.768996	));
			 param.add(new CurveParamP(	"98%",14.178662	,	16.33742	,	1.797089	));
			 param.add(new CurveParamP(	"99.6%",15.292909	,	15.58999	,	1.824913	));
			 l.add(new CurveP ( "Female [9-15]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",12.48477	,	20.53315	,	2.285163	));
			 param.add(new CurveParamP(	"2%",14.89311	,	19.4463	,	2.321938	));
			 param.add(new CurveParamP(	"9%",17.52067	,	18.76811	,	2.316963	));
			 param.add(new CurveParamP(	"25%",20.12642	,	18.34083	,	2.332147	));
			 param.add(new CurveParamP(	"50%",22.95002	,	18.25048	,	2.308356	));
			 param.add(new CurveParamP(	"75%",25.67431	,	18.02113	,	2.28165	));
			 param.add(new CurveParamP(	"91%",28.05483	,	17.54262	,	2.296781	));
			 param.add(new CurveParamP(	"98%",30.07991	,	16.82682	,	2.333136	));
			 param.add(new CurveParamP(	"99.6%",31.97356	,	16.1728	,	2.357102	));
			 l.add(new CurveP ( "Female [15-30]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP("0.4%",	19.73027	,	23.17412	,	2.201269	));
			 param.add(new CurveParamP(	"2%",22.28789	,	21.42993	,	2.20492	));
			 param.add(new CurveParamP(	"9%",24.69983	,	20.03905	,	2.313005	));
			 param.add(new CurveParamP(	"25%",27.27341	,	19.08208	,	2.332765	));
			 param.add(new CurveParamP(	"50%",29.79266	,	18.34165	,	2.337946	));
			 param.add(new CurveParamP("75%",	32.23491	,	17.68342	,	2.364681	));
			 param.add(new CurveParamP(	"91%",35.00966	,	17.18375	,	2.346992	));
			 param.add(new CurveParamP(	"98%",37.94763	,	16.67522	,	2.318143	));
			 param.add(new CurveParamP(	"99.6%",40.90867	,	16.4006	,	2.296036	));
			 l.add(new CurveP ( "Female [30-40]kg", param, new BiblioRef(), esp,"0",12));
				
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",3.347141	,	17.37989	,	2.040052	));
			 param.add(new CurveParamP(	"2%",4.102578	,	17.57553	,	2.009356	));
			 param.add(new CurveParamP(	"9%",4.962623	,	17.49206	,	1.993075	));
			 param.add(new CurveParamP(	"25%",5.850609	,	17.11389	,	1.999331	));
			 param.add(new CurveParamP("50%",	6.763033	,	16.4929	,	2.018385	));
			 param.add(new CurveParamP(	"75%",7.754581	,	15.8306	,	2.033104	));
			 param.add(new CurveParamP("91%",8.86195	,	15.26972	,	2.066372	));
			 param.add(new CurveParamP(	"98%",10.163427	,	14.78888	,	2.065822	));
			 param.add(new CurveParamP(	"99.6%",11.483938	,	14.29	,	2.088452	));
			 l.add(new CurveP ( "Male [6.5-9]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",5.984049	,	20.14643	,	1.967823	));
			 param.add(new CurveParamP(	"2%",9.846229	,	19.27645	,	2.022343	));
			 param.add(new CurveParamP(	"9%",13.263023	,	17.86271	,	2.06549	));
			 param.add(new CurveParamP(	"25%",3.899886	,	19.13236	,	1.930418	));
			 param.add(new CurveParamP(	"50%",8.058689	,	19.93668	,	1.954662	));
			 param.add(new CurveParamP("75%",	11.590806	,	18.56514	,	2.046613	));
			 param.add(new CurveParamP(	"91%",14.746881	,	16.89629	,	2.110846	));
			 param.add(new CurveParamP(	"98%",16.172128	,	15.87276	,	2.149589	));
			 param.add(new CurveParamP(	"99.6%",17.471866	,	14.94524	,	2.197558	));
			 l.add(new CurveP ( "Male [9-15]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",14.24569	,	21.71184	,	2.494588	));
			 param.add(new CurveParamP(	"2%",18.17658	,	20.35052	,	2.479084	));
			 param.add(new CurveParamP(	"9%",21.85644	,	19.68832	,	2.425187	));
			 param.add(new CurveParamP(	"25%",25.18467	,	19.15104	,	2.358881	));
			 param.add(new CurveParamP(	"50%",28.14069	,	18.80447	,	2.365743	));
			 param.add(new CurveParamP(	"75%",31.03546	,	18.58387	,	2.298283	));
			 param.add(new CurveParamP(	"91%",33.55348	,	18.07695	,	2.276793	));
			 param.add(new CurveParamP(	"98%",35.93635	,	17.49292	,	2.234906	));
			 param.add(new CurveParamP(	"99.6%",38.02187	,	16.82385	,	2.191478	));
			 l.add(new CurveP ( "Male [15-30]kg", param, new BiblioRef(), esp,"0",12));
				
			 param=new ArrayList<CurveParamP>();
			 param.add(new CurveParamP(	"0.4%",23.20063	,	24.57251	,	2.338363	));
			 param.add(new CurveParamP(	"2%",26.14607	,	22.2654	,	2.399394	));
			 param.add(new CurveParamP(	"9%",29.25459	,	20.79793	,	2.426847	));
			 param.add(new CurveParamP(	"25%",32.24568	,	19.60027	,	2.406423	));
			 param.add(new CurveParamP(	"50%",34.91356	,	18.56937	,	2.444728	));
			 param.add(new CurveParamP(	"75%",37.58418	,	17.9064	,	2.443216	));
			 param.add(new CurveParamP(	"91%",40.59932	,	17.33738	,	2.429597	));
			 param.add(new CurveParamP(	"98%",43.82808	,	16.94919	,	2.413749	));
			 param.add(new CurveParamP(	"99.6%",47.07196	,	16.65365	,	2.365383	));
			 l.add(new CurveP ( "Male [30-40]kg", param, new BiblioRef(), esp,"0",12));
				
			 
			 
			break;
		case FURET:
			break;
		case LAPIN:
			break;
		case PRIMATE:
			break;
		case RAT:
			break;
		case SOURIS:
			break;
		default:
			break;
	
		}
		return l;
	}
	

}
