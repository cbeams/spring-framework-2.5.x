package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;


public class Logic implements BeanNameAware {
	
	private Log log = LogFactory.getLog(Logic.class);
	private String name;
	private Assembler a;
	
	public void setAssembler(Assembler a) {
		this.a = a;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		this.name = name;
	}
	
	public void output() {
		System.out.println("Bean " + name);		
	}

}
