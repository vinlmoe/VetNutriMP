package controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DataConnector;
import application.VetNutri;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.AlimDBList;
import model.Vet;
import model.alimDB;

public class createDBController implements Initializable{
@FXML
private TextField nameText;
@FXML
private TextArea descArea;
	

@FXML
public void continuer() {
	if (!nameText.getText().isBlank()) {
		alimDB al=new alimDB(nameText.getText(), descArea.getText());
		mainApp.updateDB(al);
		DataConnector.addAlimDB(al, null);
		stage.close();
	}
	
	
}

@FXML
public void  cancel() {
	stage.close();
}
private Stage stage;
	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

	
	}

	FoodEditorControler mainApp;
	public void setMainApp( Stage stage, FoodEditorControler v) {
		
		this.stage=stage;
	this.mainApp=v;

	
	
	this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		public void handle(WindowEvent we) {
		
			stage.close();
		}
	});        
		
	}



	
}
