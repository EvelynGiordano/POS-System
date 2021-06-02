package com.__first.POS.backend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

public class Customization {

	private String name;
	private HashMap<String, Integer> options;
	private HashMap<String, BigDecimal> optionCosts;

	protected Customization(String name) {
		this.name=name;
		options=new HashMap<String, Integer>();
		optionCosts=new HashMap<>();
	}

	public void addOption(String name, int id, BigDecimal cost) {
		options.put(name, id);
		optionCosts.put(name, cost);
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the options in order.
	 * 
	 * @return The option Strings
	 */
	public ArrayList<String> getOptions() {
		String[] a=new String[options.size()];
		for(Entry<String, Integer> e : options.entrySet()) {
			a[e.getValue()]=e.getKey();
		}
		return new ArrayList<>(Arrays.asList(a));
	}

	/**
	 * Given an option name, returns the cost of that option.
	 * Returns 0 if the option name is invalid, or if the customization has no cost.
	 * 
	 * @param optionName The option name
	 * @return The cost
	 */
	public BigDecimal getOptionCost(String optionName) {
		if(optionName==null || optionName.equals(""))
			return new BigDecimal(0);
		BigDecimal cost=optionCosts.get(optionName);
		if(cost==null)
			return new BigDecimal(0);
		else
			return cost;
	}

	@Override
	public String toString() {
		String s=name+": ";
		s+=getOptions();
		return s;
	}

	//===============================\\
	//   For Customization Editing   \\
	//===============================\\

	private ArrayList<HashMap<String, Integer>> inventoryUsage;
	private ArrayList<BigDecimal> prices;

	public Customization(String name, ArrayList<HashMap<String, Integer>> inventoryUsage) {
		this.name=name;
		this.inventoryUsage=inventoryUsage;
		options=new HashMap<String, Integer>();
		optionCosts=new HashMap<>();
	}

	public void setInventoryUsage(ArrayList<HashMap<String, Integer>> inventoryUsage) {
		this.inventoryUsage=inventoryUsage;
	}
	
	public void setPriceArray(ArrayList<BigDecimal> prices) {
		this.prices=prices;
	}
	
	public ArrayList<BigDecimal> getPriceArray() {
		if(prices==null)
			return new ArrayList<>();
		return new ArrayList<>(prices);
	}

	public ArrayList<HashMap<String, Integer>> getInventoryUsage() {
		ArrayList<HashMap<String, Integer>> list=new ArrayList<>();
		if(inventoryUsage==null)
			return list;
		for(HashMap<String, Integer> map : inventoryUsage) {
			HashMap<String, Integer> newmap=new HashMap<>(map);
			list.add(newmap);
		}
		return list;
	}

}