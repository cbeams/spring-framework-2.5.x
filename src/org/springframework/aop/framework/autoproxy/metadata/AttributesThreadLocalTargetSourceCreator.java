/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.framework.autoproxy.target.AbstractPrototypeTargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PrototypeTargetSourceCreator driven by metadata.
 * Creates a ThreadLocalTargetSource
 * only if there's a ThreadLocalAttribute associated with the class.
 * @author Rod Johnson
 * @version $Id: AttributesThreadLocalTargetSourceCreator.java,v 1.1 2003-12-15 17:14:35 johnsonr Exp $
 */
public class AttributesThreadLocalTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	private final Attributes attributes;

	public AttributesThreadLocalTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory bf) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, ThreadLocalAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute: don't create a custom TargetSource
			return null;
		}
		else {
			return new ThreadLocalTargetSource();
		}
	}

}