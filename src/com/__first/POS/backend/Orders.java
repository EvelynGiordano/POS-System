package com.__first.POS.backend;

import java.util.ArrayList;

public class Orders {

	private ArrayList<Order> orders;

	protected Orders() {
		orders=new ArrayList<Order>();
	}

	protected void addOrder(Order order) {
		if(order!=null)
			orders.add(order);
	}

	public ArrayList<Order> getOrders() {
		return orders;
	}
}
