/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.target.PrototypeTargetSourceCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PrototypeTargetSourceCreator driven by metadata.
 * Overrides PrototypeTargetSourceCreator.createTargetSource() to create a prototype
 * only if there's a PrototypeAttribute.
 * @author Rod Johnson
 * @version $Id: AttributesPrototypeTargetSourceCreator.java,v 1.1 2003-12-15 10:07:07 johnsonr Exp $
 */
public class AttributesPrototypeTargetSourceCreator extends PrototypeTargetSourceCreator {

	private final Attributes attributes;

	public AttributesPrototypeTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected TargetSource createTargetSource(Object bean, String beanName, BeanFactory bf) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, PrototypeAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute: don't create a custom TargetSource
			return null;
		}
		else {
			// Let the superclass return a PrototypeTargetSource
			return super.getTargetSource(bean, beanName, bf);
		}
	}

}