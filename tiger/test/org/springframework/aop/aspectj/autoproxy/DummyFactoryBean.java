package org.springframework.aop.aspectj.autoproxy;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author Rob Harrop
 */
public class DummyFactoryBean implements FactoryBean {

	public Object getObject() throws Exception {
		throw new UnsupportedOperationException();
	}

	public Class getObjectType() {
		throw new UnsupportedOperationException();
	}

	public boolean isSingleton() {
		throw new UnsupportedOperationException();
	}
}
