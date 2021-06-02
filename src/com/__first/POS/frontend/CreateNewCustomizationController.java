package com.__first.POS.frontend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.__first.POS.backend.DatabaseInterface;
import com.__first.POS.backend.Inventory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CreateNewCustomizationController {

	@FXML
	private VBox inventoryVB;
	@FXML
	protected ListView<String> optionList;
	@FXML
	protected TextField customizationName;

	private Inventory inventory;
	protected DatabaseInterface dbi;
	protected ArrayList<HashMap<String, Integer>> inventoryUsage;
	protected ArrayList<BigDecimal> optionCosts;

	public CreateNewCustomizationController(DatabaseInterface dbi, Inventory inventory) {
		this.dbi=dbi;
		this.inventory=inventory;
		inventoryUsage=new ArrayList<>();
		optionCosts=new ArrayList<>();
	}

	public void initialize() {
		optionList.setCellFactory(TextFieldListCell.forListView());
		optionList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			private boolean isSysChange=false;

			@SuppressWarnings("unused")
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValueNum, Number newValueNum) {
				if(isSysChange) {
					isSysChange=false;
					return;
				}
				int oldVal=oldValueNum.intValue();
				int newVal=newValueNum.intValue();
				try {
					saveInventory(oldVal);
					drawInventoryItems();
				}catch(NumberFormatException e) {
					popup("Invalid number", "Number format incorrect", "Please enter costs and inventory counts as numbers.");
					isSysChange=true;
					optionList.getSelectionModel().select(oldVal);
				}
			}
		});
	}

	protected void saveInventory(int i) throws NumberFormatException{
		//Ensure i is in bounds
		if(i==-1 || i>=optionList.getItems().size())
			return;
		ObservableList<Node> list=inventoryVB.getChildren();
		HashMap<String, Integer> map=inventoryUsage.get(i);
		map.clear();
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
				BigDecimal cost=new BigDecimal(tf.getText());
				cost=cost.setScale(2, RoundingMode.HALF_UP);
				optionCosts.set(i, cost);
			}
		}
	}

	private void drawInventoryItems() {
		inventoryVB.getChildren().clear();
		ArrayList<String> items=inventory.getInventoryNames();
		Font font=Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15);

		int listIndex=optionList.getSelectionModel().getSelectedIndex();
		if(listIndex==-1) {
			return;
		}
		HashMap<String, Integer> map=inventoryUsage.get(listIndex);
		
		//Draw cost box
		HBox hbox=new HBox(10);
		Text t=new Text("Cost:");
		t.setFont(font);
		TextField costField=new TextField();
		costField.setFont(font);
		hbox.getChildren().addAll(t, costField);
		inventoryVB.getChildren().add(hbox);
		costField.setText(optionCosts.get(listIndex).toString());
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
		
		//Draw all inventory items
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
					if(!newValue.matches("-?(\\d*)\\z")) {
						String s=newValue.replaceAll("[^-\\d]", "");
						while(s.lastIndexOf('-')>0) {
							String first=s.substring(0, s.lastIndexOf('-'));
							String second=s.substring(s.lastIndexOf('-')+1);
							s=first+second;
						}
						tf.setText(s);
					}
				}
			});

			tf.setDisable(true);
			tf.setVisible(false);
			hb.getChildren().addAll(cb, tf);
			inventoryVB.getChildren().add(hb);

			if(map.containsKey(s)) {
				cb.setSelected(true);
				tf.setDisable(false);
				tf.setVisible(true);
				tf.setText(map.get(s)+"");
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
	private void addOption() {
		optionList.getItems().add("New Option");
		inventoryUsage.add(new HashMap<>());
		optionCosts.add(new BigDecimal("0.00"));
	}

	@FXML
	private void removeOption() {
		int i=optionList.getSelectionModel().getSelectedIndex();
		if(i>=0) {
			optionList.getItems().remove(i);
			inventoryUsage.remove(i);
		}
	}

	@FXML
	private void submitCustomization() {
		Stage stage=(Stage) inventoryVB.getScene().getWindow();
		if(optionList.getItems().size()==0) {
			//No items, just close the window
			stage.close();
			return;
		}else {
			//Store the current options inventory usage
			saveInventory(optionList.getSelectionModel().getSelectedIndex());
		}
		String name=customizationName.getText();
		if(name==null || name.equals("")) {
			popup("Invalid input", "Name cannot be empty", "Please enter a name for this customization.");
			return;
		}

		//Ensure unique option names
		Set<String> set=new HashSet<>(optionList.getItems());
		ArrayList<String> options=new ArrayList<String>(optionList.getItems());
		if(set.size()!=options.size()) {
			popup("Invalid Input", "Duplicate options", "Option names must be unique.");
			return;
		}

		try {
			dbi.addCustomizationAndOptions(name, inventoryUsage, options, optionCosts);
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
			return;
		}
		stage.close();
	}

	/**
	 * Popup dialog for DB error
	 * 
	 * @param header  the header of the popup dialog
	 * @param message the message of the popup dialog
	 */
	protected void popupDB(String header, String message) {
		popup("Database error", header, message);
	}

	protected void popup(String title, String header, String message) {
		Alert alert=new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);

		alert.showAndWait();
	}

}
