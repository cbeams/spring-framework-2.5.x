/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.autobuilds.ejbtest.domain;

import java.util.logging.Logger;

/**
 * Simple user domain object (variant 1)
 * 
 * @author colin sampaleanu
 */
public class User1 {

	// --- statics
	static Logger logger = Logger.getLogger(User1.class.getName());

	// --- attributes
	private Long _id;
	private String _username;
	private String _password;

	// --- methods

	// for persistence layer
	protected User1() {
	}

	public User1(Long id, String username, String password) {

		_id = id;
		_username = username;
		_password = password;
	}

	/**
	 * The (opaque) object ID
	 * @return Long
	 * 
	 * @hibernate.id
	 *   column = "USER_ID"
	 *   unsaved-value = "null"
	 *   generator-class = "sequence"
	 * @hibernate.generator-param
	 *   name = "sequence"
	 *   value = "USER_ID_SEQ"
	 */
	public Long getId() {
		return _id;
	}

	public void setId(Long val) {
		_id = val;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	/** 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {

		if (obj instanceof User1) {

			User1 rhs = (User1) obj;
			if (_id == null || rhs._id == null)
				return false;

			if (_id.equals(rhs._id))
				return true;
		}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// bummer. we need to work in a setup where the object is initially created with a null id,
		// and a persistence manager then assigns the id. So we just return the same hash. Do not use
		// this object in very large collecitons; this implementation will turn a hashtable into a list!
		return getClass().hashCode();
	}
}
