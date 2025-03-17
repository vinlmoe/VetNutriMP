package controller;

import java.net.URL;
import java.util.ResourceBundle;
import DataStruct.BiblioP;
import DataStruct.ReferenceP;
import application.DataConnector;
import application.VetNutri;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import model.BiblioRef;

public class BiblioEditorControler  implements Initializable {

	private BiblioRef biblio;
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
	private TableView<BiblioP> refTable;
	
	@FXML
	private TableColumn<BiblioP, Integer> yearColumn;
	@FXML
	private TableColumn <BiblioP, String>fAuthorColumn;

	@FXML
	private TextField fAuthorText;
	@FXML
	private TextField yearText;

	@FXML
	private TextArea commentTextArea;

	@FXML
	private TextArea fullRefTextArea;
	





	@FXML 
	private  void addEquation() {
		BiblioRef nEqu=new BiblioRef();
		 
		 String transUUID=nEqu.getUUID();
		 
		 DataConnector.UpdateBiblio(nEqu,null);
		 BiblioP b =new BiblioP(nEqu);
		 mBiblioList.add(b);


		 Update();
		 
		 refTable.getSelectionModel().select(b);
	}

@FXML
private void close() {
	save();
	
	DataConnector.updateListBiblio(mBiblioList, null);

stage.close();

}
@FXML
private void delete() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		DataConnector.DeleteBiblio(refTable.getSelectionModel().getSelectedItem().getBiblio().getUUID(), null);
		 mBiblioList.remove(refTable.getSelectionModel().getSelectedItem());

		Update();
	}
}


	@FXML
	private boolean save() {

		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
		if(fAuthorText.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("fAuthor");
		}
		if(yearText.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("year");
		}
		try {
			Integer.parseInt(yearText.getText());
		}catch(NumberFormatException e) {
			error=true;
			errMess+= "\n "+bundle.getString("year");
		}
		if(fullRefTextArea.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("fullRef");
		}

if (biblio!=null) {
		if (error) {
		
biblio.setConsistent(0);
		}else {
biblio.setConsistent(1);}

			//biblio.setBib(biblioCombo.getValue());
		biblio.setFirstAuthor(fAuthorText.getText());
		biblio.setYear(yearText.getText());
		biblio.setCompleteRef(fullRefTextArea.getText());
		biblio.setComment(commentTextArea.getText());
	
			
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

	
		fAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("fAuthor"));
		yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));




		refTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualBiblio(newValue));

	

			

		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			refTable.setItems(searchBiblioList());
		});
		refTable.getSelectionModel().selectedItemProperty().addListener(
				
				(observable, oldValue, newValue) -> ActualBiblio(newValue));

		 refTable.setRowFactory(new Callback<TableView<BiblioP>, TableRow<BiblioP>>() {
		        @Override
		        public TableRow<BiblioP> call(TableView<BiblioP> tableView) {
		            final TableRow<BiblioP> row = new TableRow<BiblioP>() {
		                @Override
		                protected void updateItem(BiblioP person, boolean empty){
		                    super.updateItem(person, empty);
		                    if(person!=null) {
		                  if (person.getBiblio().getConsistent()!=1) {
		                	  setStyle("-fx-background-color:lightcoral");
		                  }else {
		                	  setStyle("");
		                  }
		                }}
		            };
					return row;
		        }
		 });
	} 
	public void Update()  {
		refTable.setItems( searchBiblioList() );
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition) {
		this.mainApp=mainApp;
		this.mBiblioList=mainApp.getmBiblioList();
		this.stage=stage;
		this.edition=edition;

		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	        public void handle(WindowEvent we) {
	            System.out.println("Stage is closing");
	            close();
	        }
	    });        

		refTable.setItems(searchBiblioList());

	
	}


	private ObservableList<BiblioP> searchBiblioList()  {
		ObservableList<BiblioP> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (BiblioP al:mBiblioList) {
	

					boolean prs=true;
					for (String word: words){
						if (((al.getBiblio().getCompleteRef().toLowerCase().indexOf(word.toLowerCase())!=-1))){
							prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sEquaList.add(al);
					}					

				}
			}    		else {
				sEquaList=mBiblioList;
			}
				

	
		


		return sEquaList;
	}

	public BiblioRef getData() {
		return biblio;
	}

	private void ActualBiblio(BiblioRef al) {
		
if (al==null) {
	visibility(false);
}else {
	visibility(true);
}
		
		if (al!=null & !abort) {
save();

			if (saved==true) { 

				biblio=al;

				
				fAuthorText.setText(al.getFirstAuthor());
yearText.setText(al.getYear()+"");
commentTextArea.setText(al.getComment());
fullRefTextArea.setText(al.getCompleteRef());
			}
		}
	}
	private void ActualBiblio(BiblioP al) {
		
		if (al==null) {
			visibility(false);
		}else {
			visibility(true);
			ActualBiblio(al.getBiblio());
		}
				
			
			}
	private void visibility(boolean b) {
	fAuthorText.setVisible(b);
		yearText.setVisible(b);
	
		commentTextArea.setVisible(b);
		fullRefTextArea.setVisible(b);
	
	}

		
		}
