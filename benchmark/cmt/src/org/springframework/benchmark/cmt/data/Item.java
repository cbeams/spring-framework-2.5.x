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
public class Item implements Serializable {
	
	private static final long serialVersionUID = -4732637468796964793L;

	private String name;
	
	private int stock;
	
	public Item(String name, int stock) {
		this.name = name;
		this.stock = stock;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the stock.
	 */
	public int getStock() {
		return this.stock;
	}

	/**
	 * @param stock The stock to set.
	 */
	public void setStock(int stock) {
		this.stock = stock;
	}

	public String toString() {
		return "name='" + name + "'; stock=" + stock;
	}
}
