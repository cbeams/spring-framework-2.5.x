/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.benchmark.cmt.data;

import java.io.Serializable;

/**
 * @author Rod Johnson
 */
public class User implements Serializable {

	private static final long serialVersionUID = 8978013226634365119L;

	private String forename;

	private String surname;
	
	public User() {
	}
	


	/**
	 * @return Returns the forename.
	 */
	public String getForename() {
		return this.forename;
	}

	/**
	 * @param forename The forename to set.
	 */
	public void setForename(String forename) {
		this.forename = forename;
	}

	/**
	 * @return Returns the surname.
	 */
	public String getSurname() {
		return this.surname;
	}

	/**
	 * @param surname The surname to set.
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "User: forename='" + forename + "' surname='" + surname + "'";
	}

	/**
	 * @param forename
	 * @param surname
	 */
	public User(String forename, String surname) {
		super();
		this.forename = forename;
		this.surname = surname;
	}
	
	public boolean equals(Object b) {
		if (!(b instanceof User))
			return false;
		User other = (User) b;
		// TODO doesn't handle nulls
		return other.forename.equals(forename) && other.surname.equals(surname);
	}
}
