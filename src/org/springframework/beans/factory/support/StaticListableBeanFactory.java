package org.springframework.beans.factory.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StringUtils;

/**
 * Static factory that allows to register existing singleton instances programmatically.
 * @author Rod Johnson
 * @since 06-Jan-03
 * @version $Id: StaticListableBeanFactory.java,v 1.5 2003-11-28 21:09:22 jhoeller Exp $
 */
public class StaticListableBeanFactory implements ListableBeanFactory {

	/** Map from bean name to bean instance */
	private Map beans = new HashMap();

	public Object getBean(String name) throws BeansException {
		Object bean = this.beans.get(name);
		if (bean instanceof FactoryBean) {
			try {
				return ((FactoryBean) bean).getObject();
			}
			catch (Exception ex) {
				throw new FatalBeanException("Could not get object from FactoryBean", ex);
			}
		}
		if (bean == null)
			throw new NoSuchBeanDefinitionException(name, "defined beans are [" +
																										StringUtils.collectionToCommaDelimitedString(this.beans.keySet()) + "]");
		return bean;
	}
	
	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);
		if (!requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		}
		return bean;
	}

	public boolean containsBean(String name) {
		return this.beans.containsKey(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		Object bean = getBean(name);
		// in case of FactoryBean, return singleton status of created object
		if (bean instanceof FactoryBean) {
			return ((FactoryBean) bean).isSingleton();
		}
		else {
			return true;
		}
	}

	public String[] getAliases(String name) {
		return null;
	}

	public void autowireExistingBean(Object existingBean, int autowireMode, boolean dependencyCheck) {
		throw new UnsupportedOperationException("StaticListableBeanFactory does not support autowiring");
	}

	public int getBeanDefinitionCount() {
		return this.beans.size();
	}

	public String[] getBeanDefinitionNames() {
		return (String[]) this.beans.keySet().toArray(new String[this.beans.keySet().size()]);
	}

	public String[] getBeanDefinitionNames(Class type) {
		List matches = new LinkedList();
		Set keys = this.beans.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Class clazz = this.beans.get(name).getClass();
			if (type.isAssignableFrom(clazz)) {
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
		Map matches = new HashMap();
		Set keys = this.beans.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Object bean = this.beans.get(name);
			if (bean instanceof FactoryBean && includeFactoryBeans) {
				FactoryBean factory = (FactoryBean) bean;
				Class objectType = factory.getObjectType();
				if ((objectType == null && factory.isSingleton()) ||
						((factory.isSingleton() || includePrototypes) &&
						objectType != null && type.isAssignableFrom(objectType))) {
					Object createdObject = getBean(name);
					if (type.isInstance(createdObject)) {
						matches.put(name, createdObject);
					}
				}
			}
			else if (type.isAssignableFrom(bean.getClass())) {
				matches.put(name, bean);
			}
		}
		return matches;
	}

	/**
	 * Add a new singleton bean.
	 */
	public void addBean(String name, Object bean) {
		this.beans.put(name, bean);
	}

}
