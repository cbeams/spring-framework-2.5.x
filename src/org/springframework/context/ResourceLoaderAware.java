package org.springframework.context;

import org.springframework.core.io.ResourceLoader;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the ResourceLoader (typically the ApplicationContext) that it runs in.
 *
 * <p>Note that Resource dependencies can also be exposed as bean properties
 * of type Resource, populated via Strings with automatic type conversion by
 * the bean factory. This removes the need for implementing any callback
 * interface just for the purpose of accessing a specific file resource.
 *
 * <p>You typically need a ResourceLoader when your application object has
 * to access a variety of file resources whose names are calculated. A good
 * strategy is to make the object use a DefaultResourceLoader but still
 * implement ResourceLoaderAware to allow for overriding when running in an
 * ApplicationContext. See ReloadableResourceBundleMessageSource for an example.
 * 
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see ApplicationContextAware
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
public interface ResourceLoaderAware {

	/**
	 * Set the ResourceLoader that this object runs in.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked before ApplicationContextAware's setApplicationContext.
	 * @param resourceLoader ResourceLoader object to be used by this object
	 */
	void setResourceLoader(ResourceLoader resourceLoader);

}
