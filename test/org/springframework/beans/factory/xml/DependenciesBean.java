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

package org.springframework.beans.factory.xml;

import org.springframework.beans.TestBean;

/**
 * Simple bean used to check dependency checking
 * @author Rod Johnson
 * @since 04-Sep-2003
 */
public class DependenciesBean {
	
	private int age;
	
	private String name;
	
	private TestBean spouse;
	
	//private Dependencies recursive;

	/**
	 * @return int
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return TestBean
	 */
	public TestBean getSpouse() {
		return spouse;
	}

	/**
	 * Sets the age.
	 * @param age The age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the spouse.
	 * @param spouse The spouse to set
	 */
	public void setSpouse(TestBean spouse) {
		this.spouse = spouse;
	}

}
