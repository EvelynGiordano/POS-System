package com.__first.POS.backend;

import java.util.HashMap;

public class PlacedOrderItem {

	private String name;
	private HashMap<String, Integer> customizations; //Customization -> Option

	public PlacedOrderItem(String name) {
		this.name=name;
		customizations=new HashMap<>();
	}

	public void addCustomization(String customizationName, int optionName) {
		customizations.put(customizationName, optionName);
	}

	protected String getName() {
		return name;
	}

	protected HashMap<String, Integer> getCustomizations() {
		return customizations;
	}

}
