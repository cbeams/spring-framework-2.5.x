package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple factory for shared Map instances. Allows for central setup
 * of Maps via the "map" element in XML bean definitions.
 * @author Hans Gilde
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class MapFactoryBean implements FactoryBean, InitializingBean {

	private Map sourceMap;

	private Class targetMapClass = HashMap.class;

	private Map targetMap;

	private boolean singleton = true;

	/**
	 * Set the source Map, typically populated via XML "map" elements.
	 */
	public void setSourceMap(Map sourceMap) {
		this.sourceMap = sourceMap;
	}

	/**
	 * Set the class to use for the target Map.
	 * Default is <code>java.util.HashMap</code>.
	 * @see java.util.HashMap
	 */
	public void setTargetMapClass(Class targetMapClass) {
		if (targetMapClass == null) {
			throw new IllegalArgumentException("targetMapClass must not be null");
		}
		if (!Map.class.isAssignableFrom(targetMapClass)) {
			throw new IllegalArgumentException("targetMapClass must implement java.util.Map");
		}
		this.targetMapClass = targetMapClass;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public void afterPropertiesSet() {
		if (this.sourceMap == null) {
			throw new IllegalArgumentException("sourceMap is required");
		}
		if (this.singleton) {
			this.targetMap = (Map) BeanUtils.instantiateClass(this.targetMapClass);
			this.targetMap.putAll(this.sourceMap);
		}
	}

	public Object getObject() {
		if (this.singleton) {
			return this.targetMap;
		}
		else {
			Map result = (Map) BeanUtils.instantiateClass(this.targetMapClass);
			result.putAll(this.sourceMap);
			return result;
		}
	}

	public Class getObjectType() {
		return Map.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
