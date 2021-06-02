package com.__first.POS.backend;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class CustomizationTest {

	private Customization c1;
	private Customization c2;
	private Customization c3;

	@Before
	public void setUp() {
		c1=new Customization("Cust 1");
		c2=new Customization("Cust 2");
		c1.addOption("Option 1", 0, new BigDecimal(15.50));
		c1.addOption("Option 2", 1, new BigDecimal(0));
		c1.addOption("Option 3", 2, new BigDecimal(.1));
		c2.addOption("Opt 3", 2, new BigDecimal(.3));
		c2.addOption("Opt 1", 0, new BigDecimal(0));
		c2.addOption("Opt 2", 1, new BigDecimal(1.00));

		HashMap<String, Integer> map1=new HashMap<>();
		HashMap<String, Integer> map2=new HashMap<>();
		map1.put("Item 1", 1);
		map1.put("Item 2", 15);
		map2.put("Item 2", -1);
		map2.put("Item 3", 2);
		ArrayList<HashMap<String, Integer>> list=new ArrayList<>();
		list.add(map1);
		list.add(map2);
		c3=new Customization("Customization 3", list);
		c3.addOption("Option 1", 0, new BigDecimal(0.40));
		c3.addOption("Option 2", 1, new BigDecimal(-0.20));
	}

	@Test
	public void testGetName() {
		assertTrue(c1.getName().equals("Cust 1"));
		assertTrue(c2.getName().equals("Cust 2"));
		assertTrue(c3.getName().equals("Customization 3"));
		assertNull((new Customization(null)).getName());
	}

	@Test
	public void testOptions() {
		assertArrayEquals(new String[] {"Option 1", "Option 2", "Option 3"}, c1.getOptions().toArray());
		assertArrayEquals(new String[] {"Opt 1", "Opt 2", "Opt 3"}, c2.getOptions().toArray());
		assertArrayEquals(new String[] {"Option 1", "Option 2"}, c3.getOptions().toArray());

		assertEquals(new BigDecimal(15.50), c1.getOptionCost("Option 1"));
		assertEquals(new BigDecimal(0.00), c1.getOptionCost("Option 2"));
		assertEquals(new BigDecimal(0.10), c1.getOptionCost("Option 3"));

		assertEquals(new BigDecimal(0.30), c2.getOptionCost("Opt 3"));
		assertEquals(new BigDecimal(0.00), c2.getOptionCost("Opt 1"));
		assertEquals(new BigDecimal(1.00), c2.getOptionCost("Opt 2"));

		assertEquals(new BigDecimal(0.40), c3.getOptionCost("Option 1"));
		assertEquals(new BigDecimal(-0.20), c3.getOptionCost("Option 2"));
	}

	@Test
	public void testToString() {
		assertEquals("Cust 1: [Option 1, Option 2, Option 3]", c1.toString());
		assertEquals("Cust 2: [Opt 1, Opt 2, Opt 3]", c2.toString());
		assertEquals("Customization 3: [Option 1, Option 2]", c3.toString());
		c1=new Customization("Empty Customization");
		assertEquals("Empty Customization: []", c1.toString());
	}

	@Test
	public void testInventoryUsage() {
		ArrayList<HashMap<String, Integer>> tmpList=new ArrayList<>();
		tmpList.add(new HashMap<>());
		tmpList.add(new HashMap<>());
		Customization c=new Customization("Cust With No List", null);
		assertEquals(0, c.getInventoryUsage().size());
		c.setInventoryUsage(tmpList);
		assertNotSame(tmpList, c.getInventoryUsage());
		assertTrue(c.getInventoryUsage().size()==2);

		ArrayList<HashMap<String, Integer>> list=c3.getInventoryUsage();
		assertEquals(1, (int) list.get(0).get("Item 1"));
		assertEquals(15, (int) list.get(0).get("Item 2"));
		assertEquals(-1, (int) list.get(1).get("Item 2"));
		assertEquals(2, (int) list.get(1).get("Item 3"));
	}

	@Test
	public void testPriceArray() {
		c1.setPriceArray(null);
		c2.setPriceArray(new ArrayList<>());
		ArrayList<BigDecimal> list=new ArrayList<>();
		list.add(new BigDecimal(10.00));
		list.add(new BigDecimal(15.20));
		list.add(new BigDecimal(18.92));
		c3.setPriceArray(list);

		assertEquals(new ArrayList<>(), c1.getPriceArray());
		assertEquals(new ArrayList<>(), c2.getPriceArray());
		assertNotSame(list, c3.getPriceArray());
		assertEquals(new BigDecimal(10), c3.getPriceArray().get(0));
		assertEquals(new BigDecimal(15.2), c3.getPriceArray().get(1));
		assertEquals(new BigDecimal(18.92), c3.getPriceArray().get(2));
	}

}
