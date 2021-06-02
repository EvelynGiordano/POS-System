package com.__first.POS.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

public class InventoryItemTest {

	private InventoryItem i;
	private InventoryItem iEdited;
	
	@Before
	public void setUp() throws Exception {
		
		i = new InventoryItem("Pickles", 30, 15);
		i.setName("Mustard");
		i.setAmount(20);
		i.setDesiredAmount(40);
	}

	@Test
	public void testOrders() {
		assertEquals(i, new InventoryItem("Mustard", 40, 20));		//set methods work
		assertEquals(i.getName(), "Mustard");
		assertEquals(i.getAmount(), 20);
		assertEquals(i.getDesiredAmount(), 40);
		assertEquals(i.getDayCount(6), 20);
		assertTrue(i.equals(new InventoryItem("Mustard", 40, 20)));

	}

}