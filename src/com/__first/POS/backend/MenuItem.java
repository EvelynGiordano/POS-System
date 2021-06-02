package com.__first.POS.backend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuItem {
	
	private String name;
	private ArrayList<String> customizations;
	private HashMap<String, Integer> inventoryUsage;
	private BigDecimal cost;
	
	public MenuItem(String name) {
		this.name = name;
		customizations = new ArrayList<>(); // Initialize with an empty list
		inventoryUsage=new HashMap<>();
	}
	
	public MenuItem(String name, ArrayList<String> customizations) {
		this.name = name;
		this.customizations = customizations;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<String> getCustomizations() {
		return customizations;
	}
	
	public void setCustomizations(ArrayList<String> customizations) {
		this.customizations = customizations;
	}
	
	public void addInventoryUsage(String inventoryItem, int amount) {
		inventoryUsage.put(inventoryItem, amount);
	}
	
	public HashMap<String, Integer> getInventoryUsage() {
		return new HashMap<String, Integer>(inventoryUsage);
	}
	
	public void setInventoryUsage(HashMap<String, Integer> inventoryUsage) {
		this.inventoryUsage=new HashMap<>(inventoryUsage);
	}

	public void setCost(BigDecimal cost) {
		this.cost=cost;
	}

	public BigDecimal getCost() {
		return cost;
	}

	@Override
	public String toString() {
		return "Name: " + name + customizations;
	}

}
