/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

/**
 * @author robh
 *
 */
public class MyBean {

	private int id;

	private String name;

	private String street;

	private String city;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

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

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
}
