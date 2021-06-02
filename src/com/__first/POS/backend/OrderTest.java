package com.__first.POS.backend;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class OrderTest {

	private Order o1;
	private Order o2;
	private Order o3;
	private Calendar c1;
	private Calendar c2;
	private Calendar c3;
	private static final SimpleDateFormat sdf=new SimpleDateFormat("hh:mm aa");

	@Before
	public void setUp() throws Exception {
		c1=Calendar.getInstance();
		c2=Calendar.getInstance();
		c2.set(Calendar.DAY_OF_YEAR, 125);
		c3=Calendar.getInstance();
		c3.set(Calendar.HOUR, -500);
		o1=new Order(1, "John", c1);
		o2=new Order(2, "Jane", c2);
		o3=new Order(20000000000l, "Bob", c3);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testConstructor() {
		@SuppressWarnings("unused")
		Order order=new Order(0, null, null);
	}

	@Test
	public void testPlacedBy() {
		assertEquals("John", o1.getPlacedBy());
		assertEquals("Jane", o2.getPlacedBy());
		assertEquals("Bob", o3.getPlacedBy());
		assertEquals(null, (new Order(0, null, c1)).getPlacedBy());

		o1.setPlacedBy("Name 01");
		o2.setPlacedBy(null);
		o3.setPlacedBy("");
		assertEquals("Name 01", o1.getPlacedBy());
		assertNull(o2.getPlacedBy());
		assertEquals("", o3.getPlacedBy());
	}

	@Test
	public void testOrderID() {
		assertEquals(1, o1.getOrderID());
		assertEquals(2, o2.getOrderID());
		assertEquals(20000000000l, o3.getOrderID());

		o1.setOrderID(9223372036854775807l);
		o2.setOrderID(-500000000l);
		o3.setOrderID(57);
		assertEquals(9223372036854775807l, o1.getOrderID());
		assertEquals(-500000000l, o2.getOrderID());
		assertEquals(57, o3.getOrderID());
	}

	@Test
	public void testOrderTime() {
		assertEquals(sdf.format(c1.getTimeInMillis()), o1.getOrderTime());
		assertEquals(sdf.format(c2.getTimeInMillis()), o2.getOrderTime());
		assertEquals(sdf.format(c3.getTimeInMillis()), o3.getOrderTime());

		o1.setOrderTime("3:45 pm");
		o2.setOrderTime("Not a time string");
		o3.setOrderTime(null);
		assertEquals("3:45 pm", o1.getOrderTime());
		assertEquals("Not a time string", o2.getOrderTime());
		assertEquals(null, o3.getOrderTime());
	}

	@Test
	public void testItems() {
		o1.addItem(new OrderItem("Item 1", 0));
		o1.addItem(new OrderItem("Item 1", 1));
		o1.addItem(new OrderItem("Item 1", 2));

		o2.addItem(new OrderItem("Item 3", 0));
		o2.addItem(new OrderItem("Item 2", 1));
		o2.addItem(new OrderItem("Item 1", 2));

		o3.addItem(new OrderItem("Item 1", 0));
		o3.addItem(new OrderItem("Item 1", 1));
		o3.addItem(new OrderItem("Item 2", 2));
		o3.addItem(new OrderItem("Item 2", 3));

		ArrayList<OrderItem> list=new ArrayList<>();
		list.add(new OrderItem("Item 1", 0));
		assertArrayEquals(list.toArray(), o1.getItems().toArray());

		list.clear();
		list.add(new OrderItem("Item 3", 0));
		list.add(new OrderItem("Item 2", 0));
		list.add(new OrderItem("Item 1", 0));

		list.clear();
		list.add(new OrderItem("Item 1", 0));
		list.add(new OrderItem("Item 2", 0));
		assertArrayEquals(list.toArray(), o3.getItems().toArray());

		o1=new Order(1, "John", c1);
		assertArrayEquals(new ArrayList<>().toArray(), o1.getItems().toArray());
	}

	@Test
	public void testGetDateTime() {
		assertSame(c1, o1.getDateTime());
		assertSame(c2, o2.getDateTime());
		assertSame(c3, o3.getDateTime());
	}

	@Test
	public void testEquals() {
		assertNotEquals(new Order(15, null, c1), o1);
		assertNotEquals("String", o1);
		assertNotEquals(o2, o1);

		assertEquals(new Order(1, null, c1), o1);
		assertEquals(o1, o1);
		assertEquals(new Order(2, null, c1), o2);
		assertEquals(o2, o2);
		assertEquals(new Order(20000000000l, null, c1), o3);
		assertEquals(o3, o3);
	}

}
