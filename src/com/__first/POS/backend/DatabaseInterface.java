package com.__first.POS.backend;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;

/**
 * Implementation of all SQL code for communication with the database.
 * Also manages a single database connection, which is used for all database communication.
 */
public class DatabaseInterface {

	private static final String DB_URL="jdbc:mysql://pos.csjmdjbzpdab.us-east-1.rds.amazonaws.com/POS";
	private static final String DB_USERNAME="admin";
	private static final String DB_PASSWORD="password";

	public static enum ROLE {
		Chef, Server, Manager
	}

	private static DatabaseInterface instance;
	private static SimpleDateFormat sdf;
	private Connection con;
	private String currentUserID;
	private String currentUserName;

	/**
	 * Creates a new DatabaseInterface instance.
	 * Initiates a connection to the database.
	 * 
	 * @throws SQLException Thrown on database connection error
	 */
	private DatabaseInterface() throws SQLException {
		con=DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
		sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Returns an instance of the DatabaseInterface class.
	 * This instance is only created if one does not already exist.
	 * 
	 * @return An instance of DatabaseInterface
	 * @throws SQLException Thrown on failure to connect to the database
	 */
	public static DatabaseInterface getInstance() throws SQLException {
		if(instance==null)
			instance=new DatabaseInterface();

		return instance;
	}

	/**
	 * Checks the database to see if the given credentials are valid.
	 * Returns 0 for chef, 1 for server, and 2 for manager.
	 * Returns -1 if the userID and password are not valid.
	 * 
	 * @param userID   The entered userID
	 * @param password The entered password
	 * @return The role ID number
	 * @throws SQLException
	 */
	public int signIn(String userID, String password) throws SQLException {
		PreparedStatement ps=con.prepareStatement("select role, name, userID from users where userID=? and password=?");
		ps.setString(1, userID);
		ps.setString(2, password);
		ResultSet resSet=ps.executeQuery();

		if(resSet.next()) {
			int role=resSet.getInt(1);
			this.currentUserName=resSet.getString(2);
			this.currentUserID=resSet.getString(3);
			return role;
		}
		return -1;
	}
	
	/**
	 * Adds a given menu item to the database.
	 * Also takes the inventory the item uses.
	 * 
	 * @param mi             The menu item to be added.
	 * @param inventoryUsage The inventory usage of the added item
	 * @throws SQLException Thrown on database connection error
	 */
	public void addMenuItem(MenuItem mi, HashMap<String, Integer> inventoryUsage) throws SQLException {
		PreparedStatement ps=con.prepareStatement("INSERT INTO menu_item (name, price) VALUES (?,?)");
		ps.setString(1, mi.getName());
		ps.setBigDecimal(2, mi.getCost());
		ps.executeUpdate();

		for(String s : mi.getCustomizations())
			editOption(mi.getName(), s);

		ps=con.prepareStatement("insert into item_uses values (?,?,?)");
		for(Entry<String, Integer> e : inventoryUsage.entrySet()) {
			ps.setString(1, mi.getName());
			ps.setString(2, e.getKey());
			ps.setInt(3, e.getValue());
			ps.addBatch();
		}
		ps.executeBatch();
	}
	
	/**
	 * Removes the given menu item from the database.
	 * 
	 * @param mi The menu item to remove
	 * @throws SQLException Thrown on database connection error
	 */
	public void removeMenuItem(MenuItem mi) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM menu_item WHERE name = ?");
		ps.setString(1, mi.getName());
		ps.executeUpdate();
	}
	
	/**
	 * Edits the old menu item in the database so it matches the new version.
	 * Also updates inventory usage, and available customizations.
	 * 
	 * @param mi         The old menu item
	 * @param newVersion The new version of the menu item
	 * @throws SQLException Thrown on database connection error.
	 */
	public void editMenuItem(MenuItem mi, MenuItem newVersion) throws SQLException {
		PreparedStatement ps = con.prepareStatement("UPDATE menu_item SET name = ?, price = ? WHERE name = ?");
		ps.setString(1, newVersion.getName());
		ps.setBigDecimal(2, newVersion.getCost());
		ps.setString(3, mi.getName());
		ps.executeUpdate();
		
		for(String s: mi.getCustomizations())
			if(!newVersion.getCustomizations().contains(s))
				deleteOption(newVersion.getName(), s);
		
		for(String s: newVersion.getCustomizations())
			if(!mi.getCustomizations().contains(s))
				editOption(newVersion.getName(), s);
		
		//Manage inventory usage
		HashMap<String, Integer> oldInv=mi.getInventoryUsage();
		HashMap<String, Integer> newInv=newVersion.getInventoryUsage();
		ps=con.prepareStatement("delete from item_uses where item_name=? and inventory_name=?");
		for(String s : oldInv.keySet()) {
			if(!newInv.containsKey(s)) {
				//Delete usage
				ps.setString(1, newVersion.getName());
				ps.setString(2, s);
				ps.addBatch();
			}
		}
		ps.executeBatch();
		
		ps=con.prepareStatement("insert into item_uses values (?,?,?)");
		for(String s : newInv.keySet()) {
			if(!oldInv.containsKey(s)) {
				//Add usage
				ps.setString(1, newVersion.getName());
				ps.setString(2, s);
				ps.setInt(3, newInv.get(s));
				ps.addBatch();
			}
		}
		ps.executeBatch();
		
		ps=con.prepareStatement("update item_uses set amount=? where item_name=? and inventory_name=?");
		for(String s: oldInv.keySet()) {
			if(newInv.containsKey(s) && newInv.get(s)!=oldInv.get(s)) {
				//Update usage amount
				ps.setInt(1, newInv.get(s));
				ps.setString(2, newVersion.getName());
				ps.setString(3, s);
				ps.addBatch();
			}
		}
		ps.executeBatch();
	}
	
	/**
	 * Helper method to add a given customization to a menu item.
	 * 
	 * @param itemName The menu item to add the customization to
	 * @param custName The customization to add
	 * @throws SQLException Thrown on database connection error
	 */
	private void editOption(String itemName, String custName) throws SQLException {
		PreparedStatement ps = con.prepareStatement("INSERT INTO available VALUES (?, ?)");
		ps.setString(1, itemName);
		ps.setString(2, custName);
		ps.executeUpdate();
	}
	
	/**
	 * Helper method to delete a given customization from a menu item.
	 * 
	 * @param itemName The menu item to delete the customization from
	 * @param custName The customization to delete
	 * @throws SQLException Thrown on database connection error
	 */
	private void deleteOption(String itemName, String custName) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM available WHERE item_name = ? AND customization_name = ? ");
		ps.setString(1, itemName);
		ps.setString(2, custName);
		ps.executeUpdate();
	}

	/**
	 * Adds a customization and its options.
	 * Also adds options' costs and inventory usages.
	 * 
	 * @param name        The name of the new customization
	 * @param optionUsage The inventory usage of each option
	 * @param optionNames The names of each option
	 * @param optionCosts The cost of each option
	 * @throws SQLException Thrown on database connection error
	 */
	public void addCustomizationAndOptions(String name, ArrayList<HashMap<String, Integer>> optionUsage, ArrayList<String> optionNames,
			ArrayList<BigDecimal> optionCosts) throws SQLException {
		//Add customization
		PreparedStatement ps=con.prepareStatement("insert into customization values (?)");
		ps.setString(1, name);
		ps.executeUpdate();

		//Add options
		ps=con.prepareStatement("insert into customization_option values (?,?,?,?)");
		int id=0;
		for(String s : optionNames) {
			ps.setString(1, name);
			ps.setInt(2, id);
			ps.setString(3, s);
			ps.setBigDecimal(4, optionCosts.get(id));
			ps.addBatch();
			id++;
		}
		ps.executeBatch();

		//Add option inventory usage
		ps=con.prepareStatement("insert into customization_uses values (?,?,?,?)");
		for(int i=0; i<optionUsage.size(); i++) {
			HashMap<String, Integer> map=optionUsage.get(i);
			for(Entry<String, Integer> e : map.entrySet()) {
				if(e.getValue()==0)
					continue;
				ps.setString(1, name);
				ps.setInt(2, i);
				ps.setString(3, e.getKey());
				ps.setInt(4, e.getValue());
				ps.addBatch();
			}
		}
		ps.executeBatch();
	}
	
	/**
	 * Edits a given customization, oldCust, in the database so it matches the new customization, newCust.
	 * Deleting customization options is NOT supported.
	 * 
	 * @param oldCust The old Customization
	 * @param newCust The new Customization
	 * @throws SQLException Thrown on database connection error
	 */
	public void editCustomizationAndOptions(Customization oldCust, Customization newCust) throws SQLException {
		PreparedStatement ps=con.prepareStatement("update customization set name=? where name=?");
		String name=newCust.getName();
		if(!oldCust.getName().equals(newCust.getName())) {
			//Change customization name
			ps.setString(1, name);
			ps.setString(2, oldCust.getName());
			ps.executeUpdate();
		}

		ArrayList<String> oldOptions=oldCust.getOptions();
		ArrayList<String> newOptions=newCust.getOptions();
		ps=con.prepareStatement("insert into customization_option values(?,?,?,?)");
		PreparedStatement ps2=con.prepareStatement("update customization_option set option_name=?, price=? where name=? and optionID=?");
		for(int i=0; i<newOptions.size(); i++) {
			if(i>=oldOptions.size()) {
				//New option
				ps.setString(1, name);
				ps.setInt(2, i);
				ps.setString(3, newOptions.get(i));
				ps.setBigDecimal(4, newCust.getOptionCost(newOptions.get(i)));
				ps.addBatch();
			}else {
				//Rename
				ps2.setString(1, newOptions.get(i));
				ps2.setBigDecimal(2, newCust.getOptionCost(newOptions.get(i)));
				ps2.setString(3, name);
				ps2.setInt(4, i);
				ps2.addBatch();
			}
		}
		ps.executeBatch();
		ps2.executeBatch();

		//Check for modified item usage
		ArrayList<HashMap<String, Integer>> oldUsage=oldCust.getInventoryUsage();
		ArrayList<HashMap<String, Integer>> newUsage=newCust.getInventoryUsage();
		ps=con.prepareStatement("delete from customization_uses where customization_name=? and customization_option=? and inventory_name=?");
		ps2=con.prepareStatement("insert into customization_uses values (?,?,?,?)");
		PreparedStatement ps3=con.prepareStatement(
				"update customization_uses set amount=? where customization_name=? and customization_option=? and inventory_name=?");

		for(int i=0; i<newOptions.size(); i++) {
			if(i>=oldOptions.size()) {
				//New option
				HashMap<String, Integer> nu=newUsage.get(i);
				for(Entry<String, Integer> e : nu.entrySet()) {
					ps2.setString(1, name);
					ps2.setInt(2, i);
					ps2.setString(3, e.getKey());
					ps2.setInt(4, e.getValue());
					ps2.addBatch();
				}
			}else {
				HashMap<String, Integer> ou=oldUsage.get(i);
				HashMap<String, Integer> nu=newUsage.get(i);

				//Removed usage
				for(Entry<String, Integer> oldEntry : ou.entrySet()) {
					if(!nu.containsKey(oldEntry.getKey())) {
						//Delete usage
						ps.setString(1, name);
						ps.setInt(2, i);
						ps.setString(3, oldEntry.getKey());
						ps.addBatch();
					}
				}

				//New usage
				for(Entry<String, Integer> newEntry : nu.entrySet()) {
					if(!ou.containsKey(newEntry.getKey())) {
						//Add usage
						ps2.setString(1, name);
						ps2.setInt(2, i);
						ps2.setString(3, newEntry.getKey());
						ps2.setInt(4, newEntry.getValue());
						ps2.addBatch();
					}
				}

				//Update usage
				for(Entry<String, Integer> oldEntry : ou.entrySet()) {
					if(nu.containsKey(oldEntry.getKey())) {
						//Same inventory name
						if(oldEntry.getValue()!=nu.get(oldEntry.getKey())) {
							//Different usage amount
							//Update usage amount
							ps3.setInt(1, nu.get(oldEntry.getKey()));
							ps3.setString(2, name);
							ps3.setInt(3, i);
							ps3.setString(4, oldEntry.getKey());
							ps3.addBatch();
						}
					}
				}
			}
		}
		ps.executeBatch();
		ps2.executeBatch();
		ps3.executeBatch();
	}
	
	/**
	 * Returns the full list of menu items currently in the database, along with their available customizations.
	 * 
	 * @return An ArrayList of all MenuItems
	 * @throws SQLException Thrown on database connection error
	 */
	public ArrayList<MenuItem> retrieveMenuItems() throws SQLException {
		ArrayList<MenuItem> menu_items = new ArrayList<MenuItem>();
		
		// Get menu items from db and store in ArrayList
		PreparedStatement ps = con.prepareStatement("SELECT name, price FROM menu_item where active=TRUE");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String itemName = rs.getString("name");
			MenuItem mi = new MenuItem(itemName);
			BigDecimal cost=rs.getBigDecimal("price");
			mi.setCost(cost);
			menu_items.add(mi);
		}
		
		// Link the menu items and the customizations together to correspond to each individual item
		ps = con.prepareStatement("SELECT * FROM available");
		rs = ps.executeQuery();
		while (rs.next()) { 
			String itemName = rs.getString("item_name");
			String customizationName = rs.getString("customization_name");
			
			for (MenuItem item : menu_items) {
				if (item.getName().equals(itemName)) 
					item.getCustomizations().add(customizationName);
			}
		}
		
		//Link inventory usage
		ps=con.prepareStatement("select * from item_uses");
		rs=ps.executeQuery();
		while(rs.next()) {
			String itemName=rs.getString(1);
			String invName=rs.getString(2);
			int amount=rs.getInt(3);
			for(MenuItem item : menu_items) {
				if(item.getName().equals(itemName))
					item.addInventoryUsage(invName, amount);
			}
		}
		
		return menu_items;
	}
	
	/**
	 * Fetches the options for the given customization.
	 * 
	 * @param customizationName The name of the customization
	 * @return A Customization object with all options
	 * @throws SQLException Thrown if the database call encounters an exception
	 */
	public Customization getCustomization(String customizationName) throws SQLException {
		PreparedStatement ps=con.prepareStatement("select optionID, option_name, price from customization_option where name=?");
		ps.setString(1, customizationName);
		ResultSet resSet=ps.executeQuery();
		Customization c=new Customization(customizationName);
		while(resSet.next()) {
			int id=resSet.getInt(1);
			String optionName=resSet.getString(2);
			BigDecimal price=resSet.getBigDecimal(3);
			c.addOption(optionName, id, price);
		}
		return c;
	}
	
	/**
	 * Fetches the names of all customizations currently in the database.
	 * 
	 * @return An ArrayList with all customization names
	 * @throws SQLException Thrown on database connection error
	 */
	public ArrayList<String> getAllCustomizations() throws SQLException {
		PreparedStatement ps=con.prepareStatement("select name from customization");
		ResultSet resSet=ps.executeQuery();
		ArrayList<String> l=new ArrayList<String>();
		while(resSet.next()) {
			l.add(resSet.getString(1));
		}
		return l;
	}
	
	/**
	 * Gets the inventory usage for a given customization.
	 * 
	 * @param custName The name of the customization
	 * @return A Customization object with all of its inventory usage
	 * @throws SQLException Thrown on database connection error
	 */
	public Customization getCustomizationIventoryUsage(String custName) throws SQLException {
		Customization c=getCustomization(custName);
		ArrayList<HashMap<String, Integer>> usage=new ArrayList<>();
		ArrayList<BigDecimal> optionCost=new ArrayList<>();
		PreparedStatement ps=con.prepareStatement("SELECT co.optionID, cu.inventory_name, cu.amount, co.price "
				+"FROM customization_option co LEFT JOIN customization_uses cu ON co.name = cu.customization_name "
				+"AND co.optionID = cu.customization_option WHERE name=?");
		ps.setString(1, custName);
		ResultSet resSet=ps.executeQuery();
		
		while(resSet.next()) {
			HashMap<String, Integer> map=new HashMap<>();
			int id=resSet.getInt(1);
			//Check if this id has appeared already (multi-ingredient option)
			if(id<usage.size())
				map=usage.get(id);
			else
				usage.add(map);
			
			String invItem=resSet.getString(2);
			int amount=resSet.getInt(3);
			if(invItem!=null)
				map.put(invItem, amount);
			
			BigDecimal price=resSet.getBigDecimal(4);
			
			if(id>=optionCost.size())
				optionCost.add(price);
		}
		c.setInventoryUsage(usage);
		c.setPriceArray(optionCost);
		return c;
	}

	/**
	 * Adds the given order to the database.
	 * The order consists of an ArrayList of PlacedOrderItems.
	 * The list must have at least one item in it.
	 * 
	 * @param items	The list of PlacedOrderItems making up the order
	 * @throws SQLException	Thrown on database connection error
	 * @throws IllegalArgumentException	Thrown if the given list is null or empty
	 */
	public void commitOrder(ArrayList<PlacedOrderItem> items) throws SQLException, IllegalArgumentException {
		if(items==null || items.size()==0)
			return;
		if(currentUserID==null)
			throw new IllegalArgumentException("No signed in user");
		
		//Add order
		PreparedStatement ps=con.prepareStatement("insert into `order` (placedBy) values(?)", Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, currentUserID);
		ps.execute();
		
		//Extract the auto-generated orderID
		ResultSet resSet=ps.getGeneratedKeys();
		BigDecimal orderID;
		if(resSet.next()) {
			orderID=resSet.getBigDecimal(1);
		}else {
			throw new SQLException("Failed to add new order");
		}
		
		//Add items and link customization options
		ps=con.prepareStatement("insert into item values(?,?,?)");
		PreparedStatement ps2=con.prepareStatement("insert into applied_customization values(?,?,?,?)");
		for(int i=0; i<items.size(); i++) {
			PlacedOrderItem item=items.get(i);
			int itemNum=i+1;
			ps.setBigDecimal(1, orderID);
			ps.setInt(2, itemNum);
			ps.setString(3, item.getName());
			ps.addBatch();
			for(Entry<String, Integer> e : item.getCustomizations().entrySet()) {
				ps2.setInt(1, itemNum);
				ps2.setBigDecimal(2, orderID);
				ps2.setString(3, e.getKey());
				ps2.setInt(4, e.getValue());
				ps2.addBatch();
			}
		}
		ps.executeBatch();
		ps2.executeBatch();
	}
	
	/**
	 * Fetches inventory items, and their usage over the last 7 days.
	 * Access individual InventoryItems from the Inventory object.
	 * 
	 * @return An Inventory object containing all report information.
	 * @throws SQLException Thrown if the database calls encounter an exception.
	 */
	public Inventory getInventoryReport() throws SQLException {
		//Get all of the available ingredients, and their amount & desired_amount
		PreparedStatement ps=con.prepareStatement("select * from inventory");
		ResultSet resSet=ps.executeQuery();
		Inventory inv=new Inventory();
		while(resSet.next()) {
			String name=resSet.getString("name");
			int count=resSet.getInt("amount");
			int desiredAmount=resSet.getInt("desired_amount");
			InventoryItem item=new InventoryItem(name, desiredAmount, count);
			inv.insertItem(item);
		}

		//Setup a time at current date, time 00:00:00.0
		Calendar c=Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		//Go back 6 days
		c.setTimeInMillis(c.getTimeInMillis()-(long) 6*1000*60*60*24);

		//For the next seven days, get inventory usage numbers and write into inv
		for(int i=0; i<7; i++) {
			//Query to get the total number if inventory items used in one day
			//Includes ingredients from menu items, customizations, and inventory_update
			ps=con.prepareStatement("SELECT inventory_name, SUM(amount) FROM (SELECT inventory_name, amount "
					+"FROM `order` NATURAL JOIN item NATURAL JOIN applied_customization ac INNER join customization_uses "
					+"cu on ac.customization_name=cu.customization_name and ac.optionID=cu.customization_option "
					+"WHERE complete_time >= ? AND complete_time < ? UNION ALL SELECT inventory_name, amount "
					+"FROM `order` NATURAL JOIN item right JOIN item_uses on item.menu_item_name=item_uses.item_name "
					+"WHERE complete_time >= ? "
					+"AND complete_time < ? UNION ALL SELECT name AS 'inventory_name', -update_amount AS 'amount' "
					+"FROM inventory_update NATURAL JOIN inventory WHERE date >= ? AND date < ?) t GROUP BY inventory_name");
			ps.setString(1, sdf.format(c.getTime()));
			ps.setString(3, sdf.format(c.getTime()));
			ps.setString(5, sdf.format(c.getTime()));
			c.setTimeInMillis(c.getTimeInMillis()+(long) 1*1000*60*60*24);
			ps.setString(2, sdf.format(c.getTime()));
			ps.setString(4, sdf.format(c.getTime()));
			ps.setString(6, sdf.format(c.getTime()));
			resSet=ps.executeQuery();

			while(resSet.next()) {
				String name=resSet.getString(1);
				int numUsed=resSet.getInt(2);
				inv.getItem(name).addDayCountUsed(numUsed, i);
			}
		}
		
		balanceInventory();

		return inv;
	}

	/**
	 * Returns information for a menu report of which items are most popular.
	 * 
	 * @return Hashmap with Menu Item to Amount Ordered mappings
	 * @throws SQLException Thrown if database calls encounter an exception
	 */
	public HashMap<String, Integer> getMenuReport() throws SQLException {
		PreparedStatement ps=con.prepareStatement("SELECT menu_item_name, COUNT(menu_item_name) FROM `order` NATURAL JOIN "
				+"item WHERE complete_time > CURRENT_TIMESTAMP() - INTERVAL 7 DAY GROUP BY menu_item_name");
		ResultSet resSet=ps.executeQuery();
		HashMap<String, Integer> map=new HashMap<>();
		while(resSet.next()) {
			String name=resSet.getString(1);
			int amount=resSet.getInt(2);
			map.put(name, amount);
		}

		return map;
	}

	/**
	 * For internal use. Balances inventory used by items, customizations, and inventory_update.
	 * Only balances outside of the 7 day window of inventory reports.
	 * 
	 * @throws SQLException Thrown on database connection error
	 */
	private void balanceInventory() throws SQLException {
		//TODOL Delete expired items from inventory_last_balanced and inventory_update
		PreparedStatement ps=con.prepareStatement("select * from inventory_last_balanced order by date desc limit 1");
		ResultSet resSet=ps.executeQuery();

		//Determine date of last inventory balance. If it does not exist, set last balance to 0 epoch
		//This is the start date for balancing
		Calendar cStart=Calendar.getInstance();
		cStart.setTimeZone(TimeZone.getTimeZone("GMT"));
		if(resSet.next()) {
			Timestamp t=resSet.getTimestamp(1);
			cStart.setTimeInMillis(t.toInstant().toEpochMilli());
		}else {
			cStart.setTimeInMillis(0);
		}
		//Setup a time at current date, time 00:00:00.0
		//This will be the last day a report covers. Inventory outside this window should be balanced.
		Calendar cEnd=Calendar.getInstance();
		cEnd.set(Calendar.HOUR_OF_DAY, 0);
		cEnd.set(Calendar.MINUTE, 0);
		cEnd.set(Calendar.SECOND, 0);
		cEnd.set(Calendar.MILLISECOND, 0);
		//Go back 6 days
		cEnd.setTimeInMillis(cEnd.getTimeInMillis()-(long) 6*1000*60*60*24);
		if(cStart.getTimeInMillis()>=cEnd.getTimeInMillis()) {
			//Last balance within report window, do nothing
			return;
		}

		ps=con.prepareStatement("insert into inventory_last_balanced values (?)");
		ps.setString(1, sdf.format(cEnd.getTime()));
		ps.executeUpdate();
		//Select for all inventory usage from items, customizations, and edits in date range
		ps=con.prepareStatement("SELECT inventory_name, SUM(amount) FROM (SELECT inventory_name, amount FROM "
				+"`order` NATURAL JOIN item NATURAL JOIN applied_customization ac INNER JOIN customization_uses cu "
				+"ON ac.customization_name = cu.customization_name AND ac.optionID = cu.customization_option "
				+"WHERE complete_time > ? AND complete_time <= ? UNION ALL SELECT inventory_name, amount FROM `order` "
				+"NATURAL JOIN item right JOIN item_uses ON item.menu_item_name = item_uses.item_name WHERE "
				+"complete_time > ? AND complete_time <= ? UNION ALL SELECT name AS 'inventory_name', "
				+"- update_amount AS 'amount' FROM inventory_update NATURAL JOIN inventory WHERE date > ? AND date <= ?) "
				+"t GROUP BY inventory_name");
		ps.setString(1, sdf.format(cStart.getTime()));
		ps.setString(3, sdf.format(cStart.getTime()));
		ps.setString(5, sdf.format(cStart.getTime()));
		ps.setString(2, sdf.format(cEnd.getTime()));
		ps.setString(4, sdf.format(cEnd.getTime()));
		ps.setString(6, sdf.format(cEnd.getTime()));
		resSet=ps.executeQuery();

		HashMap<String, Integer> inv=new HashMap<String, Integer>();

		while(resSet.next()) {
			String name=resSet.getString(1);
			int numUsed=resSet.getInt(2);
			inv.put(name, numUsed);
		}

		ps=con.prepareStatement("update inventory set amount=amount-? where name=?");
		for(String s : inv.keySet()) {
			ps.setInt(1, inv.get(s));
			ps.setString(2, s);
			ps.addBatch();
		}
		int[] res=ps.executeBatch();
		for(int i : res) {
			if(i==Statement.SUCCESS_NO_INFO)
				System.out.println("Success no info?");
			else if(i==Statement.EXECUTE_FAILED)
				System.out.println("Execute failed?");
		}

	}

	/**
	 * Gets the inventory for manager.
	 * Use the returned Inventory object for adding, editing, and removing menu items.
	 * 
	 * @return The current inventory
	 * @throws SQLException Thrown on database error
	 */
	public Inventory getInventory() throws SQLException {
		PreparedStatement ps=con.prepareStatement("select * from inventory_last_balanced order by date desc limit 1");
		ResultSet resSet=ps.executeQuery();

		//Determine date of last inventory balance. If it does not exist, set last balance to 0 epoch
		Calendar cStart=Calendar.getInstance();
		cStart.setTimeZone(TimeZone.getTimeZone("GMT"));
		if(resSet.next()) {
			Timestamp t=resSet.getTimestamp(1);
			cStart.setTimeInMillis(t.toInstant().toEpochMilli());
		}else {
			cStart.setTimeInMillis(0);
		}
		
		ps=con.prepareStatement("SELECT inventory_name, SUM(amount) FROM (SELECT inventory_name, amount "
				+"FROM `order` NATURAL JOIN item NATURAL JOIN applied_customization ac "
				+"RIGHT JOIN customization_uses cu ON ac.customization_name = cu.customization_name "
				+"AND cu.customization_option = ac.optionID  WHERE complete_time > ? UNION ALL SELECT "
				+"inventory_name, amount  FROM `order` NATURAL JOIN item i "
				+"RIGHT JOIN item_uses iu ON i.menu_item_name = iu.item_name WHERE complete_time > ? "
				+"UNION ALL SELECT name AS 'inventory_name', -update_amount AS 'amount' "
				+"FROM inventory_update NATURAL JOIN inventory WHERE date > ?) t GROUP BY inventory_name");
		ps.setString(1, sdf.format(cStart.getTime()));
		ps.setString(2, sdf.format(cStart.getTime()));
		ps.setString(3, sdf.format(cStart.getTime()));
		resSet=ps.executeQuery();
		HashMap<String, Integer> adjust=new HashMap<String, Integer>();
		while(resSet.next()) {
			String s=resSet.getString(1);
			int x=resSet.getInt(2);
			adjust.put(s, x);
		}
		
		ps=con.prepareStatement("select * from inventory");
		resSet=ps.executeQuery();
		Inventory inv=new Inventory();
		while(resSet.next()) {
			String name=resSet.getString("name");
			int count=resSet.getInt("amount");
			int desiredAmount=resSet.getInt("desired_amount");
			if(adjust.containsKey(name))
				count-=adjust.get(name);
			InventoryItem item=new InventoryItem(name, desiredAmount, count);
			inv.insertItem(item);
		}

		return inv;
	}

	/**
	 * Commits all changes to the given inventory to the database.
	 * Returns an ArrayList<String> for items that could not be removed because they are used by an item or customization.
	 * 
	 * @param inv The inventory to commit changes from
	 * @return An ArrayList of failed removes, otherwise null
	 * @throws SQLException Thrown on database error
	 */
	public ArrayList<String> commitInventoryChanges(Inventory inv) throws SQLException {
		//Add items to database
		ArrayList<InventoryItem> added=inv.getAdditions();
		PreparedStatement ps=con.prepareStatement("insert into inventory values (?,?,?)");
		for(InventoryItem item : added) {
			ps.setString(1, item.getName());
			ps.setInt(2, item.getAmount());
			ps.setInt(3, item.getDesiredAmount());
			ps.addBatch();
		}
		//Doesn't do anything if no additions
		int[] res=ps.executeBatch();
		for(int i : res) {
			if(i==Statement.SUCCESS_NO_INFO)
				System.out.println("Success no info?");
			else if(i==Statement.EXECUTE_FAILED)
				System.out.println("Execute failed?");
		}

		//Edit items in database
		HashMap<InventoryItem, InventoryItem> edited=inv.getEdits();
		ps=con.prepareStatement("update inventory set name=?, desired_amount=? where name=?");
		PreparedStatement updateps=con.prepareStatement("insert into inventory_update (name, update_amount) VALUES (?,?)");
		for(InventoryItem newItem : edited.keySet()) {
			InventoryItem oldItem=edited.get(newItem);
			int amountUpdate=oldItem.getAmount()-newItem.getAmount();
			ps.setString(1, newItem.getName());
			ps.setInt(2, newItem.getDesiredAmount());
			ps.setString(3, oldItem.getName());
			ps.addBatch();
			if(amountUpdate!=0) {
				updateps.setString(1, newItem.getName());
				updateps.setInt(2, -amountUpdate);
				updateps.addBatch();
			}
		}
		//Doesn't do anything if no items
		res=ps.executeBatch();
		for(int i : res) {
			if(i==Statement.SUCCESS_NO_INFO)
				System.out.println("Success no info?");
			else if(i==Statement.EXECUTE_FAILED)
				System.out.println("Execute failed?");
		}
		res=updateps.executeBatch();
		for(int i : res) {
			if(i==Statement.SUCCESS_NO_INFO)
				System.out.println("Success no info?");
			else if(i==Statement.EXECUTE_FAILED)
				System.out.println("Execute failed?");
		}

		//Remove items from database
		ArrayList<InventoryItem> removed=inv.getRemoved();
		ArrayList<String> removeFailed=new ArrayList<String>();
		for(InventoryItem item : removed) {
			ps=con.prepareStatement("delete from inventory where name=?");
			ps.setString(1, item.getName());
			int touched=0;
			try {
				touched=ps.executeUpdate();
			}catch(SQLIntegrityConstraintViolationException e) {
				//Cannot delete item because it is referenced by a menu item or customization
				touched=-1;
			}
			if(touched!=1)
				removeFailed.add(item.getName());
		}

		if(removeFailed.size()==0)
			return null;
		return removeFailed;
	}
	
	/**
	 * Retrieves items that a chef has marked as finished, but have not been delivered by a server.
	 * Only returns orders placed by the current user.
	 * Intended for display to servers.
	 * 
	 * @return Orders marked completed by chef but not yet delivered by a server
	 * @throws SQLException Thrown on database connection error
	 */
	public Orders getServerReadyOrders() throws SQLException {
		Orders orders=new Orders();
		PreparedStatement ps;
		ps=con.prepareStatement("SELECT `order`.orderID, `item`.item_num, users.name, input_time, menu_item_name, "
				+"option_name FROM `order` JOIN users ON `order`.placedBy = users.userID NATURAL JOIN "
				+"item LEFT JOIN applied_customization ON item.item_num = applied_customization.item_num "
				+"AND item.orderID = applied_customization.orderID LEFT JOIN customization_option ON "
				+"applied_customization.customization_name = customization_option.name "
				+"AND applied_customization.optionID = customization_option.optionID WHERE completedBy IS NOT NULL "
				+"AND deliveredBy IS NULL AND userID = ? ORDER BY input_time , orderID , item_num");
		ps.setString(1, currentUserID);
		ResultSet resSet=ps.executeQuery();
		Order order=null;
		OrderItem item=null;
		while(resSet.next()) {
			long orderID=resSet.getLong(1);
			int itemNum=resSet.getInt(2);
			String name=resSet.getString(3);
			Timestamp t=resSet.getTimestamp(4);
			Calendar c=Calendar.getInstance();
			c.setTimeInMillis(t.toInstant().toEpochMilli());
			String itemName=resSet.getString(5);
			String optionName=resSet.getString(6);
			if(order==null) {
				//No order yet, create one
				order=new Order(orderID, name, c);
				//Also means there is no item, create one
				item=new OrderItem(itemName, itemNum);
			}else if(order.getOrderID()!=orderID) {
				//New orderID, add old item to order and old order to orders
				order.addItem(item);
				item=new OrderItem(itemName, itemNum);

				orders.addOrder(order);
				order=new Order(orderID, name, c);
			}else if(item==null || item.getItemNum()!=itemNum) {
				//New item, add old item to order
				order.addItem(item); //Null orders are discarded
				item=new OrderItem(itemName, itemNum);
			}
			if(optionName!=null)
				item.addCustomization(optionName);

		}
		if(item!=null) {
			order.addItem(item);
			orders.addOrder(order);
		}
		return orders;
	}

	/**
	 * Marks the given order as complete in the database.
	 * 
	 * @param orderID The order to mark delivered
	 * @throws SQLException Thrown on database connection error
	 */
	public void markOrderDelivered(long orderID) throws SQLException {
		PreparedStatement ps=con.prepareStatement("update `order` set deliveredBy=?, delivered_time=current_timestamp() where orderID=?");
		ps.setString(1, currentUserID);
		ps.setLong(2, orderID);
		ps.executeUpdate();
	}
	
	/**
	 * Returns all active orders.
	 * 
	 * @return All active orders
	 * @throws SQLException Thrown on database connection error
	 */
	public Orders getOrders() throws SQLException {
		return getOrders(false);
	}
	
	/**
	 * Returns only the current user's active orders.
	 * 
	 * @return The current user's active orders
	 * @throws SQLException Thrown on database connection error
	 */
	public Orders getOrdersOnlyCurrentUser() throws SQLException {
		return getOrders(true);
	}
	
	/**
	 * Helper method to get the current orders.
	 * Either returns all orders, or only orders placed by the current user.
	 * 
	 * @param currentUserOnly True if only the current user's orders should be returned
	 * @return Relevant orders (either all, or current users)
	 * @throws SQLException Thrown on database connection error
	 */
	private Orders getOrders(boolean currentUserOnly) throws SQLException {
		Orders orders=new Orders();
		PreparedStatement ps;
		ps=con.prepareStatement("SELECT `order`.orderID, `item`.item_num, users.name, input_time, menu_item_name, "
				+"option_name FROM `order` JOIN users ON `order`.placedBy = users.userID NATURAL JOIN "
				+"item LEFT JOIN applied_customization ON item.item_num = applied_customization.item_num "
				+"AND item.orderID = applied_customization.orderID LEFT JOIN customization_option ON "
				+"applied_customization.customization_name = customization_option.name "
				+"AND applied_customization.optionID = customization_option.optionID WHERE completedBy IS NULL "
				+"ORDER BY input_time , orderID , item_num");
		if(currentUserOnly && currentUserID!=null && !currentUserID.equals("")) {
			ps=con.prepareStatement("SELECT `order`.orderID, `item`.item_num, users.name, input_time, menu_item_name, "
					+"option_name FROM `order` JOIN users ON `order`.placedBy = users.userID NATURAL JOIN "
					+"item LEFT JOIN applied_customization ON item.item_num = applied_customization.item_num "
					+"AND item.orderID = applied_customization.orderID LEFT JOIN customization_option ON "
					+"applied_customization.customization_name = customization_option.name "
					+"AND applied_customization.optionID = customization_option.optionID WHERE completedBy IS NULL "
					+"AND users.userID=? ORDER BY input_time , orderID , item_num");
			ps.setString(1, currentUserID);
		}
		ResultSet resSet=ps.executeQuery();
		Order order=null;
		OrderItem item=null;
		while(resSet.next()) {
			long orderID=resSet.getLong(1);
			int itemNum=resSet.getInt(2);
			String name=resSet.getString(3);
			Timestamp t=resSet.getTimestamp(4);
			Calendar c=Calendar.getInstance();
			c.setTimeInMillis(t.toInstant().toEpochMilli());
			String itemName=resSet.getString(5);
			String optionName=resSet.getString(6);
			if(order==null) {
				//No order yet, create one
				order=new Order(orderID, name, c);
				//Also means there is no item, create one
				item=new OrderItem(itemName, itemNum);
			}else if(order.getOrderID()!=orderID) {
				//New orderID, add old item to order and old order to orders
				order.addItem(item);
				item=new OrderItem(itemName, itemNum);

				orders.addOrder(order);
				order=new Order(orderID, name, c);
			}else if(item==null || item.getItemNum()!=itemNum) {
				//New item, add old item to order
				order.addItem(item); //Null orders are discarded
				item=new OrderItem(itemName, itemNum);
			}
			if(optionName!=null)
				item.addCustomization(optionName);

		}
		if(item!=null) {
			order.addItem(item);
			orders.addOrder(order);
		}
		return orders;
	}

	/**
	 * Marks the given order as completed in the database.
	 * 
	 * @param orderID The ID of the order
	 * @throws SQLException Thrown on database connection error
	 */
	public void markOrderCompleted(long orderID) throws SQLException {
		PreparedStatement ps=con.prepareStatement("update `order` set completedBy=?, complete_time=current_timestamp() where orderID=?");
		ps.setString(1, currentUserID);
		ps.setLong(2, orderID);
		ps.executeUpdate();
	}
	
	/**
	 * Adds the given user to the database.
	 * userID must not be null, or an empty string.
	 * name must not be null, or an empty string.
	 * role must not be null.
	 * password must not be null.
	 * Uses the enum ROLE defined in DatabaseInterface.
	 * 
	 * @param userID   The userID for the user
	 * @param name     The name for the user
	 * @param role     The role for the user
	 * @param password The password for the user
	 * @return True if user can be added to the system, false otherwise
	 * @throws SQLException             Thrown on database connection error
	 * @throws IllegalArgumentException Thrown if an argument is null
	 */
	public boolean addUser(String userID, String name, ROLE role, String password) throws SQLException, IllegalArgumentException {
		if(userID==null || userID.equals(""))
			throw new IllegalArgumentException("userID must not be null, or empty");
		if(password==null)
			throw new IllegalArgumentException("password must not be null");
		if(name==null || name.equals(""))
			throw new IllegalArgumentException("name must not be null, or empty");
		if(role==null)
			throw new IllegalArgumentException("role must not be null");

		int roleNum=0;
		switch(role) {
			case Chef:
				roleNum=0;
				break;
			case Server:
				roleNum=1;
				break;
			case Manager:
				roleNum=2;
				break;
		}

		PreparedStatement ps=con.prepareStatement("insert into users values (?, ?, ?, ?)");
		ps.setString(1, userID);
		ps.setString(2, password);
		ps.setInt(3, roleNum);
		ps.setString(4, name);
		try {
			ps.executeUpdate();
		}catch(SQLIntegrityConstraintViolationException e) {
			return false;
		}

		return true;
	}
	
	/**
	 * Deletes the given order.
	 * 
	 * @param orderId The ID of the order to be deleted
	 * @throws SQLException Thrown on database connection error
	 */
	public void deleteOrder(long orderId) throws SQLException {
		PreparedStatement ps = con.prepareStatement("DELETE FROM POS.order WHERE orderID = ?");
		ps.setLong(1, orderId);
		ps.executeUpdate();
	}
	
	/**
	 * Returns the name of the current logged in user.
	 * 
	 * @return The current user's name. Null if no logged in user.
	 */
	public String getUser() {
		return currentUserName;
	}

}