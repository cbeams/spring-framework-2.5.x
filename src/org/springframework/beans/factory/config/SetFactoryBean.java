package org.springframework.beans.factory.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple factory for shared Set instances. Allows for central setup
 * of Sets via the "set" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 21.01.2003
 */
public class SetFactoryBean implements FactoryBean, InitializingBean {

	private Set sourceSet;

	private Class targetSetClass = HashSet.class;

	private Set targetSet;

	private boolean singleton = true;

	/**
	 * Set the source Set, typically populated via XML "set" elements.
	 */
	public void setSourceSet(Set sourceSet) {
		this.sourceSet = sourceSet;
	}

	/**
	 * Set the class to use for the target Set.
	 * Default is <code>java.util.HashSet</code>.
	 * @see java.util.HashSet
	 */
	public void setTargetSetClass(Class targetSetClass) {
		if (targetSetClass == null) {
			throw new IllegalArgumentException("targetSetClass must not be null");
		}
		if (!Set.class.isAssignableFrom(targetSetClass)) {
			throw new IllegalArgumentException("targetSetClass must implement java.util.Set");
		}
		this.targetSetClass = targetSetClass;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void afterPropertiesSet() {
		if (this.sourceSet == null) {
			throw new IllegalArgumentException("sourceSet is required");
		}
		if (this.singleton) {
			this.targetSet = (Set) BeanUtils.instantiateClass(this.targetSetClass);
			this.targetSet.addAll(this.sourceSet);
		}
	}

	public Object getObject() {
		if (this.singleton) {
			return this.targetSet;
		}
		else {
			Set result = (Set) BeanUtils.instantiateClass(this.targetSetClass);
			result.addAll(this.sourceSet);
			return result;
		}
	}

	public Class getObjectType() {
		return Set.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
