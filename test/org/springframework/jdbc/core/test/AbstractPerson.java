package org.springframework.jdbc.core.test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author trisberg
 */
public class AbstractPerson {
	private String name;
	private long age;
	private java.util.Date birth_date;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public Date getBirth_date() {
		return birth_date;
	}

	public void setBirth_date(Date birth_date) {
		this.birth_date = birth_date;
	}
}
