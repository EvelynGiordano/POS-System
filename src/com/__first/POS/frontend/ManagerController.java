package com.__first.POS.frontend;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.__first.POS.backend.DatabaseInterface;
import com.__first.POS.backend.Inventory;
import com.__first.POS.backend.InventoryItem;
import com.__first.POS.backend.MenuItem;
import com.__first.POS.backend.Order;
import com.__first.POS.backend.OrderItem;
import com.__first.POS.backend.Orders;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ManagerController {
	
    @FXML private Button logout;
    @FXML private ImageView orderImage;
    @FXML private ImageView menuImage;
    @FXML private ImageView inventoryImage;
    @FXML private ImageView reportsImage;
    @FXML private ImageView usersImage;
    @FXML private TabPane tabs;
    @FXML private Tab orderTab;
    @FXML private Tab menuTab;
    @FXML private Tab inventoryTab;
    @FXML private Tab reportTab;
    @FXML private Tab userTab;
    @FXML private Label comboLabel;
	
	/* fx:ids for orders tab */
    private Orders orders;
    //@FXML private Button orderEditButton;
    //@FXML private Button deleteOrderButton;
    @FXML private TextArea displayBox;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> placedByColumn;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, String> itemsColumn;
    @FXML private TableColumn<Order, Void> delOrderCol;
	
	/* fx:ids for menu tab */
    @FXML private GridPane menuButtons;
    @FXML private Button menuNIButton; // add a new item
    
    @FXML private TextField itemName;
    @FXML private ListView<String> customizeList;
    @FXML private ListView<String> customizeAppliedList;
    @FXML private Button menuFButton; // finished
    @FXML private Button menuDButton; // delete
    @FXML private Button applyCustomizationButton;
    @FXML private Button removeCustomizationButton;
    
    /* fx:ids for inventory tab */
    @FXML private TextField inventorySearch;
    @FXML private TableView<InventoryItem> table;
    @FXML private TableColumn<InventoryItem, String> item;
    @FXML private TableColumn<InventoryItem, Integer> quantity;
    @FXML private TableColumn<InventoryItem, Integer> desiredQuantity;
    @FXML private TableColumn<InventoryItem, Void> editCol;
    @FXML private TableColumn<InventoryItem, Void> delCol;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private BarChart<String, Number> graph;
    @FXML private ComboBox<String> combo;
	public Inventory inventory;
	private XYChart.Series<String, Number> series1;
    
    
    /* fx:ids for reports tab */
    
    private final String pathToLogin = "/com/__first/POS/frontend/SignIn.fxml";
    private static DatabaseInterface db;
    private ObservableList<InventoryItem> inv;
    private ObservableList<InventoryItem> search;
    private ObservableList<InventoryItem> resultSet;
    private ArrayList<InventoryItem> searchSet;
    
    /* fx:ids for users tab */
    
    @FXML private ComboBox<String> roles;
    @FXML private TextField firstName;
    @FXML private TextField userID;
    @FXML private PasswordField password;
    
    public void initialize() {
    	try {
    		db = DatabaseInterface.getInstance();
    	} 
    	catch (SQLException e) { }
    	
    	initOrders();
    	initMenuItems();
    	initUsers();
    	
    	try {
			initInventory();
		} catch (SQLException e) {
			System.out.println("Initializing inventory failed");
			e.printStackTrace();
		}
    	
    	//Fix images in .jar export
    	orderImage.setImage(new Image(getClass().getResource("orderSelected.png").toExternalForm()));
    	menuImage.setImage(new Image(getClass().getResource("menu.png").toExternalForm()));
    	inventoryImage.setImage(new Image(getClass().getResource("grocery-merchandising.png").toExternalForm()));
    	reportsImage.setImage(new Image(getClass().getResource("report.png").toExternalForm()));
    	usersImage.setImage(new Image(getClass().getResource("team.png").toExternalForm()));
    	
    	tabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() { 
    	    @Override 
    	    public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
    	        if(newTab.equals(orderTab)) {
    	        	orderImage.setImage(new Image(getClass().getResource("orderSelected.png").toExternalForm()));
    	        	menuImage.setImage(new Image(getClass().getResource("menu.png").toExternalForm()));
    	        	inventoryImage.setImage(new Image(getClass().getResource("grocery-merchandising.png").toExternalForm()));
    	        	reportsImage.setImage(new Image(getClass().getResource("report.png").toExternalForm()));
    	        	usersImage.setImage(new Image(getClass().getResource("team.png").toExternalForm()));
    	        }
    	        if(newTab.equals(menuTab)) {
    	        	orderImage.setImage(new Image(getClass().getResource("shopping-cart.png").toExternalForm()));
    	        	menuImage.setImage(new Image(getClass().getResource("menuSelected.png").toExternalForm()));
    	        	inventoryImage.setImage(new Image(getClass().getResource("grocery-merchandising.png").toExternalForm()));
    	        	reportsImage.setImage(new Image(getClass().getResource("report.png").toExternalForm()));
    	        	usersImage.setImage(new Image(getClass().getResource("team.png").toExternalForm()));
    	        }
				if(newTab.equals(inventoryTab)) {
					orderImage.setImage(new Image(getClass().getResource("shopping-cart.png").toExternalForm()));
			    	menuImage.setImage(new Image(getClass().getResource("menu.png").toExternalForm()));
			    	inventoryImage.setImage(new Image(getClass().getResource("inventorySelected.png").toExternalForm()));
			    	reportsImage.setImage(new Image(getClass().getResource("report.png").toExternalForm()));
			    	usersImage.setImage(new Image(getClass().getResource("team.png").toExternalForm()));
    	        }
				if(newTab.equals(reportTab)) {
					orderImage.setImage(new Image(getClass().getResource("shopping-cart.png").toExternalForm()));
			    	menuImage.setImage(new Image(getClass().getResource("menu.png").toExternalForm()));
			    	inventoryImage.setImage(new Image(getClass().getResource("grocery-merchandising.png").toExternalForm()));
			    	reportsImage.setImage(new Image(getClass().getResource("reportSelected.png").toExternalForm()));
			    	usersImage.setImage(new Image(getClass().getResource("team.png").toExternalForm()));
    	        }
				if(newTab.equals(userTab)) {
					orderImage.setImage(new Image(getClass().getResource("shopping-cart.png").toExternalForm()));
			    	menuImage.setImage(new Image(getClass().getResource("menu.png").toExternalForm()));
			    	inventoryImage.setImage(new Image(getClass().getResource("grocery-merchandising.png").toExternalForm()));
			    	reportsImage.setImage(new Image(getClass().getResource("report.png").toExternalForm()));
			    	usersImage.setImage(new Image(getClass().getResource("userSelected.png").toExternalForm()));
    	        }
    	    }
    	});
    	
    	
    }
    
    //===================\\
    //   ORDERS TAB   	 \\
    //===================\\
    
    private void initOrders() {
    	
		// SET UP TABLE COLUMNS
		placedByColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("placedBy"));
		orderIdColumn.setCellValueFactory(new PropertyValueFactory<Order, Long>("orderID"));
		orderTimeColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("orderTime"));
		itemsColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("items"));
		
		ordersTable.setItems(getOrders());
		
		editableCols();
		addDeleteOrderBtn();
		
		// Display order items on hover 
		ordersTable.setRowFactory(tableView -> {
		    final TableRow<Order> row = new TableRow<>();

		    row.hoverProperty().addListener((observable) -> {
		        Order orderItems = row.getItem();

		        if (row.isHover() && orderItems != null) {
		        	displayOrderItems(orderItems.getItems());
		        } else {
		        	displayBox.setText(""); 
		        }
		    });

		    return row;
		});
		
    }
    
    private ObservableList<Order> getOrders() {
    	ObservableList<Order> list = null;
    	try {
			orders = db.getOrders();
			list = FXCollections.observableArrayList(orders.getOrders());
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return list;
    }
    
    private void displayOrderItems(ArrayList<OrderItem> items) {
    	String res = "";
    	
    	for (OrderItem oi :items) {
    		String orderName = oi.getName();
    		String customizations = oi.getCustomizations().toString().replace("[", "(").replace("]", ")");
    		if (customizations.equals("()"))
    			res += orderName + "\n";
    		else
    			res += orderName + ": " + customizations + "\n";
    	}
    	
    	
    	displayBox.setText("\n\n\n" + res);
    }
    
    private void editableCols() {
    	
    	placedByColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    	placedByColumn.setOnEditCommit(e -> {
    		e.getTableView().getItems().get(e.getTablePosition().getRow()).setPlacedBy(e.getNewValue());
    	});
    	
    	orderTimeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    	orderTimeColumn.setOnEditCommit(e -> {
    		e.getTableView().getItems().get(e.getTablePosition().getRow()).setOrderTime(e.getNewValue());
    	});
    	
    	ordersTable.setEditable(true);
    	
    }
    
    private void removeSelectedOrder(Order selectedOrder) {
		//Order selectedItem = ordersTable.getSelectionModel().getSelectedItem();
		ordersTable.getItems().remove(selectedOrder);
		try {
			db.deleteOrder(selectedOrder.getOrderID());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
    }
    
    //===================\\
    //   MENU ITEM TAB   \\
    //===================\\
    
    private void initMenuItems() {
    	customizeList.setCellFactory(TextFieldListCell.forListView());
    	menuNIButton.setOnAction(e -> newBlankItem());
    	
    	displayCurMenu();
    	resetMenu();
    }
    
    /* code block for making new, blank items because it's slightly different from editing new blocks:
        - creating a new menu item out of the void rather than editing one which exists
        - canceling creation rather than deleting an existing item
     */
    private void newBlankItem() {
    	itemName.clear();
    	itemName.setPromptText("- item name here -");
    	customizeAppliedList.setItems(FXCollections.observableArrayList(new ArrayList<String>()));
    	try {
			customizeList.setItems(FXCollections.observableArrayList(db.getAllCustomizations()));
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
		}
    	
    	menuFButton.setOnAction(e -> createMI());
    	menuDButton.setOnAction(e -> resetMenu());
    	
    	enableEditMenu();
    }

    
	@FXML
	private void applyCustomization() {
		String item=customizeList.getSelectionModel().getSelectedItem();
		if(item!=null) {
			customizeList.getItems().remove(item);
			customizeAppliedList.getItems().add(item);
		}
	}

	@FXML
	private void removeCustomization() {
		String item=customizeAppliedList.getSelectionModel().getSelectedItem();
		if(item!=null) {
			customizeAppliedList.getItems().remove(item);
			customizeList.getItems().add(item);
		}
	}
    
    //code for loading and editing extant menu items
    private void loadMenuItemToEdit(MenuItem mi) {
    	itemName.setText(mi.getName());
    	customizeAppliedList.setItems(FXCollections.observableArrayList(mi.getCustomizations()));
    	try {
			ArrayList<String> allCust=db.getAllCustomizations();
			for(String s : mi.getCustomizations())
				allCust.remove(s);
			ObservableList<String> customizeObsList=customizeList.getItems();
			customizeObsList.clear();
			for(String s : allCust)
				customizeObsList.add(s);
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
		}
    	
    	menuFButton.setOnAction(e -> editMI(mi));
    	menuDButton.setOnAction(e -> deleteItem(mi));
    	
    	enableEditMenu();
    }

    private void createMI() {
    	editMI(null);
    }

    private void editMI(MenuItem mi) {
    	if(itemName.getText().equals("")) {
    		popup("Invalid item name", "The menu item must have a name.");
    		return;
    	}
    	try {
    		FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/__first/POS/frontend/SelectItemUsage.fxml"));
    		Inventory inv=db.getInventory();
    		SelectItemUsageController controller=new SelectItemUsageController(db, inv, itemName.getText(), mi);
    		for(String s : customizeAppliedList.getItems()) {
    			controller.addCustomization(s);
    		}
    		loader.setController(controller);
    		Scene s=new Scene((Parent) loader.load());
    		Stage window=new Stage();
    		window.setScene(s);
    		window.centerOnScreen();
    		window.setOnHidden(e -> initMenuItems());
    		window.show();
    	}catch(IOException e) {
    		popup("Window Failed", "Failed to open a new window", ""+e);
    	}catch(SQLException e) {
    		popupDB("Database connection error", ""+e);
    	}
    }
    
    private void deleteItem(MenuItem mi) {
    	try {
    		db.removeMenuItem(mi);
    	} catch (SQLException e) {
			System.out.println("Deleting menu item failed");
		}
    	
    	displayCurMenu();
    	resetMenu();
    }
    
    //resets the menu, especially the right hand side
    private void displayCurMenu() {
    	//clear all old buttons, hopefully gets rid of "hanging" buttons
    	menuButtons.getChildren().clear();
    	
    	ArrayList<MenuItem> items;
    	
    	try {
    		items = db.retrieveMenuItems();
    	} catch (SQLException e) {
    		items = new ArrayList<MenuItem>();
    	}
    	
    	
		Button but;
		
    	menuButtons.add(menuNIButton, 0, 0); //we removed everything, so add back in the default '+' button
	    int row = 0, col = 1; //start at col 1 because of default '+' button
	    
	    for(MenuItem mi: items) {
	    	//reset button name, onclick, and then add it
	    	but = new Button();
	    	but.setPrefSize(91.0, 77.0);
	    	
	    	but.setOnAction(e -> loadMenuItemToEdit(mi));
	    	
	    	but.setText(mi.getName());
	    	but.setWrapText(true);
	    	but.setTextAlignment(TextAlignment.CENTER);
	    	
	    	menuButtons.add(but, col, row);
	    	
	    	//abbreviated code to go to the next column, or wrap around to the next row/0th column
	    	col = (col+1) % 5;
	    	row += (col == 0) ? 1 : 0;
	    }
    }
    
    private void enableEditMenu() {
    	itemName.setEditable(true);
    	
    	menuFButton.setDisable(false);
    	menuDButton.setDisable(false);
    	applyCustomizationButton.setDisable(false);
    	removeCustomizationButton.setDisable(false);
    }
    
    private void resetMenu() {
    	itemName.setText("No item selected");
    	itemName.setEditable(false);
    	customizeList.getItems().clear();
    	customizeAppliedList.getItems().clear();
    	
    	menuFButton.setDisable(true);
    	menuDButton.setDisable(true);
    	applyCustomizationButton.setDisable(true);
    	removeCustomizationButton.setDisable(true);
    }
    
    @FXML
    private void createNewCustomization() {
    	try {
			FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/__first/POS/frontend/CreateNewCustomization.fxml"));
    		Inventory inv=db.getInventory();
			CreateNewCustomizationController controller=new CreateNewCustomizationController(db, inv);
			loader.setController(controller);
			Scene s=new Scene((Parent) loader.load());
			Stage window=new Stage();
			window.setScene(s);
			window.centerOnScreen();
			window.setOnHidden(e -> initMenuItems());
			window.show();
		}catch(IOException e) {
			popup("Window Failed", "Failed to open a new window", ""+e);
		}catch(SQLException e) {
			popupDB("Database connection error", ""+e);
		}
    }
    
    @FXML
    private void modifyExistingCustomization() {
    	try {
    		FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/__first/POS/frontend/CreateNewCustomization.fxml"));
    		Inventory inv=db.getInventory();
    		ArrayList<String> customizationNames=db.getAllCustomizations();
    		ModifyCustomizationController controller=new ModifyCustomizationController(db, inv, customizationNames);
    		loader.setController(controller);
    		Scene s=new Scene((Parent) loader.load());
    		Stage window=new Stage();
    		window.setScene(s);
    		window.centerOnScreen();
    		window.setOnHidden(e -> initMenuItems());
    		window.show();
    	}catch(IOException e) {
    		popup("Window Failed", "Failed to open a new window", ""+e);
    	}catch(SQLException e) {
    		popupDB("Database connection error", ""+e);
    	}
    }
    
    //===================\\
    //   INVENTORY TAB   \\
    //===================\\
    
    /**
     * Initializes the manager's view of the inventory 
     * and assigns an ArrayList of InventoryItem objects to the JFX table
     * @throws SQLException
     */
	public void initInventory() throws SQLException {
		//Get an instance of the database
		//and get the inventory
		if(inventory==null)
			inventory=db.getInventory();
		resultSet = inventory.getInventoryList();
		inv = resultSet;
		
		//apply database info
		//to the table
		item.setCellValueFactory(new PropertyValueFactory<InventoryItem, String>("name"));
		quantity.setCellValueFactory(new PropertyValueFactory<InventoryItem, Integer>("originalCount"));
		desiredQuantity.setCellValueFactory(new PropertyValueFactory<InventoryItem, Integer>("desiredAmount"));
		item.setStyle("-fx-alignment: CENTER-LEFT;"); //needs to be done in css
		table.setPlaceholder(new Label("No search results"));
		table.setItems(inv);
		
		addEditButtonToTable();
		addDeleteButtonToTable();
		
		ObservableList<String> observableList = FXCollections.observableList(inventory.getInventoryNames());
    	combo.setItems(observableList);
    	combo.getSelectionModel().selectFirst();
    	setInventoryReport();
		
	}
	
	@FXML
	void addInventoryItem(ActionEvent event) {
		//open a new window
		//show options to input item into the database
		try {
			FXMLLoader loader=new FXMLLoader(getClass().getResource("/com/__first/POS/frontend/ModifyInventory.fxml"));
			AddInventoryController controller=new AddInventoryController();
			controller.inventory=inventory;
			loader.setController(controller);
			Scene s=new Scene((Parent) loader.load());
			Stage window=new Stage();
			window.setTitle("Add Inventory Item");
			window.setScene(s);
			window.centerOnScreen();
			window.show();
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    @FXML
    private void saveInventoryChanges() {
    	try {
			ArrayList<String> failed=db.commitInventoryChanges(inventory);
			if(failed != null)
				popup("Deletion Failed!", "The item " + failed.toString() + " is in use and may not be deleted.");
			else
				popup("Save Successful!", "Your changes have been saved.");
			inventory = db.getInventory();
			resultSet = inventory.getInventoryList();
			ObservableList<String> observableList = FXCollections.observableList(inventory.getInventoryNames());
	    	combo.setItems(observableList);
	    	combo.getSelectionModel().selectFirst();
	    	setInventoryReport();
			table.setItems(resultSet);
			
		}catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    void deleteInventoryItem(ActionEvent event) {
    	//delete from database
    }


    @FXML
    private void logout(ActionEvent event) throws IOException {
    	 Parent root = FXMLLoader.load(getClass().getResource(pathToLogin));
         Scene s = new Scene(root);
         s.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
         Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
         window.setScene(s);
         window.centerOnScreen();
         window.show();
    }
    
    @FXML
    void SetInventoryReport(ActionEvent event) throws IOException, SQLException {
    	setInventoryReport();
    }
    
    @FXML
    void SetMenuReport(ActionEvent event) throws IOException, SQLException {
    	setMenuReport();
    }
   
    
    private void addEditButtonToTable()  {
        Callback<TableColumn<InventoryItem, Void>, TableCell<InventoryItem, Void>> cellFactory = new Callback<TableColumn<InventoryItem, Void>, TableCell<InventoryItem, Void>>() {
            @Override
            public TableCell<InventoryItem, Void> call(final TableColumn<InventoryItem, Void> param) {
                final TableCell<InventoryItem, Void> cell = new TableCell<InventoryItem, Void>() {

                    private final Button btn = new Button("Edit");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                        	try {
                        		 FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/__first/POS/frontend/ModifyInventory.fxml"));
                        	     ModifyInventoryController controller = new ModifyInventoryController();
                        	     controller.currentItem = getTableView().getItems().get(getIndex());
//                        	     controller.modifyItemName.setDisable(false);
                        	     controller.inventory = inventory;

                        	     loader.setController(controller);
                        	     Scene s = new Scene((Parent) loader.load());
                        	     Stage window = new Stage();
                        	     window.setTitle("Edit Inventory Item");
                        		 window.setScene(s);
                        		 window.centerOnScreen();
                        		 window.show();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        editCol.setCellFactory(cellFactory);

    }
    
    private void addDeleteButtonToTable() {
        Callback<TableColumn<InventoryItem, Void>, TableCell<InventoryItem, Void>> cellFactory = new Callback<TableColumn<InventoryItem, Void>, TableCell<InventoryItem, Void>>() {
            @Override
            public TableCell<InventoryItem, Void> call(final TableColumn<InventoryItem, Void> param) {
                final TableCell<InventoryItem, Void> cell = new TableCell<InventoryItem, Void>() {

                    private final Button btn = new Button("Delete");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                        	InventoryItem  currentItem = getTableView().getItems().get(getIndex());
                        	Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Confirm Delete");
                            alert.setHeaderText("Are you sure you want to delete this item?");
                            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r->event.consume());
                            if (alert.getResult() == ButtonType.OK) {
                            	inventory.removeInventoryItem(currentItem);
                                System.out.println();
                            }
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        delCol.setCellFactory(cellFactory);

    }
    
    private void addDeleteOrderBtn() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<TableColumn<Order, Void>, TableCell<Order, Void>>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<Order, Void>() {

                    private final Button btn = new Button("Delete");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                        	Order currentOrder = getTableView().getItems().get(getIndex());
                        	Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Confirm Delete");
                            alert.setHeaderText("Are you sure you want to delete this item?");
                            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r->event.consume());
                            if (alert.getResult() == ButtonType.OK) {
                            	removeSelectedOrder(currentOrder);
                            }
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                };
                return cell;
            }
        };

        delOrderCol.setCellFactory(cellFactory);

    }
    
    private void setInventoryReport() {
    	try {
    		if(combo.getValue() != null) {
				Inventory inv = db.getInventoryReport();
				InventoryItem i = inv.getItem(combo.getValue());
				graph.getData().clear();
				graph.layout();
				graph.setAnimated(true);
				graph.setTitle(i.getName());
				combo.setVisible(true);
				comboLabel.setVisible(true);
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
				
				Calendar today = Calendar.getInstance();
				Calendar yest1 = Calendar.getInstance();
				Calendar yest2 = Calendar.getInstance();
				Calendar yest3 = Calendar.getInstance();
				Calendar yest4 = Calendar.getInstance();
				Calendar yest5 = Calendar.getInstance();
				Calendar yest6 = Calendar.getInstance();
				
				today.add(Calendar.DAY_OF_WEEK, 0);
				yest1.add(Calendar.DAY_OF_WEEK, -1);
				yest2.add(Calendar.DAY_OF_WEEK, -2);
				yest3.add(Calendar.DAY_OF_WEEK, -3);
				yest4.add(Calendar.DAY_OF_WEEK, -4);
				yest5.add(Calendar.DAY_OF_WEEK, -5);
				yest6.add(Calendar.DAY_OF_WEEK, -6);
				
				series1 = new Series<String, Number>();
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest6.getTime()), i.getDayCount(0)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest5.getTime()), i.getDayCount(1)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest4.getTime()), i.getDayCount(2)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest3.getTime()), i.getDayCount(3)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest2.getTime()), i.getDayCount(4)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(yest1.getTime()), i.getDayCount(5)));
				series1.getData().add(new Data<String, Number>(dateFormat.format(today.getTime()), i.getDayCount(6)));
				graph.getData().add(series1);
			graph.setAnimated(false);
    		}
		}catch(SQLException e) {
			popup("Database connection error", e.toString());
		}
    }
    
    private void setMenuReport() {
    	try {
			HashMap<String, Integer> menuReport = db.getMenuReport();
			ArrayList<MenuItem> menu = db.retrieveMenuItems();
			graph.getData().clear();
			graph.layout();
			graph.setTitle("Menu Item Popularity");
			graph.setAnimated(true);
			combo.setVisible(false);
			comboLabel.setVisible(false);
			
			series1 = new Series<String, Number>();
			Iterator hmIterator = menuReport.entrySet().iterator();
			
			System.out.println(menuReport.isEmpty());
			List<Map.Entry<String, Integer>> menuList = new ArrayList<Map.Entry<String, Integer>>();
			
			while (hmIterator.hasNext()) { 
	            Map.Entry<String, Integer> mapElement = (Map.Entry)hmIterator.next();
	            String item = mapElement.getKey();
	            Integer num = mapElement.getValue();
	            menuList.add(mapElement);
	        }
			
			
			Collections.sort(menuList, new Comparator<Map.Entry<String, Integer>>() {
			  @Override
			  public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
			    	 if(o1.getValue() > o2.getValue())
			    		 return -1;
			    	 else if(o1.getValue() == o2.getValue())
			    		 return 0;
			    	 else if(o1.getValue() < o2.getValue())
			    		 return 1;
			    	 else
			    		 return 0;
			  }
			});
			
			for(Map.Entry<String, Integer> i : menuList){
				series1.getData().add(new Data<String, Number>(i.getKey(), i.getValue()));
			}
			
			for(MenuItem m : menu) {
				if(!menuReport.containsKey(m.getName()))
					series1.getData().add(new Data<String, Number>(m.getName(), 0));
			}
			
			
			graph.getData().add(series1);
			graph.setAnimated(false);
		}catch(SQLException e) {
			popup("Database connection error", e.toString());
		}
    }
    
    //===================\\
    //   ADD USERS TAB   \\
    //===================\\
    
    
    public void initUsers() {
    	ArrayList<String> list = new ArrayList<String>() {{
    	    add("Manager");
    	    add("Chef");
    	    add("Server");
    	}};
    	ObservableList<String> observableList = FXCollections.observableList(list);
    	roles.setItems(observableList);
    	roles.getSelectionModel().selectFirst();
    }
    
    @FXML
    void addNewUser(ActionEvent event) throws IOException, SQLException {

    	if(firstName.getText().isBlank() || userID.getText().isBlank() 
    			|| password.getText().isBlank() || firstName.getText().contains("[0-9]+")) {
    		if(firstName.getText().contains("[0-9]+")) {
        		popup("Invalid Entry", "Name must contain only alphabetic characters");
        	}else {
        		popup("Incomplete Fields", "All entries are required before creating a new user.");
        	}
    		
    	}else {
    			if (!db.addUser(userID.getText(), firstName.getText(), 
        				DatabaseInterface.ROLE.valueOf(roles.getSelectionModel().getSelectedItem()), password.getText())) {
    				popup("Error", "The username you have entered is already in use.");
    			}else {
    				db.addUser(userID.getText(), firstName.getText(), 
            				DatabaseInterface.ROLE.valueOf(roles.getSelectionModel().getSelectedItem()), password.getText());
    				popup("", "User Added Successfully");
    			}
    			
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

    public void popup(String title, String message) {
		Alert alert=new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
}
