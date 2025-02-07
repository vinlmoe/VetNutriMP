package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import DataStruct.SpeciesConverter;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import graph.component.AutocompletionlTextField;
import graph.component.FloatEditingCell;
import graph.component.StringEditingCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AnimalEv;
import model.Espece;
import model.Sex;
import model.Vet;
import model.targetAdjust;

public class OptionController  implements Initializable {
	private Vet vet;

	@FXML
	private TextField lastText;
	@FXML
	private TextField firstText;
	@FXML
	private TextField streetText;
	@FXML
	private TextField zipText;
	@FXML
	private TextField licenseText;
	@FXML
	private TextField cityText;
	@FXML
	private TextField Text;
	@FXML
	private VBox vb;

	@FXML
	private ComboBox<String> langCombo;
	private AdviseController AC;
	@FXML
	private Slider slideSize;

	@FXML
	public void continuer() {
		vet.setAdresse(streetText.getText());
		vet.setCodePost(zipText.getText());
		vet.setLanguage(langCombo.getSelectionModel().getSelectedItem());
		vet.setVille(cityText.getText());
		vet.setScale((float)slideSize.getValue());
		vet.setAdviseList(AC.getItems());
		vet.setNom(lastText.getText());
		vet.setPrenom(firstText.getText());
		mainApp.setVet(vet);
		stage.close();
	}
	private Stage stage;
	@Override 
	public void initialize(URL location, ResourceBundle resources) {
		langCombo.getItems().add("Français");
		langCombo.getItems().add("English");
		langCombo.getSelectionModel().selectFirst();
	}
	VetNutri mainApp;
	public void setMainApp( Stage stage, VetNutri v, Vet vet) {
		this.vet=vet;
		this.stage=stage;
		this.mainApp=v;
		slideSize.setValue(vet.getScale());
		this.langCombo.setValue(vet.getLanguage());
		this.cityText.setText(vet.getVille());
		this.firstText.setText(vet.getPrenom());
		this.lastText.setText(vet.getNom());
		this.streetText.setText(vet.getAdresse());
		this.zipText.setText(vet.getCodePost());
		this.licenseText.setText(vet.getOrdre());
		this.AC=setAdvise(vb);
	}
	private AdviseController setAdvise (VBox p) {
		AdviseController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(mainApp.getBun());
		loader.setLocation(VetNutri.class.getResource("/view/adviseView.fxml"));
		try {
			SplitPane rt=(SplitPane)loader.load();
		
			p.getChildren().add(rt);
			coefwin=loader.getController();
			coefwin.setMainApp(vet.getAdviseList());
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}






}
