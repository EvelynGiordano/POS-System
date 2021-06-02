package com.__first.POS.backend;

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class OrdersTest {

	private Orders o1;
	private Orders o2;
	private Calendar c1;
	private Calendar c2;
	private ArrayList<Order> olist;

	@Before
	public void setUp() throws Exception {
		olist=new ArrayList<>();
		c1=Calendar.getInstance();
		c2=Calendar.getInstance();
		c2.add(Calendar.HOUR, -50);
		o1=new Orders();
		o2=new Orders();
		o1.addOrder(null);
		o1.addOrder(null);
		olist.add(new Order(52, "John Doe", c1));
		olist.add(new Order(53, "Jane Doe", c2));
		o1.addOrder(olist.get(0));
		o1.addOrder(olist.get(1));

		olist.add(new Order(5, "Johnathan", c2));
		olist.add(new Order(6, "Bob", c1));
		olist.add(new Order(700000, "Chris", Calendar.getInstance()));
		o2.addOrder(olist.get(2));
		o2.addOrder(olist.get(3));
		o2.addOrder(null);
		o2.addOrder(olist.get(4));
		o2.addOrder(null);
	}

	@Test
	public void testOrders() {
		assertSame(olist.get(0), o1.getOrders().get(0));
		assertSame(olist.get(1), o1.getOrders().get(1));

		assertSame(olist.get(2), o2.getOrders().get(0));
		assertSame(olist.get(3), o2.getOrders().get(1));
		assertSame(olist.get(4), o2.getOrders().get(2));
	}

}
