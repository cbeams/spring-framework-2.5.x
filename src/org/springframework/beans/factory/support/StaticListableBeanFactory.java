package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Static factory that allows to register existing singleton instances programmatically.
 * @author Rod Johnson
 * @since 06-Jan-03
 * @version $Id: StaticListableBeanFactory.java,v 1.2 2003-10-31 17:01:27 jhoeller Exp $
 */
public class StaticListableBeanFactory implements ListableBeanFactory {

	/** Map from bean name to bean instance */
	private Map beans = new HashMap();

	public Object getBean(String name) throws BeansException {
		Object bean = this.beans.get(name);
		if (bean == null)
			throw new NoSuchBeanDefinitionException(name, "No such bean");
		return bean;
	}
	
	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);

		if (!requiredType.isAssignableFrom(bean.getClass()))
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);

		return bean;
	}

	public boolean isSingleton(String name) {
		return true;
	}

	public String[] getAliases(String name) {
		return null;
	}

	public int getBeanDefinitionCount() {
		return beans.size();
	}

	public String[] getBeanDefinitionNames() {
		return (String[]) beans.keySet().toArray(new String[beans.keySet().size()]);
	}

	public String[] getBeanDefinitionNames(Class type) {
		Set keys = beans.keySet();
		List matches = new LinkedList();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Class clazz = beans.get(name).getClass();
			if (type.isAssignableFrom(clazz)) {
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	public Map getBeansOfType(Class type) {
		Map result = new HashMap();
		String[] beanNames = getBeanDefinitionNames(type);
		for (int i = 0; i < beanNames.length; i++) {
			result.put(beanNames[i], getBean(beanNames[i]));
		}
		return result;
	}

	/**
	 * Add a new singleton bean.
	 */
	public void addBean(String name, Object bean) {
		this.beans.put(name, bean);
	}

}
