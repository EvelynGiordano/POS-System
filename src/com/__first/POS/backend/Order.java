package com.__first.POS.backend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Order {

	private String placedBy;
	private long orderID;
	private String orderTime;
	private Calendar datetime;
	ArrayList<OrderItem> items;

	protected Order(long orderID, String placedBy, Calendar c) throws IllegalArgumentException {
		if(c==null)
			throw new IllegalArgumentException("Calendar c cannot be null");
		this.orderID=orderID;
		this.placedBy=placedBy;
		this.datetime=c;
		SimpleDateFormat sdf=new SimpleDateFormat("hh:mm aa");
		orderTime=sdf.format(datetime.getTime());
		items=new ArrayList<OrderItem>();
	}

	public String getPlacedBy() {
		return placedBy;
	}

	public long getOrderID() {
		return orderID;
	}

	public String getOrderTime() {
		return orderTime;
	}

	public ArrayList<OrderItem> getItems() {
		return items;
	}

	public Calendar getDateTime() {
		return datetime;
	}

	protected void addItem(OrderItem item) {
		if(item==null)
			return;
		//Check if identical item already exists
		if(items.contains(item)) {
			//Identical item found, increment count instead of adding
			int i=items.indexOf(item);
			items.get(i).incrementNum();
			return;
		}

		items.add(item);
	}

	public void setPlacedBy(String placedBy) {
		this.placedBy=placedBy;
	}

	public void setOrderID(long orderID) {
		this.orderID=orderID;
	}

	public void setOrderTime(String orderTime) {
		this.orderTime=orderTime;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Order) {
			Order order=(Order) obj;
			return this.orderID==order.orderID;
		}
		return false;
	}

}
