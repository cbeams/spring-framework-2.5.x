package org.springframework.context.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

// let the class implement the interface and it'll fail!
public class Assembler /*implements TestIF*/ {
	
	private Service service;
	private Logic l;	
	private String name;
	
	public void setService(Service service) {
		this.service = service;
	}
	
	public void setLogic(Logic l) {
		this.l = l;
	}
	
	public void setBeanName(String name) {
		this.name = name;
	}
	
	public void afterPropertiesSet() throws Exception {

	}
	
	public void test() {		
	}
	
	public void output() {
		System.out.println("Bean " + name);
		l.output();
	}
}
