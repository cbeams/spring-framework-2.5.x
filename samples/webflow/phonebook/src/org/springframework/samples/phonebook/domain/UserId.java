package org.springframework.samples.phonebook.domain;

import java.io.Serializable;

public class UserId implements Serializable {

	private String id;

	public UserId() {
		this.id = "";
	}

	public UserId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else
			return this.toString().equals(obj.toString());
	}

	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

	public String toString() {
		return id;
	}
}