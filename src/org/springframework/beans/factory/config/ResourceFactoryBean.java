package org.springframework.beans.factory.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean for Resource descriptors. Exposes a looked-up Resource object.
 *
 * <p>Delegates to the ApplicationContext's getResource method.
 * Resource loading behavior is specific to the context implementation.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see org.springframework.context.ApplicationContext#getResource
 */
public class ResourceFactoryBean implements FactoryBean {

	private Resource resource;

	/**
	 * Set the resource location.
	 * @param location the resource location to feed into getResource
	 */
	public void setLocation(Resource location) {
		this.resource = location;
	}

	public Object getObject() {
		return this.resource;
	}

	public Class getObjectType() {
		return (this.resource != null ? this.resource.getClass() : Resource.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
