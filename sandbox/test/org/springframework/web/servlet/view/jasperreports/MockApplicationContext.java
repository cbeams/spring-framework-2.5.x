/*
 * Created on Sep 18, 2004
 */
package org.springframework.web.servlet.view.jasperreports;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author robh
 *
 */
public class MockApplicationContext implements ApplicationContext {
    
    /* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContext#getParent()
	 */
	public ApplicationContext getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContext#getDisplayName()
	 */
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContext#getStartupDate()
	 */
	public long getStartupDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
	 */
	public void publishEvent(ApplicationEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount()
	 */
	public int getBeanDefinitionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames()
	 */
	public String[] getBeanDefinitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames(java.lang.Class)
	 */
	public String[] getBeanDefinitionNames(Class type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition(java.lang.String)
	 */
	public boolean containsBeanDefinition(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class, boolean, boolean)
	 */
	public Map getBeansOfType(Class type, boolean includePrototypes,
			boolean includeFactoryBeans) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.HierarchicalBeanFactory#getParentBeanFactory()
	 */
	public BeanFactory getParentBeanFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.lang.String, java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, String defaultMessage,
			Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)
	 */
	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.MessageSource#getMessage(org.springframework.context.MessageSourceResolvable, java.util.Locale)
	 */
	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
	 */
	public Resource getResource(String location) {
		return new FileSystemResource(location);
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String)
	 */
	public Object getBean(String name) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactory#getBean(java.lang.String, java.lang.Class)
	 */
	public Object getBean(String name, Class requiredType)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactory#containsBean(java.lang.String)
	 */
	public boolean containsBean(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactory#isSingleton(java.lang.String)
	 */
	public boolean isSingleton(String name)
			throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactory#getAliases(java.lang.String)
	 */
	public String[] getAliases(String name)
			throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.ListableBeanFactory#getBeansOfType(java.lang.Class)
     */
    public Map getBeansOfType(Class type) throws BeansException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactory#getType(java.lang.String)
     */
    public Class getType(String name) throws NoSuchBeanDefinitionException {
        // TODO Auto-generated method stub
        return null;
    }





}
