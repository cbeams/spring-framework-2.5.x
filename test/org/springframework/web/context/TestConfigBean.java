/*
 * TestConfigBean.java
 *
 * Created on 14 December 2001, 18:14
 */

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

package org.springframework.web.context;

/**
 *
 * @author  rod
 * @version 
 */
public class TestConfigBean {

	/** Holds value of property name. */
	private String name;
	
	/** Holds value of property age. */
	private int age;
	
	/** Creates new TestConfigBean */
    public TestConfigBean() {
    }

	/** Getter for property name.
	 * @return Value of property name.
	 */
	public String getName() {
		return name;
	}
	
	/** Setter for property name.
	 * @param name New value of property name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** Getter for property age.
	 * @return Value of property age.
	 */
	public int getAge() {
		return age;
	}
	
	/** Setter for property age.
	 * @param age New value of property age.
	 */
	public void setAge(int age) {
		this.age = age;
	}
	
}
