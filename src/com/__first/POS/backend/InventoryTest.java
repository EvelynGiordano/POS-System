package com.__first.POS.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class InventoryTest {
	private Inventory iAdd;
	private Inventory iDelete;
	private Inventory iEdit;
	private InventoryItem a;
	private InventoryItem a1;
	private InventoryItem b;
	private InventoryItem c;
	private ArrayList<InventoryItem> invAdd;
	private ArrayList<InventoryItem> invDelete;
	HashMap<InventoryItem, InventoryItem> edits;

	@Before
	public void setUp() throws Exception {
		a = new InventoryItem("Mustard", 30, 10);
		a1 = new InventoryItem("Mustard", 35, 10);
		b = new InventoryItem("Lettuce", 50, 40);
		c = new InventoryItem("Ketchup", 35, 20);
		
		iAdd = new Inventory();
		invAdd = new ArrayList<InventoryItem>();
		invAdd.add(a);			//elements that will be added
		invAdd.add(b);
		invAdd.add(c);
		
		
		
		iDelete = new Inventory();
		invDelete = new ArrayList<InventoryItem>();
		invDelete.add(b); 		//the element to be deleted
		
		iEdit = new Inventory();
		edits = new HashMap<InventoryItem, InventoryItem>();
		edits.put(a1, a);
		
		iAdd.addInventoryItem("Mustard", 30, 10);
		iAdd.addInventoryItem("Lettuce", 40, 20);
		iAdd.addInventoryItem("Ketchup", 20, 15);
		
		iDelete.addInventoryItem("Mustard", 30, 10);
		iDelete.addInventoryItem("Lettuce", 40, 20);
		iDelete.removeInventoryItem(b);
		
		iEdit.addInventoryItem("Mustard", 30, 10);
		iEdit.addInventoryItem("Lettuce", 50, 40);
		iEdit.editInventoryItem(a, "Mustard", 10, 35);
		
	}

@Test
	public void testOrders() {

		assertEquals(invAdd, iAdd.getAdditions()); //test get additions
		assertEquals(new ArrayList<>(), iDelete.getRemoved()); //test get additions
		assertEquals(edits.toString(), iEdit.getEdits().toString()); //test get edits
		assertEquals(iAdd.getItem(a.getName()), a);
		assertEquals(iAdd.getItem(b.getName()), b);
		assertEquals(iAdd.getItem(c.getName()), c);
		assertSame(iAdd.addInventoryItem("", 20, 30), false);
		assertSame(iAdd.addInventoryItem(null, 20, 30), false);
		assertSame(iAdd.addInventoryItem("Mustard", 20, 32), false);
		assertSame(iAdd.addInventoryItem("Hot Sauce", 30, 50), true);
		assertSame(iEdit.editInventoryItem(c, "Ketchup", 15, 20), false);
		assertSame(iEdit.editInventoryItem(a, "", null, null), false);
		assertSame(iEdit.editInventoryItem(b, "Lettuce", 40, 50), true);
		assertSame(iDelete.removeInventoryItem(null), false);
		assertSame(iDelete.removeInventoryItem(c), false);
		assertSame(iDelete.removeInventoryItem(a), true);

}

}

