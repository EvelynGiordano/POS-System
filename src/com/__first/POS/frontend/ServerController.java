package com.__first.POS.frontend;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

import com.__first.POS.backend.Customization;
import com.__first.POS.backend.DatabaseInterface;
import com.__first.POS.backend.MenuItem;
import com.__first.POS.backend.Order;
import com.__first.POS.backend.OrderItem;
import com.__first.POS.backend.Orders;
import com.__first.POS.backend.PlacedOrderItem;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ServerController {
	
	@FXML private Button logout;
	private final String pathToLogin = "/com/__first/POS/frontend/SignIn.fxml";
	
	
	/* fx:ids for tab switching */
	@FXML private ImageView orderImage;
	@FXML private ImageView cookingImage;
	@FXML private ImageView readyImage;
	@FXML private Tab orderTab;
	@FXML private Tab cookingTab;
	@FXML private Tab readyTab;
	@FXML private TabPane tabs;
	
	//initalized in initTabImages()
	//0/1/2: unselected tab image for order/cooking/ready tabs. 3/4/5: selected tab image for order/cooking/ready tabs
	private Image[] tabImages; 
	
	
	/*  fx:ids for ordering tab  */
	@FXML private GridPane itemList;
	
	@FXML private Text itemName;
	@FXML private Accordion customAccordion;
	@FXML private Label orderItemPrice;
	@FXML private Label totalOrderPriceLabel;
	@FXML private Button addToOrder;
	private BigDecimal totalOrderPrice = BigDecimal.ZERO;
	private ArrayList<Customization> customs;
	private ArrayList<ToggleGroup> buttonGroups; //stores each set of customization options in a togglegroup, and all those groups in here
	
	@FXML private Button submitOrder;
	@FXML private ListView<String> orderDisplay;
	private ArrayList<PlacedOrderItem> currentlyOrdered; //stores all current orders
	
	/* fx:ids for "in progress" */
	@FXML private TextArea displayBox;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> placedByColumn;
    @FXML private TableColumn<Order, Long> orderIdColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, String> itemsColumn;
    @FXML private TableColumn<Order, Void> delOrderCol;
    private Orders orders;
    private String currentServer;
    
    // */

	/* fx:ids for "ready to serve" */
    @FXML private TableColumn<Order, String> readyIDColumn;
    @FXML private TableColumn<Order, Long> readyPlacedColumn;
    @FXML private TableColumn<Order, String> readyItemsColumn;
    @FXML private TableColumn<Order, Void> readyServeColumn;
    @FXML private TableView<Order> readyTable;
    private Orders readyOrders;
   
	
	/* The DatabaseInteface */
	private DatabaseInterface dbi;
	
	// MAIN INITIALIZE METHOD \\
	
	public void initialize() {
		try {
			dbi = DatabaseInterface.getInstance();
		} catch (SQLException e) {
			System.out.println("Error connecting to database");
		}
		
		//System.out.println("hello?");
		
		initTabImages();
		
		initOrderPlacing();
		initInProgress();
		initServing();
		
	}
	
	public void initTabImages() {
		//you can only use this sort of implicit array definition in constructors so we're taking the long way 'round
		Image[] temp = {new Image(getClass().getResource("serv_menu.png").toExternalForm()),
						new Image(getClass().getResource("serv_chef_hat.png").toExternalForm()),
						new Image(getClass().getResource("serv_food.png").toExternalForm()),
						new Image(getClass().getResource("serv_menu_select.png").toExternalForm()),
						new Image(getClass().getResource("serv_chef_hat_select.png").toExternalForm()),
						new Image(getClass().getResource("serv_food_select.png").toExternalForm())   };
		tabImages = temp;
		
		
		
		//fix tab images for .jar export or something?
		orderImage.setImage(tabImages[3]); //default selected tab
		cookingImage.setImage(tabImages[1]);
		readyImage.setImage(tabImages[2]);
		
		//now that we've set up the tabImages array, let's set up the tab switching behavior
		tabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {

				//make tabs and images into an array to avoid a bunch of if statements and consolidate the code
				Tab[] tabs = {orderTab, cookingTab, readyTab};
				ImageView[] imgs = {orderImage, cookingImage, readyImage};
				
				//loop through indices
				for(int i = 0; i < tabs.length; i++) {
					
					//if equal to new tab number, use index i + 3 to get to 4/5/6 in the tabImages array
					if(tabs[i].equals(newTab)) {
						imgs[i].setImage(tabImages[i+3]);
					}
					
					//if equal to the old number, use index i to get to 0/1/2 in the tabImages array
					else if(tabs[i].equals(oldTab)) {
						imgs[i].setImage(tabImages[i]);
					}
				}
			}
		});
		
	}
	
	//====================\\
    //   PLACING ORDERS   \\
    //====================\\
	
	private void initOrderPlacing() {
		loadItemList();
		resetSelectedItem();
		resetOrder();
		
		
	}
	
	//deals with the grid of all items on the left of the screen
	private void loadItemList() {
		//clear all old buttons, hopefully gets rid of "hanging" buttons
		itemList.getChildren().clear();
		
		ArrayList<MenuItem> items;
    	
    	try {
    		items = dbi.retrieveMenuItems();
    	} catch (SQLException e) {
    		items = new ArrayList<MenuItem>();
    	}
    	
    	ArrayList<Button> cur = new ArrayList<Button>();
		Button but;
	    int row = 0, col = 0;
	    
	    for(MenuItem mi: items) {
	    	//reset button name, onclick, and then add it
	    	but = new Button();
	    	but.setPrefSize(91.0, 77.0);
	    	

	    	but.setOnMouseClicked(e -> loadItemToCustomize(mi));
	    	
	    	but.setText(mi.getName());
	    	but.setWrapText(true);
	    	but.setTextAlignment(TextAlignment.CENTER);
	    	
	    	itemList.add(but, col, row);
	    	cur.add(but);
	    	
	    	//abbreviated code to go to the next column, or wrap around to the next row/0th column
	    	col = (col+1) % 4;
	    	row += (col == 0) ? 1 : 0;
	    }
	}
	
	//deals with the currently selected item on the middle/right of the screen
	private void resetSelectedItem() {
		itemName.setText("");
		orderItemPrice.setText("Price: $0.00");
		addToOrder.setDisable(true);
		
		buttonGroups = new ArrayList<ToggleGroup>();
		customAccordion.getPanes().clear();
	}
	private void loadItemToCustomize(MenuItem mi) {
		resetSelectedItem();
		itemName.setText(mi.getName());
		orderItemPrice.setText("Price: $" + mi.getCost());
		addToOrder.setOnMouseClicked(e -> orderItem(mi));
		addToOrder.setDisable(false);
		
		//get list of customizations
		customs = new ArrayList<Customization>();
		
		//populate list
		for(String name: mi.getCustomizations()) {
			try {
				customs.add(dbi.getCustomization(name));
			} catch (SQLException e) {
				System.out.println("Failed to retrieve customization " + name);
			}
		}
		
		//for each customization, create new titled pane and add it to accordion
		for(Customization c: customs) {
			
			if(c == null)
				continue;
			
			//set up titledpane with proper header
			TitledPane tp = new TitledPane();
			tp.setText(c.getName());
			
			//add gridpane to titledpane, this will store the radiobuttons
			GridPane gp = new GridPane();
			tp.setContent(gp);
			int curRow = 0;
			
			//add in selectable options
			ToggleGroup tg = new ToggleGroup();
			for(String o: c.getOptions()) {
				RadioButton rb = new RadioButton(o);
				rb.setToggleGroup(tg);
				gp.add(rb, 0, curRow++); //increment curRow so that next radio button is on next row
			}
			
			//add to accordion
			buttonGroups.add(tg);
			customAccordion.getPanes().add(tp);
		}
	}
	
	//deals with the current order on the right of the screen
	private void resetOrder() {
		submitOrder.setDisable(true);
		orderDisplay.setItems(FXCollections.observableArrayList(new ArrayList<String>()));
		currentlyOrdered=new ArrayList<>();
		totalOrderPrice=BigDecimal.ZERO;
		totalOrderPriceLabel.setText("Total: $0.00");
	}
	private void orderItem(MenuItem mi) {
		//set order button enabled so that you can now press it
		submitOrder.setDisable(false);
		
		String toAdd = mi.getName() + " - $" + mi.getCost();
		totalOrderPrice = totalOrderPrice.add(mi.getCost());
		PlacedOrderItem item = new PlacedOrderItem(mi.getName());
		
		for(ToggleGroup tg: buttonGroups) {
			//gets selected toggle, casts to radio button, then gets radiobutton's text
			RadioButton rb=((RadioButton)tg.getSelectedToggle());
			if(rb!=null) {
				String option=rb.getText();
				BigDecimal cPrice;
				
				
				TitledPane tpane=(TitledPane) rb.getParent().getParent().getParent();
				
				for(Customization c : customs) {
					if(tpane.getText().equals(c.getName())) {
						cPrice=c.getOptionCost(option);
						toAdd+="\n  "+option+" - $"+cPrice;
						totalOrderPrice=totalOrderPrice.add(cPrice);
					}
				}
				
				item.addCustomization(tpane.getText(), tg.getToggles().indexOf(rb));
			}
		}
		totalOrderPriceLabel.setText("Total: $" + totalOrderPrice);
		
		currentlyOrdered.add(item);
		orderDisplay.getItems().add(toAdd);
		
		submitOrder.setOnMouseClicked(e -> {
			try {
				dbi.commitOrder(currentlyOrdered);
				resetSelectedItem();
				resetOrder();
			}catch(IllegalArgumentException e1) {
				//Pretty sure you can't get here not signed in but just in case
				popup("Authentication Error", "User not signed in", "Please sign in.");
			}catch(SQLException e1) {
				popupDB("Database connection error", e1.toString());
				
			}
		});
		
		resetSelectedItem();
	}
	
	
	//=====================\\
    //  IN PROGRESS (tab)  \\
    //=====================\\
	
	private void initInProgress() {
	    	
			// SET UP TABLE COLUMNS
			placedByColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("placedBy"));
			orderIdColumn.setCellValueFactory(new PropertyValueFactory<Order, Long>("orderID"));
			orderTimeColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("orderTime"));
			itemsColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("items"));
			
			ordersTable.setItems(getOrders());
			
			currentServer = dbi.getUser();
			
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
			orders = dbi.getOrdersOnlyCurrentUser();
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
    	
    	
    	displayBox.setText("\n\n" + res);
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
			dbi.deleteOrder(selectedOrder.getOrderID());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
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
                                System.out.println("Deleted");
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
	
	
	//====================\\
    //   SERVING ORDERS   \\
    //====================\\
    
	private void initServing() {
		/* set up buttons */
		readyIDColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("placedBy"));
		readyPlacedColumn.setCellValueFactory(new PropertyValueFactory<Order, Long>("orderID"));
		readyItemsColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("items"));
		
		resetReadyTable();
		addServeButtons();
	}
		
	private void resetReadyTable() {
		ObservableList<Order> list = null;
    	
		try {
    		readyOrders = dbi.getServerReadyOrders();
			list = FXCollections.observableArrayList(readyOrders.getOrders());
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		readyTable.setItems(list);
	}
	
	private void addServeButtons() {
        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = new Callback<TableColumn<Order, Void>, TableCell<Order, Void>>() {
            @Override
            public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
                final TableCell<Order, Void> cell = new TableCell<Order, Void>() {

                    private final Button btn = new Button("Serve");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                        	Order currentOrder = getTableView().getItems().get(getIndex());
                        	Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setTitle("Confirm serving");
                            alert.setHeaderText("Are you sure you're serving the correct order??");
                            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r->event.consume());
                            if (alert.getResult() == ButtonType.OK) {
                            	serveOrder(currentOrder);
                                //System.out.println("Deleted");
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

        readyServeColumn.setCellFactory(cellFactory);
    }
	private void serveOrder(Order selected) {

		readyTable.getItems().remove(selected);
		
		try {
			dbi.markOrderDelivered(selected.getOrderID());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		resetReadyTable();
    }
	
	/**
	 * Popup dialog for DB error
	 * 
	 * @param header  the header of the popup dialog
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
}
