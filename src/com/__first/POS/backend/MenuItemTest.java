package com.__first.POS.backend;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class MenuItemTest {
	
	MenuItem mi1;
	MenuItem mi2;
	MenuItem mi3;
	
	String c1;
	String c2;
	String c3;
	
	HashMap<String, Integer> inventoryUsage;
	
	@Before
	public void setUp() {
		mi1 = new MenuItem("Menu Item 1");
		mi2 = new MenuItem("Menu Item 2");
		
		c1 = "cust 1";
		c2 = "cust 2";
		c3 = "cust 3";
		
		mi3 = new MenuItem("Menu Item 3", new ArrayList<>(Arrays.asList(c1, c2, c3)));
	}
	
	@Test
	public void getNameTest() {
		assertTrue(mi1.getName().equals("Menu Item 1"));
		assertTrue(mi2.getName().equals("Menu Item 2"));
		assertTrue(mi3.getName().equals("Menu Item 3"));
		assertNull(new MenuItem(null).getName());
	}
	
	@Test
	public void setNameTest() {
		mi1.setName("Menu Item 0");
		mi2.setName("Menu Item 1");
		mi3.setName("Menu Item 2");
		
		assertTrue(mi1.getName().equals("Menu Item 0"));
		assertTrue(mi2.getName().equals("Menu Item 1"));
		assertTrue(mi3.getName().equals("Menu Item 2"));
	}
	
	@Test
	public void testGetCustomizations() {
		assertTrue(mi3.getCustomizations().equals(Arrays.asList("cust 1", "cust 2", "cust 3")));
	}
	
	@Test
	public void testSetCustomizations() {
		mi3.setCustomizations(new ArrayList<String>(Arrays.asList("cust 0", "cust 1", "cust 2")));
		
		assertTrue(mi3.getCustomizations().equals(Arrays.asList("cust 0", "cust 1", "cust 2")));
	}
	
	@Test
	public void testAddInventoryUsage() {
		InventoryItem invItem1 = new InventoryItem("inv item 1", 10, 50);
		InventoryItem invItem2 = new InventoryItem("inv item 2", 20, 50);
		
		mi1.addInventoryUsage(invItem1.getName(), invItem1.getDesiredAmount());
		mi2.addInventoryUsage(invItem2.getName(), invItem2.getDesiredAmount());
		
		assertTrue(mi1.getInventoryUsage().containsKey("inv item 1"));
		assertTrue(mi1.getInventoryUsage().containsValue(10));
		
		assertTrue(mi2.getInventoryUsage().containsKey("inv item 2"));
		assertTrue(mi2.getInventoryUsage().containsValue(20));
		
	}
	
	/*
	 * This tests adding and getting inventory usage together
	 */
	@Test
	public void testGetInventoryUsage() {
		InventoryItem invItem1 = new InventoryItem("inv item 1", 10, 50);
		InventoryItem invItem2 = new InventoryItem("inv item 2", 20, 50);
		
		mi1.addInventoryUsage(invItem1.getName(), invItem1.getDesiredAmount());
		mi2.addInventoryUsage(invItem2.getName(), invItem2.getDesiredAmount());
		
		assertTrue(mi1.getInventoryUsage().containsKey("inv item 1"));
		assertTrue(mi1.getInventoryUsage().containsValue(10));
		
		assertTrue(mi2.getInventoryUsage().containsKey("inv item 2"));
		assertTrue(mi2.getInventoryUsage().containsValue(20));
		
	}
	
	@Test
	public void testSetInventoryUsage() {
		InventoryItem invItem1 = new InventoryItem("inv item 1", 10, 50);
		InventoryItem invItem2 = new InventoryItem("inv item 2", 20, 50);
		
		invItem1 = new InventoryItem("inv item 0", 30, 50);
		invItem2 = new InventoryItem("inv item 1", 5, 50);
		
		HashMap<String, Integer> inventoryUsage = new HashMap<>();
		inventoryUsage.put(invItem1.getName(), invItem1.getDesiredAmount());
		inventoryUsage.put(invItem2.getName(), invItem2.getDesiredAmount());
		
		mi1.setInventoryUsage(inventoryUsage);
		mi2.setInventoryUsage(inventoryUsage);
		
		assertTrue(mi1.getInventoryUsage().containsKey("inv item 0"));
		assertTrue(mi1.getInventoryUsage().containsValue(30));
		
		assertTrue(mi2.getInventoryUsage().containsKey("inv item 1"));
		assertTrue(mi2.getInventoryUsage().containsValue(5));

	}
	
	// get cost and set cost are tested together in one method
	@Test
	public void setCostTest() {
		BigDecimal zero = BigDecimal.ZERO;
		BigDecimal one = BigDecimal.ONE;
		
		mi1.setCost(zero);
		mi2.setCost(one);
		
		assertTrue(mi1.getCost().equals(BigDecimal.ZERO));
		assertTrue(mi2.getCost().equals(BigDecimal.ONE));
	}
	
	

}
