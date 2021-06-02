package com.__first.POS.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Inventory {

	private ObservableList<InventoryItem> inventory;
	private ArrayList<InventoryItem> added;
	private ArrayList<InventoryItem> removed;
	private HashMap<InventoryItem, InventoryItem> edited; //<NewItem, OldItem>

	protected Inventory(ArrayList<InventoryItem> inventory) {
		this.inventory=FXCollections.observableList(inventory);
		added=new ArrayList<InventoryItem>();
		removed=new ArrayList<InventoryItem>();
		edited=new HashMap<InventoryItem, InventoryItem>();
	}

	protected Inventory() {
		inventory=FXCollections.observableList(new ArrayList<InventoryItem>());
		added=new ArrayList<InventoryItem>();
		removed=new ArrayList<InventoryItem>();
		edited=new HashMap<InventoryItem, InventoryItem>();
	}

	protected void insertItem(InventoryItem item) {
		inventory.add(item);
	}
	
	protected ArrayList<InventoryItem> getAdditions(){
		return added;
	}
	
	protected HashMap<InventoryItem, InventoryItem> getEdits(){
		return edited;
	}
	
	protected ArrayList<InventoryItem> getRemoved(){
		return removed;
	}

	/**
	 * Returns the names of all current menu items.
	 * 
	 * @return An ArrayList of all current menu items' names
	 */
	public ArrayList<String> getInventoryNames() {
		ArrayList<String> l=new ArrayList<String>();
		for(InventoryItem item : inventory) {
			l.add(item.getName());
		}

		return l;
	}

	/**
	 * Returns the InventoryItem matching the given name.
	 * Use getInventoryItems() to get all names.
	 * 
	 * @param name The inventory item name
	 * @return The matching InventoryItem, or null if it does not exist
	 */
	public InventoryItem getItem(String name) {
		for(InventoryItem i : inventory) {
			if(i.getName().equals(name))
				return i;
		}
		return null;
	}

	/**
	 * Add a new item to the inventory.
	 * The new item cannot already be in the inventory.
	 * Name cannot be null.
	 * 
	 * @param name          The name of the new item
	 * @param amount        The amount of the new item
	 * @param desiredAmount The desired amount for the new item
	 * @return True if the item was successfully added
	 */
	public boolean addInventoryItem(String name, int amount, int desiredAmount) {
		if(name==null || name.isBlank())
			return false;
		for(InventoryItem i : inventory) {
			if(i.getName().equals(name))
				return false;
		}

		for(InventoryItem removedItem : removed) {
			if(removedItem.getName().equals(name)) {
				//Added item was already removed
				//Switch to an edit
				removed.remove(removedItem);
				InventoryItem newItem=new InventoryItem(name, desiredAmount, amount);
				edited.put(newItem, removedItem);
				inventory.add(newItem);
				return true;
			}
		}

		InventoryItem item=new InventoryItem(name, desiredAmount, amount);
		added.add(item);
		inventory.add(item);

		return true;
	}

	/**
	 * Edits an existing inventory item. Uses the given parameters.
	 * Pass null for any parameter to leave it unchanged.
	 * All new parameters cannot be null.
	 * 
	 * @param oldItem          The existing item to be edited
	 * @param newName          The new name
	 * @param newAmount        The new amount
	 * @param newDesiredAmount The new desired amount
	 * @return True if the item was successfully edited
	 */
	public boolean editInventoryItem(InventoryItem oldItem, String newName, Integer newAmount, Integer newDesiredAmount) {
		//TODOL void the edit if it is the same as the original (reduces queries made to DB)
		//Must be some edit made, otherwise invalid
		if(newName==null && newAmount==null && newDesiredAmount==null)
			return false;
		//Ensure the inventory object already exists (otherwise this is an add)
		if(!inventory.contains(oldItem))
			return false;

		//If the inventory object has already been edited, merge those changes
		//Overwrite old changes if necessary
		if(edited.containsValue(oldItem)) {
			//Retrieve the old edit from the edited list
			InventoryItem oldEdit=null;
			for(Entry<InventoryItem, InventoryItem> i : edited.entrySet()) {
				if(i.getKey()==oldItem)
					oldEdit=i.getKey();
			}
			//Replace values in existing edit as needed
			if(newName!=null && !newName.isBlank()) {
				oldEdit.setName(newName);
			}else {
				return false;
			}
			if(newAmount!=null) {
				oldEdit.setAmount(newAmount);
			}else {
				return false;
			}
			if(newDesiredAmount!=null) {
				oldEdit.setDesiredAmount(newDesiredAmount);
			}else {
				return false;
			}
			
			inventory.set(inventory.indexOf(oldEdit), oldEdit);
			return true;
		}

		//New edit
		InventoryItem edit=new InventoryItem(oldItem);
		if(newName!=null && !newName.isBlank()) {
			edit.setName(newName);
		}else {
			return false;
		}
		if(newAmount!=null) {
			edit.setAmount(newAmount);
		}else {
			return false;
		}
		if(newDesiredAmount!=null) {
			edit.setDesiredAmount(newDesiredAmount);
		}else {
			return false;
		}
		edited.put(edit, oldItem);
		inventory.set(inventory.indexOf(oldItem), edit);

		return true;
	}

	/**
	 * Remove the given InventoryItem from the inventory.
	 * 
	 * @param item Item currently in the inventory
	 * @return True if the item was removed successfully
	 */
	public boolean removeInventoryItem(InventoryItem item) {
		if(item==null)
			return false;
		//Ensure inventory item exists
		if(!inventory.contains(item))
			return false;

		//Check if item is on added list. If it is, remove it
		if(added.contains(item)) {
			added.remove(item);
		}else {
			removed.add(item);
		}
		
		inventory.remove(item);
		return true;
	}
	
	public ObservableList<InventoryItem> getInventoryList(){
		return inventory;
	}

	@Override
	public String toString() {
		String s="";
		s+="Inventory: "+inventory;
		s+="\nAdded: "+added;
		s+="\nEdited: "+edited;
		s+="\nRemoved: "+removed;
		return s;
	}
}
