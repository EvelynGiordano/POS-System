package com.__first.POS.frontend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.__first.POS.backend.Customization;
import com.__first.POS.backend.DatabaseInterface;
import com.__first.POS.backend.Inventory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ModifyCustomizationController extends CreateNewCustomizationController {

	@FXML
	private Button removeOptionButton;

	private ChoiceBox<String> customizationSelect;
	private ArrayList<String> customizationNames;
	private Customization oldCustomization;

	public ModifyCustomizationController(DatabaseInterface dbi, Inventory inventory, ArrayList<String> customizationNames) {
		super(dbi, inventory);
		this.customizationNames=customizationNames;
		oldCustomization=null;
	}

	@Override
	public void initialize() {
		super.initialize();

		//Add in customization ChoiceBox
		BorderPane bpane=(BorderPane) customizationName.getParent().getParent();
		VBox vb=new VBox(10);
		vb.setAlignment(Pos.TOP_CENTER);
		customizationSelect=new ChoiceBox<String>(FXCollections.observableArrayList(customizationNames));
		customizationSelect.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@SuppressWarnings("unused")
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					oldCustomization=dbi.getCustomizationIventoryUsage(newValue);
					optionList.setItems(FXCollections.observableArrayList(oldCustomization.getOptions()));
					customizationName.setText(newValue);
					inventoryUsage=oldCustomization.getInventoryUsage();
					optionCosts=oldCustomization.getPriceArray();
				}catch(SQLException e) {
					popupDB("Database connection error", ""+e);
					return;
				}
			}
		});
		vb.getChildren().addAll(customizationSelect, customizationName.getParent());
		bpane.topProperty().setValue(vb);

		//Delete removeOptionButton
		HBox hb=(HBox) removeOptionButton.getParent();
		hb.getChildren().remove(removeOptionButton);
	}

	@FXML
	private void submitCustomization() {
		Stage stage=(Stage) customizationName.getScene().getWindow();
		if(optionList.getItems().size()==0) {
			//No items, just close the window
			stage.close();
			return;
		}else {
			//Store the current options inventory usage
			saveInventory(optionList.getSelectionModel().getSelectedIndex());
		}
		String newName=customizationName.getText();
		if(newName==null || newName.equals("")) {
			popup("Invalid Input", "Name cannot be empty", "Please enter a name for this customization.");
			return;
		}

		//Ensure unique option names
		Set<String> set=new HashSet<>(optionList.getItems());
		ArrayList<String> options=new ArrayList<String>(optionList.getItems());
		if(set.size()!=options.size()) {
			popup("Invalid Input", "Duplicate options", "Option names must be unique.");
			return;
		}

		Customization newCustomization=new Customization(newName, inventoryUsage);
		for(int i=0; i<optionList.getItems().size(); i++) {
			newCustomization.addOption(optionList.getItems().get(i), i, optionCosts.get(i));
		}

		try {
			dbi.editCustomizationAndOptions(oldCustomization, newCustomization);
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
			return;
		}
		stage.close();
	}
}
