/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.web;

import org.springframework.benchmark.cmt.data.Order;

/**
 * 
 * @author Rod Johnson
 */
public class OrdersBean {
	
	private Order[] orders;
	
	private long time;
	
	private String message;
	

	/**
	 * @param orders
	 * @param time
	 */
	public OrdersBean(Order[] orders, String message, long time) {
		super();
		this.orders = orders;
		this.time = time;
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}

	/**
	 * @return Returns the orders.
	 */
	public Order[] getOrders() {
		return this.orders;
	}
	/**
	 * @return Returns the time.
	 */
	public long getTime() {
		return this.time;
	}
}
