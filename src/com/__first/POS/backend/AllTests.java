package com.__first.POS.backend;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CustomizationTest.class, InventoryItemTest.class, InventoryTest.class, MenuItemTest.class, OrderItemTest.class, OrdersTest.class,
		OrderTest.class, PlacedOrderItemTest.class})
public class AllTests {

}
