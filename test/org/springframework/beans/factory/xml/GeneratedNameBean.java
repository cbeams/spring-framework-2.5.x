package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.BeanNameAware;

/**
 * @author robh
 */
public class GeneratedNameBean implements BeanNameAware {

	private String beanName;

	private GeneratedNameBean child;

	public GeneratedNameBean getChild() {
		return child;
	}

	public void setChild(GeneratedNameBean child) {
		this.child = child;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
}
