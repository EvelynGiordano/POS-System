package com.__first.POS.frontend;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.__first.POS.backend.DatabaseInterface;
import com.__first.POS.backend.Inventory;
import com.__first.POS.backend.MenuItem;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SelectItemUsageController {

	@FXML
	private VBox inventoryVB;

	private Inventory inventory;
	private DatabaseInterface dbi;
	private String itemName;
	private ArrayList<String> customizations;
	private HashMap<String, Integer> existingUsage;
	private MenuItem oldMI;
	private BigDecimal price;
	private boolean edit;

	public SelectItemUsageController(DatabaseInterface dbi, Inventory inventory, String itemName, MenuItem oldMI) {
		this.dbi=dbi;
		this.inventory=inventory;
		this.itemName=itemName;
		customizations=new ArrayList<>();
		this.oldMI=oldMI;
		this.edit=oldMI!=null;
		if(edit) {
			existingUsage=oldMI.getInventoryUsage();
			price=oldMI.getCost();
		}
	}

	public void initialize() {
		drawInventoryItems();
	}

	public void addCustomization(String customization) {
		customizations.add(customization);
	}

	private void drawInventoryItems() {
		ArrayList<String> items=inventory.getInventoryNames();
		Font font=Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15);
		
		//Draw cost box
		HBox hbox=new HBox(10);
		Text t=new Text("Cost:");
		t.setFont(font);
		TextField costField=new TextField();
		costField.setFont(font);
		hbox.getChildren().addAll(t, costField);
		inventoryVB.getChildren().add(hbox);
		if(price!=null)
			costField.setText(price.toString());
		else
			costField.setText("0.00");
		//Restrict text field values
		costField.textProperty().addListener(new ChangeListener<String>() {

			@SuppressWarnings("unused")
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(!newValue.matches("-?(\\d*)\\z")) {
					String s=newValue.replaceAll("[^-\\d.]", "");
					//Deal with -'s
					//One allowed at start of string to make a negative number
					while(s.lastIndexOf('-')>0) {
						String first=s.substring(0, s.lastIndexOf('-'));
						String second=s.substring(s.lastIndexOf('-')+1);
						s=first+second;
					}
					//Deal with .'s
					if(s.chars().filter(c -> c=='.').count()>1) {
						//New string has multiple .'s
						int firstIndex=s.indexOf('.');
						while(s.lastIndexOf('.')>firstIndex) {
							String first=s.substring(0, s.lastIndexOf('.'));
							String second=s.substring(s.lastIndexOf('.')+1);
							s=first+second;
						}
					}
					//Only allow 2 numbers after the '.'
					if(s.indexOf('.')<s.length()-3) {
						s=s.substring(0, s.indexOf('.')+3);
					}
					costField.setText(s);
				}
			}
		});
		
		for(String s : items) {
			HBox hb=new HBox(10);
			hb.setAlignment(Pos.CENTER_LEFT);
			CheckBox cb=new CheckBox(s);
			cb.setOnAction(this::checkTicked);
			cb.setFont(font);
			TextField tf=new TextField("1");
			tf.setPrefColumnCount(2);
			tf.setFont(font);
			tf.setAlignment(Pos.CENTER_RIGHT);
			// force the field to be numeric only
			tf.textProperty().addListener(new ChangeListener<String>() {
				@SuppressWarnings("unused")
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					if(!newValue.matches("\\d*")) {
						tf.setText(newValue.replaceAll("[^\\d]", ""));
					}
				}
			});

			tf.setDisable(true);
			tf.setVisible(false);
			hb.getChildren().addAll(cb, tf);
			inventoryVB.getChildren().add(hb);

			if(existingUsage!=null && existingUsage.containsKey(s)) {
				cb.setSelected(true);
				tf.setDisable(false);
				tf.setVisible(true);
				tf.setText(existingUsage.get(s)+"");
			}
		}
	}

	@FXML
	private void checkTicked(ActionEvent e) {
		CheckBox cb=(CheckBox) e.getSource();
		HBox hb=(HBox) cb.getParent();
		TextField tf=(TextField) hb.getChildren().get(1);
		boolean state=cb.isSelected();
		tf.setDisable(!state);
		tf.setVisible(state);
	}

	@FXML
	private void finishMenuItem() {
		ObservableList<Node> list=inventoryVB.getChildren();
		HashMap<String, Integer> map=new HashMap<>();
		for(Node n : list) {
			HBox hb=(HBox) n;
			Node node=hb.getChildren().get(0);
			if(node instanceof CheckBox) {
				CheckBox cb=(CheckBox) node;
				if(cb.isSelected()) {
					TextField tf=(TextField) hb.getChildren().get(1);
					int num=Integer.parseInt(tf.getText());
					if(num!=0)
						map.put(cb.getText(), num);
				}
			}else if(node instanceof Text) {
				TextField tf=(TextField) hb.getChildren().get(1);
				try {
					price=new BigDecimal(tf.getText());
				}catch(NumberFormatException e) {
					popup("Invalid number", "Number format incorrect", "Please enter costs and inventory counts as numbers.");
					return;
				}
			}
		}

		MenuItem item=new MenuItem(itemName, customizations);
		item.setCost(price);
		try {
			if(!edit) {
				dbi.addMenuItem(item, map);
			}else {
				item.setInventoryUsage(map);
				dbi.editMenuItem(oldMI, item);
			}
			Stage stage=(Stage) inventoryVB.getScene().getWindow();
			stage.close();
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
		}
	}
	
	/**
	 * Popup dialog for DB error
	 * 
	 * @param header   the header of the popup dialog
	 * @param message the message of the popup dialog
	 */
	private void popupDB(String header, String message) {
		popup("Database error", header, message);
	}
	
	private void popup(String title, String header, String message) {
		Alert alert=new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		
		alert.showAndWait();
	}

}
