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

package org.springframework.web.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;

/**
 * JSF VariableResolver that first delegates to the original resolver of the
 * underlying JSF implementation, then to the Spring root WebApplicationContext.
 *
 * <p>Configure this resolver in your faces-config.xml file as follows:
 *
 * <pre>
 * &lt;application>
 *   ...
 *   &lt;variable-resolver>org.springframework.web.jsf.DelegatingVariableResolver&lt;/variable-resolver>
 * &lt;/application></pre>
 *
 * All your JSF expressions can then implicitly refer to the names of
 * Spring-managed middle tier beans, for example in property values of
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
 * <b>Note:</b> Spring's JSF support has been developed and tested against
 * JSF 1.1. Unfortunately, the JSF 1.1 RI (as of June 2004) does not apply a
 * custom VariableResolver to property values of JSF-managed beans: This has
 * to be considered a bug, as it is supposed to work according to the JSF spec.
 * It does work in <a href="http://www.marinschek.com/myfaces/tiki">MyFaces</a>
 * 1.0.5, for example.
 *
 * @author Juergen Hoeller
 * @since 22.06.2004
 * @see FacesContextUtils#getRequiredWebApplicationContext
 */
public class DelegatingVariableResolver extends VariableResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	protected final VariableResolver originalVariableResolver;

	/**
	 * Create a new DelegatingVariableResolver, using the given original VariableResolver.
	 * <p>A JSF implementation will automatically pass its original resolver into the
	 * constructor of a configured resolver, provided that there is a corresponding
	 * constructor argument.
	 * @param originalVariableResolver the original VariableResolver
	 */
	public DelegatingVariableResolver(VariableResolver originalVariableResolver) {
		this.originalVariableResolver = originalVariableResolver;
	}

	/**
	 * Return the original VariableResolver that this resolver delegates to.
	 */
	protected final VariableResolver getOriginalVariableResolver() {
		return originalVariableResolver;
	}

	public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException {
		// ask original resolver
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve variable '" + name + "' in via original VariableResolver");
		}
		Object originalResult = this.originalVariableResolver.resolveVariable(facesContext, name);
		if (originalResult != null) {
			return originalResult;
		}

		// ask Spring root context
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve variable '" + name + "' in root WebApplicationContext");
		}
		WebApplicationContext wac = getWebApplicationContext(facesContext);
		if (wac.containsBean(name)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Successfully resolved variable '" + name + "' in root WebApplicationContext");
			}
			return wac.getBean(name);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Could not resolve variable '" + name + "'");
		}
		return null;
	}

	/**
	 * Retrieve the web application context to delegate bean name resolution to.
	 * Default implementation delegates to FacesContextUtils.
	 * @param facesContext the current JSF context
	 * @return the Spring web application context
	 * @see FacesContextUtils#getRequiredWebApplicationContext
	 */
	protected WebApplicationContext getWebApplicationContext(FacesContext facesContext) {
		return FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}

}
