/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.data;

import java.io.Serializable;

/**
 * 
 * @author Rod Johnson
 */
public class Order implements Serializable {
	
	private static final long serialVersionUID = 5578599792267875866L;

	private int quantity;
	
	private long itemId;
	
	private long userid;
	
	//private Date 
	
	public Order(long userid, long id, int qty) {
		this.userid = userid;
		itemId = id;
		this.quantity = qty;
	}

	/**
	 * @return Returns the itemId.
	 */
	public long getItemId() {
		return this.itemId;
	}
	
	public long getUserId() {
		return this.userid;
	}

	/**
	 * @param itemId The itemId to set.
	 */
	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return Returns the quantity.
	 */
	public int getQuantity() {
		return this.quantity;
	}

	/**
	 * @param quantity The quantity to set.
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
