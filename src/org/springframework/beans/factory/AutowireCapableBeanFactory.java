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
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring by type.
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;


	/**
	 * Create a new bean instance of the given class by guessing the
	 * appropriate autowiring strategy: if there is a no-arg constructor,
	 * autowire by type, else autowire a constructor.
	 * @param beanClass the class of the bean to instantiate
	 * @return the new bean instance
	 * @throws BeansException if instantiation respectively wiring failed
	 * @see #autowireConstructor
	 * @see #autowireBeanProperties
	 */
	Object autowire(Class beanClass) throws BeansException;

	/**
	 * Create a new bean instance of the given class by autowiring
	 * an appropriate constructor.
	 * @param beanClass the class of the bean to instantiate
	 * @return the new bean instance
	 * @throws BeansException if instantiation respectively wiring failed
	 */
	Object autowireConstructor(Class beanClass) throws BeansException;

	/**
	 * Autowire the bean properties of the given bean instance by name or type.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
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

}
