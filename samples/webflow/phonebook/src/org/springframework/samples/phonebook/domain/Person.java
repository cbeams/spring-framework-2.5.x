package org.springframework.samples.phonebook.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Person implements Serializable {

	private String firstName;

	private String lastName;

	private UserId userId;

	private String phone;

	private List colleagues = new ArrayList();

	public Person() {
		this("", "", "", "");
	}

	public Person(String firstName, String lastName, String userId, String phone) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.userId = new UserId(userId);
		this.phone = phone;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public UserId getUserId() {
		return this.userId;
	}

	public String getPhone() {
		return this.phone;
	}

	public List getColleagues() {
		return this.colleagues;
	}

	public int nrColleagues() {
		return this.colleagues.size();
	}

	public Person getColleague(int i) {
		return (Person)this.colleagues.get(i);
	}

	public void addColleague(Person colleague) {
		this.colleagues.add(colleague);
	}
}