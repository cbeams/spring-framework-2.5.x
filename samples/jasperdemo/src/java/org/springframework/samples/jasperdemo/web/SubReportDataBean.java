
package org.springframework.samples.jasperdemo.web;

/**
 * Simple JavaBean used for sub-report rendering.
 * 
 * @author Rob Harrop
 */
public class SubReportDataBean {

	private int id;

	private String name;

	private float quantity;

	private float price;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getQuantity() {
		return quantity;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
