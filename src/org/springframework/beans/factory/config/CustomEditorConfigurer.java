/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * BeanFactoryPostProcessor implementation that allows for convenient
 * registration of custom property editors.
 *
 * <p>Configuration example, assuming XML bean definitions and inner
 * beans for PropertyEditor instances:
 *
 * <pre>
 * &lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="customEditors"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="java.util.Date"&gt;
 *         &lt;bean class="mypackage.MyCustomDateEditor"/&gt;
 *       &lt;/entry&gt;
 *       &lt;entry key="mypackage.MyObject"&gt;
 *         &lt;bean id="myEditor" class="mypackage.MyObjectEditor"&gt;
 *           &lt;property name="myParam"&gt;&lt;value&gt;myValue&lt;/value&gt;&lt;/property&gt;
 *         &lt;/bean&gt;
 *       &lt;/entry&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * <p>Also supports "java.lang.String[]"-style array class names.
 * Delegates to ClassUtils for actual class name resolution.
 *
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see ConfigurableBeanFactory#registerCustomEditor
 * @see org.springframework.util.ClassUtils#forName
 */
public class CustomEditorConfigurer implements BeanFactoryPostProcessor, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Map customEditors;

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Specify the custom editors to register via a Map, using the class name
	 * of the required type as key and the PropertyEditor instance as value.
	 * @see ConfigurableListableBeanFactory#registerCustomEditor
	 */
	public void setCustomEditors(Map customEditors) {
		this.customEditors = customEditors;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.customEditors != null) {
			for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
				Object key = it.next();
				Class requiredType = null;
				if (key instanceof Class) {
					requiredType = (Class) key;
				}
				else if (key instanceof String) {
					String className = (String) key;
					try {
						requiredType = ClassUtils.forName(className);
					}
					catch (ClassNotFoundException ex) {
						throw new BeanInitializationException(
								"Could not load required type [" + className + "] for custom editor", ex);
					}
				}
				else {
					throw new BeanInitializationException(
							"Invalid key [" + key + "] for custom editor - needs to be Class or String");
				}
				Object value = this.customEditors.get(key);
				if (!(value instanceof PropertyEditor)) {
					throw new BeanInitializationException("Mapped value [" + value + "] for custom editor key [" +
							key + "] is not of required type [" + PropertyEditor.class.getName() + "]");
				}
				beanFactory.registerCustomEditor(requiredType, (PropertyEditor) value);
			}
		}
	}

}
