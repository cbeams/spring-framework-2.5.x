package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;

/**
 * Simple factory for shared Map instances. Allows for central setup
 * of Maps via the "map" element in XML bean definitions.
 * @author Hans Gilde
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class MapFactoryBean implements FactoryBean {

	private Map map;

	private boolean singleton = true;

	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public Object getObject() {
		if (this.singleton) {
			return this.map;
		}
		else {
			return new HashMap(this.map);
		}
	}

	public Class getObjectType() {
		return Map.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
