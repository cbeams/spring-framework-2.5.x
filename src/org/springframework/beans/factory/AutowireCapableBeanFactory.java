package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Extension of the ListableBeanFactory interface to be implemented by bean
 * factories that are capable of autowiring and expose this functionality
 * for existing bean instances.
 * @author Juergen Hoeller
 * @since 04.12.2003
 */
public interface AutowireCapableBeanFactory extends ListableBeanFactory {

	/**
	 * Constant that indicates autowiring by name.
	 * @see #autowireExistingBean
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring by type.
	 * @see #autowireExistingBean
	 */
	int AUTOWIRE_BY_TYPE = 2;


	/**
	 * Create a new bean instance of the given class by autowiring
	 * an appropriate constructor.
	 * @param beanClass the class of the bean to instantiate
	 * @return the new bean instance
	 * @throws BeansException if instantiation respectively wiring failed
	 */
	Object autowireConstructor(Class beanClass) throws BeansException;

	/**
	 * Autowire the given existing bean instance by name or type.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 */
	void autowireExistingBean(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Apply BeanPostProcessors to the given existing bean instance.
	 * Will also check for BeanNameAware and BeanFactoryAware,
	 * which might be implemented as BeanPostProcessors by bean factories.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see org.springframework.beans.factory.config.BeanPostProcessor
	 * @see BeanNameAware
	 * @see BeanFactoryAware
	 */
	Object applyBeanPostProcessors(Object existingBean, String name) throws BeansException;
	
	/**
	 * Register a singleton instance of the given class, with autowiring via constructor
	 * or bean properties if possible.
	 * This is useful when we have a bean class, and want to add a bean of that class
	 * to an existing factory, benefiting from autowiring.
	 * <br>Unlike other autowire methods, the instance is added to this factory.
	 * @param beanName name of the registered bean
	 * @param beanClass class of bean to add to context
	 * @param dependencyCheck whether dependency check should be performed for objects.
	 * Dependency check for simple parameters is impossible.
	 * @return the configured bean if successful
	 */
	Object registerBeanOfClass(String beanName, Class beanClass, boolean dependencyCheck) throws BeansException;

}
