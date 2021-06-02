package com.__first.POS.backend;

import java.util.ArrayList;
import java.util.Arrays;

public class InventoryItem {

	private static final Integer[] EMPTY_INITIALIZE= {0, 0, 0, 0, 0, 0, 0};

	private String name;
	private int desiredAmount;
	private int originalCount;
	private ArrayList<Integer> countsUsed; //First int is 7 days ago, last int is today

	protected InventoryItem(String name, int desiredAmount, int originalCount) {
		this.name=name;
		this.desiredAmount=desiredAmount;
		countsUsed=new ArrayList<Integer>(Arrays.asList(EMPTY_INITIALIZE));
		this.originalCount = originalCount;
	}
	
	protected InventoryItem(InventoryItem item) {
		this.name=item.name;
		this.desiredAmount=item.desiredAmount;
		this.originalCount=item.originalCount;
	}

	protected void addDayCountUsed(int count, int loc) {
		countsUsed.add(loc, count);
		if(countsUsed.size()>EMPTY_INITIALIZE.length)
			countsUsed.remove(countsUsed.size()-1);
	}
	
	protected int totalCountUsed() {
		int sum=0;
		for(Integer i:countsUsed) {
			sum+=i;
		}
		return sum;
	}
	
	protected void setName(String name) {
		this.name=name;
	}
	
	protected void setAmount(int amount) {
		this.originalCount=amount;
	}
	
	protected void setDesiredAmount(int desiredAmount) {
		this.desiredAmount=desiredAmount;
	}

	/**
	 * Returns the total count at the end of the given day
	 * 
	 * @param day The requested day. 0 is 7 days ago, 6 is today.
	 * @return The total count at the end of the given day.
	 * @throws IndexOutOfBoundsException Thrown if given day is not in range [0,7).
	 */
	public int getDayCount(int day) throws IndexOutOfBoundsException {
		if(day<0 || day>countsUsed.size())
			throw new IndexOutOfBoundsException(day+"outside acceptable range: "+0+"-"+(countsUsed.size()-1));
		int count=0;
		for(int i=0; i<=day; i++) {
			count+=countsUsed.get(i);
		}
		return originalCount-count;
	}

	public int getDesiredAmount() {
		return desiredAmount;
	}

	public String getName() {
		return name;
	}
	
	public int getAmount() {
		return originalCount;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof InventoryItem) {
			InventoryItem item=(InventoryItem) obj;
			return this.name.equals(item.name);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		String s="("+name;
		s+=", "+originalCount;
		s+=", "+desiredAmount+")";
		return s;
	}

	public int getOriginalCount() {
		return originalCount;
	}
}
