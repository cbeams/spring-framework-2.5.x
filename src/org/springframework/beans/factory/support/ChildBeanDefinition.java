/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import org.springframework.beans.PropertyValues;

/**
 * Extension of BeanDefinition interface for beans whose class is defined
 * by their ancestry. PropertyValues defined by the parent will also be
 * "inherited", although it's possible to override them by redefining
 * them in the property values associated with the child.
 * @author Rod Johnson
 * @version $Revision: 1.3 $
 */
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	public ChildBeanDefinition(String parentName, PropertyValues pvs, boolean singleton) {
		super(pvs, singleton);
		this.parentName = parentName;
	}

	/**
	 * Return the name of the parent bean definition in
	 * the current bean factory.
	 */
	public String getParentName() {
		return parentName;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof ChildBeanDefinition))
			return false;
		return super.equals(obj) && ((ChildBeanDefinition) obj).getParentName().equals(this.getParentName());
	}

	public String toString() {
		return "Child bean definition with parent '" + getParentName() + "'";
	}

}
