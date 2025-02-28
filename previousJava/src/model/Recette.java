package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Aliments.pourcentPart;
import DataStruct.AlimP;
import application.TextConstant;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Recette implements Serializable{
	static private final long serialVersionUID = 1120L;
	private String UUID;
	private int number;
	private String name;
	private Espece espece= Espece.CH;
	private String description="";

	private float objectComp;
	private String version= TextConstant.VERSION.nameToString();

	private boolean actual;
	private List<AlimentRation> alimentList= new ArrayList<AlimentRation>();
	public Recette(String name){
		UUID=java.util.UUID.randomUUID().toString();
		version= TextConstant.VERSION.nameToString();
		this.name=name;
	}
	public Recette(String name, String uuid){
		UUID=uuid;
		version= TextConstant.VERSION.nameToString();
		this.name=name;
	}
	public Recette(Recette re){
		UUID=java.util.UUID.randomUUID().toString();
		version= TextConstant.VERSION.nameToString();
		this.name=re.getName()+" bis";
for (AlimentRation al:re.getAlimentList()) {
	this.addAliment((AlimentRation) al.clone());
}


	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Ration getRation () {
		Ration r=new Ration();
		r.add(alimentList);
		return r;
	}
	public AlimentRation get(int i) {
		return alimentList.get(i);
	}
	public String getVersion() {
		return version;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	public int getNumber() {
		return number;
	}
	public int size(){
		return alimentList.size();
	}
	public String getUUID() {
		return UUID;
	}
	public boolean isActual() {
		return actual;
	}
	public void setActual(boolean actual) {
		this.actual = actual;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public List<AlimentRation> getAlimentList() {
		return alimentList;
	}
	
	public void setAlimentList(List<AlimentRation> alimentList) {
		this.alimentList = alimentList;
	}


	public void addAliment (AlimentEv alim){
		alimentList.add(new AlimentRation(alim));
		
	}
	
	public void addAliment (AlimentRation alim){
		alimentList.add((alim));
		
	}
	public void removeAliment (String UUIDVal){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				alimentList.remove(i);
			}
		}
	}
	public void removeById (int ID){
		
			if (ID<alimentList.size() & ID>=0){
				alimentList.remove(ID);
			}
		
	}
	public void setQuantiteOfAlim (String UUIDVal, float quant){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setQuantite(quant);
			}
		}
	}
	public void setWeightOfAlim (String UUIDVal, float quant){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setWeight(quant);
			}
		}
	}
	public void setTargetOfAlim (String UUIDVal, targetAdjust targ ){
		for (int i=0; i< alimentList.size(); i++){
			if (alimentList.get(i).getUUID().equals(UUIDVal) ){
				this.alimentList.get(i).setTarget(targ);
			}
		}
	}
	public void removeAllAlim(){
		alimentList.removeAll(alimentList);
	}
	public void transfertRation (Ration rat){
		for (AlimentRation alim:rat.getAlimentList()){
			this.addAliment((AlimentRation)alim.clone());
		}
	}
	public Espece getEspece() {
		return espece;
	}
	public void setEspece(Espece espece) {
		this.espece = espece;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ObservableList<AlimP> getAlimList(){
		ObservableList<AlimP> ol=FXCollections.observableArrayList();
		for (AlimentRation alr: alimentList) {
			ol.add(new AlimP(alr));
		}
		
		
		return ol;
	}
public void setAlimList(ObservableList<AlimP>ol) {
	 alimentList.clear();
	 AlimentRation alr; 
	for (AlimP al:ol) {
	alr=new AlimentRation (al.getAlim(),al.getQuantity(), al.getUUID() );
	alr.setTarget(al.getAdjustOn().getTarget());
		alimentList.add(alr);
	}
}
}
	

