package com.__first.POS.backend;

import java.util.ArrayList;
import java.util.Collections;

public class OrderItem {

	private String name;
	private ArrayList<String> customizations;
	private int itemNum;
	private int num;

	protected OrderItem(String name, int itemNum) {
		this.name=name;
		this.itemNum=itemNum;
		customizations=new ArrayList<String>();
		num=1;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof OrderItem) {
			OrderItem item=(OrderItem) obj;
			boolean names=this.name.equals(item.name);
			boolean customs=customizationsEqual(item);
			return names && customs;
		}
		return false;
	}

	private boolean customizationsEqual(OrderItem item) {
		if(item.customizations.size()==this.customizations.size()) {
			ArrayList<String> cur=new ArrayList<String>(customizations);
			ArrayList<String> comp=new ArrayList<String>(item.customizations);
			Collections.sort(cur);
			Collections.sort(comp);
			return cur.equals(comp);
		}
		return false;
	}

	protected void incrementNum() {
		num++;
	}

	protected void addCustomization(String customization) {
		customizations.add(customization);
	}

	public String getName() {
		return name;
	}

	public int getNum() {
		return num;
	}

	public int getItemNum() {
		return itemNum;
	}

	public ArrayList<String> getCustomizations() {
		return new ArrayList<String>(customizations);
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("(");
		if(num>1)
			sb.append(num+" ");
		sb.append(name);
		if(customizations.size()>0) {
			sb.append(": ");
			sb.append(customizations.get(0));
			for(int i=1; i<customizations.size(); i++)
				sb.append(", "+customizations.get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
