package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that want to be aware of their
 * bean name in a bean factory.
 * @author Juergen Hoeller
 * @since 01.11.2oo3
 */
public interface BeanNameAware {

	/**
	 * Set the name of the bean in the bean factory that created this bean.
	 * @param name the name of the bean in the factory
	 */
	void setBeanName(String name);

}
