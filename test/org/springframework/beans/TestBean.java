/*
 *	$Id: TestBean.java,v 1.8 2004-03-19 17:52:29 jhoeller Exp $
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

package org.springframework.beans;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Simple test bean used for testing bean factories,
 * AOP framework etc.
 * @author  Rod Johnson
 * @since 15 April 2001
 */
public class TestBean implements BeanFactoryAware, ITestBean, IOther, Comparable {

	private BeanFactory beanFactory;

	private boolean postProcessed;

	/** Holds value of property age. */
	private int age;

	/** Holds value of property name. */
	private String name;

	private ITestBean spouse;

	private String touchy;

	private Collection friends = new LinkedList();

	private Set someSet = new HashSet();

	private Map someMap = new HashMap();

	private Date date = new Date();

	private Float myFloat = new Float(0.0);

	private INestedTestBean doctor = new NestedTestBean();

	private INestedTestBean lawyer = new NestedTestBean();

	private IndexedTestBean nestedIndexedBean;

	public String getTouchy() {
		return touchy;
	}

	public TestBean() {
	}

	public TestBean(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setPostProcessed(boolean postProcessed) {
		this.postProcessed = postProcessed;
	}

	public boolean isPostProcessed() {
		return postProcessed;
	}

	public void setTouchy(String touchy) throws Exception {
		if (touchy.indexOf('.') != -1)
			throw new Exception("Can't contain a .");
		if (touchy.indexOf(',') != -1)
			throw new NumberFormatException("Number format exception: contains a ,");
		this.touchy = touchy;
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

	public ITestBean getSpouse() {
		return spouse;
	}

	public void setSpouse(ITestBean spouse) {
		this.spouse = spouse;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Float getMyFloat() {
		return myFloat;
	}

	public void setMyFloat(Float myFloat) {
		this.myFloat = myFloat;
	}

	public boolean equals(Object other) {
		if (other == null || !(other instanceof TestBean))
			return false;
		TestBean tb2 = (TestBean) other;
		if (tb2.age != age)
			return false;

		if (name == null)
			return tb2.name == null;

		if (!tb2.name.equals(name))
			return false;

		return true;
	}

	public int compareTo(Object other) {
		if (this.name != null && other instanceof TestBean) {
			return this.name.compareTo(((TestBean) other).getName());
		}
		else {
			return 1;
		}
	}

	public String toString() {
		String s = "name=" + name + "; age=" + age + "; touchy=" + touchy;
		s += "; spouse={" + (spouse != null ? spouse.getName() : null) + "}";
		return s;
	}

	/**
	 * @see ITestBean#exceptional(Throwable)
	 */
	public void exceptional(Throwable t) throws Throwable {
		if (t != null)
			throw t;
	}

	/**
	 * @see ITestBean#returnsThis()
	 */
	public Object returnsThis() {
		return this;
	}

	/**
	 * @see IOther#absquatulate()
	 */
	public void absquatulate() {
		//System.out.println("IOther.absquatulate");
	}

	/**
	 * @return Collection
	 */
	public Collection getFriends() {
		return friends;
	}

	/**
	 * Sets the friends.
	 * @param friends The friends to set
	 */
	public void setFriends(Collection friends) {
		this.friends = friends;
	}

	public Set getSomeSet() {
		return someSet;
	}

	public void setSomeSet(Set someSet) {
		this.someSet = someSet;
	}

	public Map getSomeMap() {
		return someMap;
	}

	public void setSomeMap(Map someMap) {
		this.someMap = someMap;
	}

	public INestedTestBean getDoctor() {
		return doctor;
	}

	public INestedTestBean getLawyer() {
		return lawyer;
	}

	public void setDoctor(INestedTestBean bean) {
		doctor = bean;
	}

	public void setLawyer(INestedTestBean bean) {
		lawyer = bean;
	}

	public IndexedTestBean getNestedIndexedBean() {
		return nestedIndexedBean;
	}

	public void setNestedIndexedBean(IndexedTestBean nestedIndexedBean) {
		this.nestedIndexedBean = nestedIndexedBean;
	}

}
