package org.springframework.context.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.support.ApplicationObjectSupport;
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
public class ContextResourceFactoryBean extends ApplicationObjectSupport implements FactoryBean {

	private String location;

	private Resource resource;

	/**
	 * Set the resource location.
	 * @param location the resource location to feed into getResource
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	protected void initApplicationContext() {
		this.resource = getApplicationContext().getResource(this.location);
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
