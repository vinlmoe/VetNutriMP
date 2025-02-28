package controller;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.ListSelectionView;

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
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
import model.Advise;
import model.AnimalEv;
import model.ConsultationEv;
import model.Espece;
import model.PdfExport;
import model.Ration;
import model.Sex;
import model.Vet;
import model.targetAdjust;

public class OrdonnanceController  implements Initializable {
	private Vet vet;
private AnimalEv anim;
private ConsultationEv cons;
	@FXML
	private ListSelectionView<Ration> rationSelection;
	@FXML
	private ListSelectionView<Advise>adviseSelection;
	

	@FXML
	private TextArea adviseArea;


	
	@FXML
	public void continuer() {
		ArrayList<Advise>al= new ArrayList<Advise>(adviseSelection.getTargetItems());
		if (!adviseArea.getText().isBlank()) {
		al.add(new Advise("",adviseArea.getText()));}
		new PdfExport().createOrdonnancePdf(anim, cons, new ArrayList<Ration>(rationSelection.getTargetItems()), al, vet, mainApp.getBun());
		stage.close();
	}
	@FXML
	public void PressePapier() {
	 Toolkit toolkit = Toolkit.getDefaultToolkit();
    Clipboard clipboard = toolkit.getSystemClipboard();
    ArrayList<Advise>al= new ArrayList<Advise>(adviseSelection.getTargetItems());
	if (!adviseArea.getText().isBlank()) {
	al.add(new Advise("",adviseArea.getText()));}
	
    // La chaine de caractères à insérer dans le presse-papier
    String textToInsert = 	new PdfExport().createOrdonnancePP(anim, cons, new ArrayList<Ration>(rationSelection.getTargetItems()), al, vet, mainApp.getBun());
    StringSelection stringSelection = new StringSelection(textToInsert);

    // Insertion dans le presse-papier
    clipboard.setContents(stringSelection, null);
	}
	
	@FXML
	public void cancel() {
	
		stage.close();
	}
	private Stage stage;
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		adviseSelection.setCellFactory(cell -> new ListCell<Advise>() {

           
            final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(Advise advice, boolean empty) {
                super.updateItem(advice, empty);

                if (advice == null || empty) {
              
                    setText(null);
                    setTooltip(null);
                } else {
                    // A advice is to be listed in this cell
                    setText(advice.getName());

                    // Let's show our Author when the user hovers the mouse cursor over this row
                    tooltip.setText(advice.getText());
                    setTooltip(tooltip);
                }
            }
        });
		
		
	}
	VetNutri mainApp;
	public void setMainApp( AnimalEv anim, ConsultationEv cons, Vet vet, VetNutri v, Stage stage ) {
		this.vet=vet;
		this.stage=stage;
		this.mainApp=v;
		this.anim=anim;
		this.cons=cons;
		rationSelection.setSourceHeader(new Label(v.getBun().getString("dispRat")));
		rationSelection.setTargetHeader(new Label(v.getBun().getString("selectRat")));
		adviseSelection.setSourceHeader(new Label(v.getBun().getString("dispAdv")));
		adviseSelection.setTargetHeader(new Label(v.getBun().getString("selectAdv")));
		for (Ration r:cons.getRationList()) {
			if (r.isActual()) {
	rationSelection.getTargetItems().add(r);}
			else {
				rationSelection.getSourceItems().add(r);
			}
		}
		adviseSelection.getSourceItems().addAll(vet.getAdviseList());
	}
	






}
