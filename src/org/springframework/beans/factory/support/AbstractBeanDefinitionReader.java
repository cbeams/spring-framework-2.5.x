package org.springframework.beans.factory.support;

/**
 * Abstract base class for bean definition readers.
 * Provides common properties like the bean factory to work on
 * and the class loader to use for loading bean classes. 
 * @author Juergen Hoeller
 * @since 11.12.2003
 */
public abstract class AbstractBeanDefinitionReader {

	private BeanDefinitionRegistry beanFactory;

	private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();

	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the BeanFactory that this reader works on.
	 */
	public BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Set the class loader to use for bean classes.
	 * Default is the thread context class loader.
	 * <p>Setting this to null suggests to not load bean classes but just register
	 * bean definitions with class names, for example when just registering beans
	 * in a registry but not actually instantiating them in a factory.
	 * @see java.lang.Thread#getContextClassLoader
	 */
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	/**
	 * Return the class loader for bean classes.
	 */
	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

}
