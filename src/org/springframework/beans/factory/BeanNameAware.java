package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that want to be aware of their
 * bean name in a bean factory.
 *
 * <p>For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Juergen Hoeller
 * @since 01.11.2oo3
 * @see BeanFactoryAware
 * @see InitializingBean
 * @see BeanFactory
 */
public interface BeanNameAware {

	/**
	 * Set the name of the bean in the bean factory that created this bean.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * @param name the name of the bean in the factory
	 */
	void setBeanName(String name);

}
