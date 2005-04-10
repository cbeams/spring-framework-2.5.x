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

package org.springframework.samples.jca.model;

/**
 * @author Thierry TEMPLIER
 */
public class Person {
	private int id;
	private String lastName;
	private String firstName;
	
	public String getFirstName() {
		return firstName;
	}

	public int getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public void setFirstName(String string) {
		firstName = string;
	}

	public void setId(int i) {
		id = i;
	}

	public void setLastName(String string) {
		lastName = string;
	}

	public String toString() {
		return lastName+" "+firstName+" ("+id+")";
	}

}
