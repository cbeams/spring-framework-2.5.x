package org.springframework.samples.phonebook.domain;

import java.io.Serializable;

public class PhoneBookQuery implements Serializable {

	private String firstName;

	private String lastName;

	public PhoneBookQuery() {
		this.firstName = "";
		this.lastName = "";
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}