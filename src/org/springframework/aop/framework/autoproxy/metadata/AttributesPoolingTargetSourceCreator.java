/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.metadata;

import java.util.Collection;

import org.springframework.aop.framework.autoproxy.target.AbstractPoolingTargetSourceCreator;
import org.springframework.aop.framework.autoproxy.target.PoolingAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.metadata.Attributes;

/**
 * PoolingTargetSourceCreator driven by metadata.
 * @author Rod Johnson
 * @version $Id: AttributesPoolingTargetSourceCreator.java,v 1.2 2003-12-30 01:07:11 jhoeller Exp $
 */
public class AttributesPoolingTargetSourceCreator extends AbstractPoolingTargetSourceCreator {

	private final Attributes attributes;

	public AttributesPoolingTargetSourceCreator(Attributes attributes) {
		this.attributes = attributes;
	}

	protected PoolingAttribute getPoolingAttribute(Object bean, String beanName, BeanFactory beanFactory) {
		Class beanClass = bean.getClass();
		// See if there's a pooling attribute
		Collection atts = attributes.getAttributes(beanClass, PoolingAttribute.class);
		if (atts.isEmpty()) {
			// No pooling attribute
			return null;
		}
		else if (atts.size() > 1) {
			throw new BeanDefinitionStoreException("Cannot have more than one pooling attribute on " + beanClass);
		}
		else {
			return (PoolingAttribute) atts.iterator().next();
		}
	}

}