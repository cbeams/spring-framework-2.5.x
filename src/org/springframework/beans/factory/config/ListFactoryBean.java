package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

/**
 * Simple factory for shared List instances. Allows for central setup
 * of Lists via the "list" element in XML bean definitions.
 * @author Hans Gilde
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class ListFactoryBean implements FactoryBean {

	private List list;

	private boolean singleton = true;

	public void setList(List list) {
		this.list = list;
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
			return this.list;
		}
		else {
			return new ArrayList(this.list);
		}
	}

	public Class getObjectType() {
		return List.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
