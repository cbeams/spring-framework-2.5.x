/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.jsf.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * JSF 1.2 <code>ELResolver</code> that delegates to the Spring root
 * <code>WebApplicationContext</code>, resolving name references to
 * Spring-defined beans.
 *
 * <p>Configure this resolver in your <code>faces-config.xml</code> file as follows:
 *
 * <pre>
 * &lt;application>
 *   ...
 *   &lt;el-resolver>org.springframework.web.jsf.el.DelegatingFacesELResolver&lt;/el-resolver>
 * &lt;/application></pre>
 *
 * All your JSF expressions can then implicitly refer to the names of
 * Spring-managed service layer beans, for example in property values of
 * JSF-managed beans:
 *
 * <pre>
 * &lt;managed-bean>
 *   &lt;managed-bean-name>myJsfManagedBean&lt;/managed-bean-name>
 *   &lt;managed-bean-class>example.MyJsfManagedBean&lt;/managed-bean-class>
 *   &lt;managed-bean-scope>session&lt;/managed-bean-scope>
 *   &lt;managed-property>
 *     &lt;property-name>mySpringManagedBusinessObject&lt;/property-name>
 *     &lt;value>#{mySpringManagedBusinessObject}&lt;/value>
 *   &lt;/managed-property>
 * &lt;/managed-bean></pre>
 *
 * with "mySpringManagedBusinessObject" defined as Spring bean in
 * applicationContext.xml:
 *
 * <pre>
 * &lt;bean id="mySpringManagedBusinessObject" class="example.MySpringManagedBusinessObject">
 *   ...
 * &lt;/bean></pre>
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.jsf.WebApplicationContextVariableResolver
 * @see org.springframework.web.jsf.FacesContextUtils#getRequiredWebApplicationContext
 */
public class SpringBeanFacesELResolver extends ELResolver {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
		if (base == null) {
			// Ask Spring root application context.
			String beanName = property.toString();
			if (logger.isTraceEnabled()) {
				logger.trace("Attempting to resolve variable '" + beanName + "' in Spring ApplicationContext");
			}
			BeanFactory bf = getBeanFactory(elContext);
			if (bf.containsBean(beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully resolved variable '" + beanName + "' in Spring ApplicationContext");
				}
				elContext.setPropertyResolved(true);
				return bf.getBean(beanName);
			}
		}

		return null;
	}

	public Class<?> getType(ELContext elContext, Object base, Object property) throws ELException {
		if (base == null) {
			// Ask Spring root application context.
			String name = property.toString();
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to resolve variable '" + name + "' in root WebApplicationContext");
			}
			BeanFactory bf = getBeanFactory(elContext);
			if (bf.containsBean(name)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Successfully resolved variable '" + name + "' in root WebApplicationContext");
				}
				elContext.setPropertyResolved(true);
				return bf.getType(name);
			}
		}

		return null;
	}

	public void setValue(ELContext elContext, Object base, Object property, Object value) throws ELException {
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) throws ELException {
		return false;
	}

	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}


	/**
	 * Retrieve the Spring BeanFactory to delegate bean name resolution to.
	 * <p>The default implementation delegates to <code>getWebApplicationContext</code>.
	 * Can be overridden to provide an arbitrary BeanFactory reference to resolve
	 * against; usually, this will be a full Spring ApplicationContext.
	 * @param elContext the current JSF ELContext
	 * @return the Spring BeanFactory (never <code>null</code>)
	 * @see #getWebApplicationContext
	 */
	protected BeanFactory getBeanFactory(ELContext elContext) {
		return getWebApplicationContext(elContext);
	}

	/**
	 * Retrieve the web application context to delegate bean name resolution to.
	 * <p>The default implementation delegates to FacesContextUtils.
	 * @param elContext the current JSF ELContext
	 * @return the Spring web application context (never <code>null</code>)
	 * @see org.springframework.web.jsf.FacesContextUtils#getRequiredWebApplicationContext
	 */
	protected WebApplicationContext getWebApplicationContext(ELContext elContext) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}

}
