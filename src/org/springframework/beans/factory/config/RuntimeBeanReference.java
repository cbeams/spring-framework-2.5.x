package org.springframework.beans.factory.config;

/** 
 * Immutable placeholder class used for the value of a PropertyValue
 * object when it's a reference to another bean in this factory
 * to be resolved at runtime.
 * @author Rod Johnson
 */
public class RuntimeBeanReference {
	
	private final String beanName;

	/**
	 * Create a new RuntimeBeanReference to the given bean name.
	 * @param beanName name of the target bean
	 */
	public RuntimeBeanReference(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Return the target bean name.
	 */
	public String getBeanName() {
		return beanName;
	}

	public String toString() {
	   return '<' + getBeanName() + '>';
	}

}
