/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.cmt.server;

/**
 * 
 * @author Rod Johnson
 */
public class InsufficientStockException extends Exception {
	
	private static final long serialVersionUID = -4316068024274819159L;

	private int stock;
	
	private int requested;
	
	public InsufficientStockException(long id, int stock, int requested) {
		super("There are only " + stock + " of item " + id + " in stock; user tried to order " + requested);
		this.stock = stock;
		this.requested = requested;
	}

	/**
	 * @return Returns the requested.
	 */
	public int getRequested() {
		return this.requested;
	}

	/**
	 * @return Returns the stock.
	 */
	public int getStock() {
		return this.stock;
	}

}
