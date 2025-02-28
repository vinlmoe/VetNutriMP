package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import org.controlsfx.control.CheckListView;
import DataStruct.BiblioP;
import DataStruct.EquationConsP;
import DataStruct.KindEnergyConverter;
import DataStruct.SpeciesConverter;
import Enumerise.AllNutrient;
import Enumerise.EquationKind;
import Enumerise.NutrientBase;
import Enumerise.VariableKind;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;

import graph.component.StringCellbundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AlimIndic;
import model.BiblioRef;
import model.Espece;

public class EquationEditorControler  implements Initializable {

	private Equation equ;
	private ObservableList<EquationConsP> mEquList;
	private ObservableList<BiblioP> mBiblioList;
	private Stage stage;

	private boolean saved=true;

	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;


	//Définition graphique 

@FXML
private VBox vBoxEditor;

	@FXML
	private TextField searchText;
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TableView<EquationConsP> refTable;
	@FXML
	private TableColumn<EquationConsP, String> speciesColumn;
	@FXML
	private TableColumn<EquationConsP, String> refColumn;
	@FXML
	private TableColumn <EquationConsP, String>nameColumn;

	@FXML
	private ComboBox<Espece> speciesDescCombo;
	@FXML
	private TextField refName;
	@FXML
	private ComboBox<BiblioRef> biblioCombo; 
	
	@FXML
	private ComboBox<AllNutrient> nutrientCombo;

	@FXML
	private ComboBox<EquationKind> kindCombo;

	@FXML
	private TextArea descriptionTextArea;

	@FXML
	private TextArea equationTextArea;
	@FXML
	private CheckListView<VariableKind> listVariable;
	@FXML 
	private  void addEquation() {
		save();
		Equation nEqu=new Equation();
		 
		 String transUUID=nEqu.getUUID();
		 
		 DataConnector.UpdateEquation(nEqu, null);
		 EquationConsP eqp=new EquationConsP(nEqu);
		 mEquList.add(eqp);


		 Update();
		 
		 refTable.getSelectionModel().select(eqp);
	}

	@FXML 
	private  void duplicate() {
		save();
		if (equ!=null) {
			
		
		
		Equation	 nEqu=(Equation) equ.clone();
		nEqu.setName("(Dup)"+nEqu.getName());
		 String transUUID=nEqu.getUUID();
		 
		 
		 
		 DataConnector.UpdateEquation(nEqu, null);
		 
		 EquationConsP eqp=new EquationConsP(nEqu);
		 mEquList.add(eqp);


		 Update();
		 
		 refTable.getSelectionModel().select(eqp);
		}
	}

	

@FXML
private void close() {
save();
	
		DataConnector.updateListEquation(mEquList, null);
	
	stage.close();
}
@FXML
private void delete() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("You will delete the Selected rconsultation ");
		alert.setContentText("Are you sure you want it??");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			DataConnector.DeleteEquation(refTable.getSelectionModel().getSelectedItem().getEquation().getUUID(), null);
			mEquList.remove(refTable.getSelectionModel().getSelectedItem());
			Update();
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	
	
	}
}


	@FXML
	private boolean save() {

		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
		if(refName.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("Name");
		}
		if(equationTextArea.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("equation");
		}
if(equ==null) {
	error=true;
	errMess+= "\n "+bundle.getString("equation");
}

		if (error) {
			if (equ!=null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(errMess);

			alert.show();
			}
		}else {


			//equ.setBib(biblioCombo.getValue());
			equ.setName(refName.getText());
			equ.setDescription(descriptionTextArea.getText());
			equ.setEquationScript(equationTextArea.getText());
			equ.setSpecie(speciesDescCombo.getSelectionModel().getSelectedItem());
		equ.setKind(kindCombo.getSelectionModel().getSelectedItem());
			equ.setBib(biblioCombo.getSelectionModel().getSelectedItem());
			ObservableList<VariableKind>listVar=	listVariable.getCheckModel().getCheckedItems();
			equ.setAlllNut(nutrientCombo.getSelectionModel().getSelectedItem());
			equ.removeAllvariable();
			for (VariableKind vk:listVar) {
				equ.addVariable(vk);
			}

			for (int i=0; i<mEquList.size();i++) {
				if (mEquList.get(i).getEquation().getUUID().equals(equ.getUUID())) {
					mEquList.get(i).getEquation().Update(equ);
				
				}
			}
			
			refTable.refresh();
			saved=true;
		}
		return saved;

	}

	@Override 
	public void initialize(URL location, ResourceBundle resources) {
visibility(false);
		bundle=resources;
		//AlimTabl

		refTable.setEditable(true);

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("especeStr"));
		refColumn.setCellValueFactory(new PropertyValueFactory<>("ref"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));




		refTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualEquation(newValue));

		Callback<TableColumn<EquationConsP, String>, TableCell<EquationConsP, String>> stringCellFactory
		= (TableColumn<EquationConsP, String> param) -> new StringCellbundle(bundle);
		
		speciesColumn.setCellFactory(stringCellFactory);

		 for(VariableKind ta:VariableKind.values()) {
	       	  listVariable.getItems().add(ta);
	         }



		listVariable.setCellFactory(listView -> new CheckBoxListCell<VariableKind>(  listVariable::getItemBooleanProperty) {
			@Override
			public void updateItem(VariableKind employee, boolean empty) {
				super.updateItem(employee, empty);
				setText(employee == null ? "" : resources.getString( employee.getName()));
			}
		});
		 for(Espece ta:Espece.values()) {
       	  speciesCombo.getItems().add(ta);
         }

		 for(Espece ta:Espece.values()) {
	       	  speciesDescCombo.getItems().add(ta);
	         }
			
		 for(EquationKind ta:EquationKind.values()) {
	       	  kindCombo.getItems().add(ta);
	         }
		 for (Object an:AllNutrient.values().values().toArray()) {
			 nutrientCombo.getItems().add((AllNutrient)an);
		 }
		 nutrientCombo.getSelectionModel().select(0);
		
		speciesCombo.getSelectionModel().select(0);

		speciesCombo.setConverter(new SpeciesConverter(bundle));
		speciesDescCombo.setConverter(new SpeciesConverter(bundle));
kindCombo.setConverter(new KindEnergyConverter(bundle));

		speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			refTable.setItems( searchEquationList());


		}); 
		
		
		nutrientCombo.setConverter(new StringConverter<AllNutrient>()
		{
			
		    // Method to convert a Person-Object to a String
	    @Override
	    public String toString(AllNutrient person)
	    {
	        return person == null? null : bundle.getString(person.getLabel())+" ("+person.getUnit()+")";
	    }
	 
	    // Method to convert a String to a Person-Object
	    @Override
	    public AllNutrient fromString(String string)
	    {
	    
	        return new AllNutrient(NutrientBase.PROTEINE);
	    }
	});
kindCombo.getSelectionModel().selectedItemProperty().addListener(
		
		(observable, oldValue, newValue) ->{ 
	if (newValue!=null) {
		if (newValue.equals(EquationKind.NEED)) {
			nutrientCombo.setVisible(true);
		}else {
			nutrientCombo.setVisible(false);
		}}}
		);


		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			refTable.setItems(searchEquationList());
		});
		refTable.getSelectionModel().selectedItemProperty().addListener(
				
				(observable, oldValue, newValue) -> ActualEquation(newValue));

	
	} 
	public void Update()  {
		refTable.setItems( searchEquationList() );
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition) {
		this.mainApp=mainApp;
		this.mEquList=mainApp.getmEquationList();
		this.stage=stage;
		this.edition=edition;

		mBiblioList=mainApp.getmBiblioList();
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	        public void handle(WindowEvent we) {
	            System.out.println("Stage is closing");
	            close();
	        }
	    });        
		for( BiblioP bp:mBiblioList) {
			biblioCombo.getItems().add(bp.getBiblio());
		}


		refTable.setItems(searchEquationList());

	
	}


	private ObservableList<EquationConsP> searchEquationList()  {
		ObservableList<EquationConsP> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (EquationConsP al:mEquList) {
				if (		al.getEquation().getSpecie().getUUID().equals(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {

					boolean prs=true;
					for (String word: words){
						if (((al.getName().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getRef().toLowerCase().indexOf(word.toLowerCase())!=-1)){
							prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sEquaList.add(al);
					}					

				}
			}    		

		}else {
			for (EquationConsP al:mEquList) {
				if (		al.getEquation().getSpecie().getUUID().equals(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {			sEquaList.add(al);
				}

			}
		}


		return sEquaList;
	}

	public Equation getData() {
		return equ;
	}

	private void ActualEquation(Equation al) {
if (al==null) {
	visibility(false);
}else {
	visibility(true);
}
		
		if (al!=null & !abort) {
save();
			if (saved==true) { 

				equ=al;
if (equ.getKind().equals(EquationKind.NEED)) {
	nutrientCombo.setVisible(true);
}else {
	nutrientCombo.setVisible(false);
}
				
				refName.setText(equ.getName());
				descriptionTextArea.setText(equ.getDescription());
				equationTextArea.setText(equ.getEquationScript() );
				speciesDescCombo.getSelectionModel().select(equ.getSpecie());
				biblioCombo.getSelectionModel().select(equ.getBib());
				kindCombo.getSelectionModel().select(equ.getKind());
				listVariable.getCheckModel().clearChecks();
				for(VariableKind ind:equ.getVar()) {
					listVariable.getCheckModel().check(ind);
				}
				
				nutrientCombo.getSelectionModel().select(al.getAlllNut());
			
							}
		}
	}
	private void ActualEquation(EquationConsP al) {
		if (al==null) {
			visibility(false);
		}else {
			visibility(true);
			ActualEquation(al.getEquation());
		}
				
			
			}
	private void visibility(boolean b) {
		refName.setVisible(b);
		speciesDescCombo.setVisible(b);
		kindCombo.setVisible(b);
		biblioCombo.setVisible(b);
		descriptionTextArea.setVisible(b);
		equationTextArea.setVisible(b);
		listVariable.setVisible(b);
	}
		
		}
