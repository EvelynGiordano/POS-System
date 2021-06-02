package com.__first.POS.backend;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class PlacedOrderItemTest {

	private PlacedOrderItem i1;
	private PlacedOrderItem i2;
	private PlacedOrderItem i3;

	@Before
	public void setUp() throws Exception {
		i1=new PlacedOrderItem("Item 1");
		i2=new PlacedOrderItem("Item 2");
		i3=new PlacedOrderItem("Item 3");
	}

	@Test
	public void testGetName() {
		assertEquals("Item 1", i1.getName());
		assertEquals("Item 2", i2.getName());
		assertEquals("Item 3", i3.getName());
		assertEquals(null, (new PlacedOrderItem(null).getName()));
	}

	@Test
	public void testCustomizations() {
		i1.addCustomization("Cust 1", 2);
		i1.addCustomization("Cust 2", 0);
		i1.addCustomization("Cust 3", 5);
		i2.addCustomization("Customization 1", 0);
		i2.addCustomization("Customization 3", 1);
		i3.addCustomization("C1", 1);
		i3.addCustomization("C2", 8);
		i3.addCustomization("C3", 15);
		i3.addCustomization("C7", 1);
		i3.addCustomization("C8", 3);
		i3.addCustomization("C9", 0);
		i3.addCustomization("C10", 2);

		HashMap<String, Integer> map=i1.getCustomizations();
		assertEquals(2, (int) map.get("Cust 1"));
		assertEquals(0, (int) map.get("Cust 2"));
		assertEquals(5, (int) map.get("Cust 3"));

		map=i2.getCustomizations();
		assertEquals(0, (int) map.get("Customization 1"));
		assertEquals(1, (int) map.get("Customization 3"));

		map=i3.getCustomizations();
		assertEquals(1, (int) map.get("C1"));
		assertEquals(8, (int) map.get("C2"));
		assertEquals(15, (int) map.get("C3"));
		assertEquals(1, (int) map.get("C7"));
		assertEquals(3, (int) map.get("C8"));
		assertEquals(0, (int) map.get("C9"));
		assertEquals(2, (int) map.get("C10"));

		i1=new PlacedOrderItem("Item 01");
		assertEquals(new HashMap<>(), i1.getCustomizations());
	}

}
