package controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import DataStruct.AlimP;
import DataStruct.ConstrainP;
import application.VetNutri;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import model.AlimentUnif;
import model.AnimalEv;
import model.DataAccess;
import model.Vet;
import model.JsonConverter;
import java.nio.file.Files;
import model.BiblioRef;
import model.AlimentEv;
import model.ReferenceEv;

public class ExportController implements Initializable {
	private AlimP transit;
	private AlimentUnif alimU;
	private ObservableList<AlimP> mAlimList;
	private AlimP alAc;
	private Stage stage;
	private String[] _brandList = { "Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola" };
	private Set<String> brandList = new HashSet<>(Arrays.asList(_brandList));
	private AutoCompletionBinding<String> autoCompletionBinding;
	private boolean saved = true;
	private ObservableList<ConstrainP> constrains = FXCollections.observableArrayList();
	boolean abort = false;
	private ResourceBundle bundle;
	private boolean edition = false;
	private String DataKind = "";
	private TableFoodController TFC;
	private Vet vet;
	private TableFoodController TFCa;
	private FoodSearchController FSC;
	private SimpleSearchController2 SSC;
	private TableAnimController TAC;
	private TableAnimController TACa;
	private TableMethodController TMC;
	private TableMethodController TMCa;
	private TableRecipeController TRC;
	private TableRecipeController TRCa;
	private TableRefController TRefC;
	private TableRefController TRefCa;

	@FXML
	private BorderPane mainPane;

	@FXML
	private VBox VBmod;
	@FXML
	private VBox VBmodif;

	@FXML
	private void export() {
		if (DataKind.equals("food")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save food");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("VetBrain food file", "*.vbrf"));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Excel File ", "*.xlsx"));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JSON File", "*.json"));
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				try {

					String extension = FilenameUtils.getExtension(file.getAbsolutePath());
					if (extension.equals("xlsx")) {
						DataAccess.exportAliment(TFCa.getData(), file, vet);
					} else if (extension.equals("json")) {
						// Export JSON
						JsonConverter converter = new JsonConverter();
						List<AlimentEv> aliments = TFCa.getData().stream()
								.map(alimP -> alimP.getAlim())
								.collect(Collectors.toList());
						String jsonContent = converter.alimentEvListToJson(aliments);
						Files.write(file.toPath(), jsonContent.getBytes());
					} else {
						DataAccess.writeAliments(TFCa.getData(), vet, file);
					}

				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
		} else if (DataKind.equals("animal")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("VetBrain animal file", "*.vbra"));
			fileChooser.setTitle("Save animal");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JSON File", "*.json"));
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				try {
					if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("json")) {
						// Export JSON
						JsonConverter converter = new JsonConverter();
						List<AnimalEv> animals = TACa.getData().stream()
								.map(animP -> animP.getAnial())
								.collect(Collectors.toList());
						String jsonContent = converter.animalEvListToJson(animals);
						System.out.println();
						Files.write(file.toPath(), jsonContent.getBytes());
					} else {
						DataAccess.writeAnimals(TACa.getData(), file);
					}
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
		} else if (DataKind.equals("recipe")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("VetBrain recipe file", "*.vbre"));
			fileChooser.setTitle("Save recipe");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JSON File", "*.json"));
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				try {
					if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("json")) {
						// Export JSON
						JsonConverter converter = new JsonConverter();
						String jsonContent = converter.rationListToJson(TRCa.getData());
						Files.write(file.toPath(), jsonContent.getBytes());
					} else {
						DataAccess.writeRecettes(TRCa.getData(), file);
					}
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
		} else if (DataKind.equals("ref")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("VetBrain reference file", "*.vbrr"));
			fileChooser.setTitle("Save ref");
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("JSON File", "*.json"));
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				try {
					if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("json")) {
						// Export JSON
						JsonConverter converter = new JsonConverter();
						List<BiblioRef> refs = new ArrayList<>();
						for (ReferenceEv refEv : TRefCa.getData()) {
							refs.addAll(refEv.getAllBibli());
						}
						String jsonContent = converter.biblioRefListToJson(refs);
						Files.write(file.toPath(), jsonContent.getBytes());
					} else {
						DataAccess.writeReferences(TRefCa.getData(), file);
					}
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
		} else if (DataKind.equals("method")) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("VetBrain method file", "*.vbrm"));
			fileChooser.setTitle("Save method");
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				try {
					DataAccess.writeMethods(TMCa.getData(), file);
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}
		}
	}

	@FXML
	private void addFood() {
		if (DataKind.equals("food")) {
			if (TFC.getSelected() != null) {
				TFCa.addAlim(TFC.getSelected());
			}
		} else if (DataKind.equals("animal")) {
			if (TAC.getSelected() != null) {
				TACa.add(TAC.getSelected());
			}
		} else if (DataKind.equals("recipe")) {
			if (TRC.getSelected() != null) {
				TRCa.add(TRC.getSelected());
			}
		} else if (DataKind.equals("ref")) {
			if (TRefC.getSelected() != null) {
				TRefCa.add(TRefC.getSelected());
			}
		} else if (DataKind.equals("method")) {
			if (TMC.getSelected() != null) {
				TMCa.add(TMC.getSelected());
			}
		}

	}

	@FXML
	private void addAllFood() {

		if (DataKind.equals("food")) {
			TFCa.addAlim(TFC.getData());
		} else if (DataKind.equals("animal")) {

			TACa.add(TAC.getData());
		} else if (DataKind.equals("recipe")) {
			TRCa.add(TRC.getData());
		} else if (DataKind.equals("ref")) {
			TRefCa.add(TRefC.getData());
		} else if (DataKind.equals("method")) {
			TMCa.add(TMC.getData());
		}

	}

	@FXML
	private void removeFood() {

		if (DataKind.equals("food")) {
			TFCa.deleteAlim();
		} else if (DataKind.equals("animal")) {

			TACa.delete();
		} else if (DataKind.equals("recipe")) {
			TRCa.delete();
		} else if (DataKind.equals("ref")) {
			TRefCa.delete();
		} else if (DataKind.equals("method")) {
			TMCa.delete();
		}
	}

	@FXML
	private void removeAllFood() {
		if (DataKind.equals("food")) {
			TFCa.deleteAllAlim();
		} else if (DataKind.equals("animal")) {

			TACa.deleteAll();
		} else if (DataKind.equals("recipe")) {
			TRCa.deleteAll();
		} else if (DataKind.equals("ref")) {
			TRefCa.deleteAll();
		} else if (DataKind.equals("method")) {
			TMCa.deleteAll();
		}

	}

	@FXML
	private void close() {

		stage.close();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		bundle = resources;
		// AlimTabl

	}

	VetNutri mainApp;

	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, String dataKind, Vet vet) {
		this.mainApp = mainApp;

		this.stage = stage;
		this.edition = edition;
		this.vet = vet;
		this.DataKind = dataKind;
		if (DataKind.equals("food")) {
			ObservableList<AlimP> listA = null;

			this.mAlimList = mainApp.getmAlimList();
			listA = mainApp.getmAlimList();
			String[] str = new String[listA.size()];
			for (int i = 0; i < listA.size(); i++) {
				str[i] = listA.get(i).getBrand();
			}
			brandList = new HashSet<>(Arrays.asList(str));

			TFC = setFoodTable(VBmod, false);
			TFC.getAlimTable().getItems().addListener(new ListChangeListener() {
				@Override
				public void onChanged(Change c) {
					// TODO Auto-generated method stub

					TFCa.setID();

				}

			});
			TFCa = setFoodTable(VBmodif, true);
			TFC.getAlimTable().itemsProperty().addListener((observable, oldValue, newValue) -> {

			});
			TFCa.getAlimTable().getItems().addListener(new ListChangeListener() {
				@Override
				public void onChanged(Change c) {
					// TODO Auto-generated method stub
					TFCa.setID();

				}

			});
			TFCa.getAlimTable().getSelectionModel().selectedItemProperty().addListener(

					(observable, oldValue, newValue) -> {
						if (!TFCa.isIndeletion()) {
						}
					});
			FSC = setSearchBand(mainPane, false, TFC.getAlimTable());
			TFC.setItems(FSC.searchAlimList());

		} else if (DataKind == "animal") {
			TAC = setAnimalTable(VBmod, false);
			TACa = setAnimalTable(VBmodif, true);
			SSC = setSimpleSearchBand(mainPane, false, TAC);
			TAC.setItems(mainApp.getmAnimList());
		} else if (DataKind == "recipe") {
			TRC = setRecipeTable(VBmod, false);
			TRCa = setRecipeTable(VBmodif, true);
			SSC = setSimpleSearchBand(mainPane, false, TAC);
			TRC.setItems(mainApp.getmRecetteList());
		} else if (DataKind == "ref") {
			TRefC = setRefTable(VBmod, false);
			TRefCa = setRefTable(VBmodif, true);
			SSC = setSimpleSearchBand(mainPane, false, TAC);
			TRefC.setItems(mainApp.getMainListRef());
		} else if (DataKind == "method") {
			TMC = setMethodTable(VBmod, false);
			TMCa = setMethodTable(VBmodif, true);
			SSC = setSimpleSearchBand(mainPane, false, TAC);
			TMC.setItems(mainApp.getMethodList());
		}
	}

	private TableFoodController setFoodTable(VBox p, boolean anal) {

		TableFoodController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/foodTableView.fxml"));

		try {
			VBox rt = (VBox) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private TableAnimController setAnimalTable(VBox p, boolean anal) {

		TableAnimController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/animalTableView.fxml"));

		try {
			VBox rt = (VBox) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private TableRecipeController setRecipeTable(VBox p, boolean anal) {

		TableRecipeController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/recipeTableView.fxml"));

		try {
			VBox rt = (VBox) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private TableRefController setRefTable(VBox p, boolean anal) {

		TableRefController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/refTableView.fxml"));

		try {
			VBox rt = (VBox) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private TableMethodController setMethodTable(VBox p, boolean anal) {

		TableMethodController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/methodTableView.fxml"));

		try {
			VBox rt = (VBox) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private FoodSearchController setSearchBand(BorderPane p, boolean anal, TableView<AlimP> alimTable) {

		FoodSearchController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/searchBar.fxml"));

		try {
			Accordion rt = (Accordion) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.setTop(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(mainApp, stage, anal, alimTable, vet);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private SimpleSearchController2 setSimpleSearchBand(BorderPane p, boolean anal, TableInterface alimTable) {

		SimpleSearchController2 coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/searchBarSimple.fxml"));

		try {
			Accordion rt = (Accordion) loader.load();
			// rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.setTop(rt);

			coefwin = loader.getController();
			coefwin.setMainApp(mainApp, stage, anal, alimTable);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void exportBiblioRef(TableRefController TRefCa) {
		ArrayList<BiblioRef> biblioRefs = new ArrayList<>();
		ObservableList<ReferenceEv> refs = TRefCa.getData();
		for (ReferenceEv refEv : refs) {
			biblioRefs.addAll(refEv.getAllBibli());
		}
		JsonConverter jsonConverter = new JsonConverter();
		String json = jsonConverter.biblioRefListToJson(biblioRefs);
		// TODO: Save json to file or send it somewhere
	}

}
