package com.__first.POS.frontend;

import com.__first.POS.backend.Inventory;
import com.__first.POS.backend.InventoryItem;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

public class ModifyInventoryController {
	@FXML public TextField modifyItemName;
    @FXML public TextField modifyQuantity;
    @FXML public TextField modifyDesired;
    @FXML private Button applyChanges;
    public InventoryItem currentItem;
    public Inventory inventory;
    public InventoryItem addedItem;
    public String modification;
    
    
    @FXML
    void initialize(){
    	modifyItemName.setText(currentItem.getName());
    	modifyItemName.setDisable(true);
    	modifyQuantity.setText(Integer.toString(currentItem.getOriginalCount()));
    	modifyDesired.setText(Integer.toString(currentItem.getDesiredAmount()));
    	modifyQuantity.setFocusTraversable(false);
    	modifyDesired.setFocusTraversable(false);
    	applyChanges.setFocusTraversable(false);
    }
    
    @FXML
    void applyEditChanges(ActionEvent event) {
    	if(!(modifyQuantity.getText().contains("[0-9]+") && !(modifyDesired.getText().contains("[0-9]+")))) {
    		if(!modifyQuantity.getText().isBlank() && !modifyDesired.getText().isBlank()) {
    			inventory.editInventoryItem(currentItem, currentItem.getName(), Integer.parseInt(modifyQuantity.getText()), Integer.parseInt(modifyDesired.getText()));
    			Node source = (Node) event.getSource();
    		    Stage stage = (Stage) source.getScene().getWindow();
    		    stage.close();
    		}
    		
    	} else {
    		popup("Invalid Quantity or Desired Amount", "Quantity and Desired Quantity must contain only digits");
    	}
    	
    }
    
    public void popup(String title, String message) {
		Alert alert=new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
}
