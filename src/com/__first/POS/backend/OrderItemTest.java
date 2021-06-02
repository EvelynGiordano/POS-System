package com.__first.POS.backend;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class OrderItemTest {

	private OrderItem i1;
	private OrderItem i2;
	private OrderItem i3;

	@Before
	public void setUp() throws Exception {
		i1=new OrderItem("Item 1", 0);
		i2=new OrderItem("Item 2", 15);
		i3=new OrderItem("Item 3", -9);

		i1.addCustomization("Cust 1");
		i1.addCustomization("Cust 2");
		i1.addCustomization("Cust 3");
		i2.addCustomization("Cust 5");
		i2.addCustomization("Cust 7");
		i3.addCustomization("Customization09");
	}

	@Test
	public void testEquals() {
		assertNotEquals(i2, i1);
		assertNotEquals(null, i1);
		assertNotEquals("String", i1);

		assertEquals(i1, i1);
		assertEquals(i2, i2);
		assertEquals(i3, i3);

		assertNotEquals(new OrderItem("Item 1", 0), i1);
		i2=new OrderItem("Item 1", 0);
		i2.addCustomization("Cust 1");
		i2.addCustomization("Cust 2");
		i2.addCustomization("Cust 3");
		assertEquals(i2, i1);
	}

	@Test
	public void testNum() {
		assertEquals(1, i1.getNum());
		assertEquals(1, i2.getNum());
		assertEquals(1, i3.getNum());

		i1.incrementNum();
		i1.incrementNum();
		i1.incrementNum();
		i1.incrementNum();
		i2.incrementNum();
		i2.incrementNum();
		i3.incrementNum();
		assertEquals(5, i1.getNum());
		assertEquals(3, i2.getNum());
		assertEquals(2, i3.getNum());
	}

	@Test
	public void testCustomizations() {
		ArrayList<String> list=new ArrayList<>();
		list.add("Cust 1");
		list.add("Cust 2");
		list.add("Cust 3");
		assertNotSame(list, i1.getCustomizations());
		assertEquals(list, i1.getCustomizations());

		assertArrayEquals(new String[] {"Cust 5", "Cust 7"}, i2.getCustomizations().toArray());
		assertArrayEquals(new String[] {"Customization09"}, i3.getCustomizations().toArray());
	}

	@Test
	public void testGetName() {
		assertEquals("Item 1", i1.getName());
		assertEquals("Item 2", i2.getName());
		assertEquals("Item 3", i3.getName());
		assertEquals(null, (new OrderItem(null, 0).getName()));
		assertEquals("", (new OrderItem("", 0).getName()));
	}

	@Test
	public void testGetItemNum() {
		assertEquals(0, i1.getItemNum());
		assertEquals(15, i2.getItemNum());
		assertEquals(-9, i3.getItemNum());
	}

	@Test
	public void testToString() {
		assertEquals("(Item 1: Cust 1, Cust 2, Cust 3)", i1.toString());
		assertEquals("(Item 2: Cust 5, Cust 7)", i2.toString());
		assertEquals("(Item 3: Customization09)", i3.toString());
		assertEquals("(Item 1)", (new OrderItem("Item 1", 0).toString()));
	}

}
