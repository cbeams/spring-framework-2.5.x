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

package org.springframework.web.servlet.mvc.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;

/**
 * Implementation of {@link HandlerMapping} that follows a simple convention for
 * generating URL path mappings from the class names of registered
 * {@link org.springframework.web.servlet.mvc.Controller} and
 * {@link org.springframework.web.servlet.mvc.throwaway.ThrowawayController} beans.
 *
 * <p>For simple {@link org.springframework.web.servlet.mvc.Controller} implementations
 * (those that handle a single request type), the convention is to take the
 * {@link ClassUtils#getShortName short name} of the <code>Class</code>,
 * remove the 'Controller' suffix if it exists and return the remaining text, lowercased,
 * as the mapping, with a leading <code>/</code>. For example:
 * <ul>
 * <li><code>WelcomeController</code> -> <code>/welcome*</code></li>
 * <li><code>HomeController</code> -> <code>/home*</code></li>
 * </ul>
 *
 * <p>For {@link MultiActionController MultiActionControllers} then a similar mapping is registered,
 * except that all sub-paths are registed using the trailing wildcard pattern <code>/*</code>.
 * For example:
 * <ul>
 * <li><code>WelcomeController</code> -> <code>/welcome/*</code></li>
 * <li><code>CatalogController</code> -> <code>/catalog/*</code></li>
 * </ul>
 *
 * <p>For {@link MultiActionController} it is often useful to use
 * this mapping strategy in conjunction with the
 * {@link org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver}.
 *
 * <p>Thanks to Warren Oliver for suggesting the "caseSensitive", "pathPrefix"
 * and "basePackage" properties which have been added in Spring 2.5.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.servlet.mvc.throwaway.ThrowawayController
 * @see org.springframework.web.servlet.mvc.multiaction.MultiActionController
 */
public class ControllerClassNameHandlerMapping extends AbstractUrlHandlerMapping implements HandlerMapping {

	/**
	 * Common suffix at the end of controller implementation classes.
	 * Removed when generating the URL path.
	 */
	private static final String CONTROLLER_SUFFIX = "Controller";


	private Set excludedPackages = Collections.singleton("org.springframework.web.servlet.mvc");

	private Set excludedClasses = Collections.EMPTY_SET;

	private boolean caseSensitive = false;

	private String pathPrefix;

	private String basePackage;


	/**
	 * Specify Java packages that should be excluded from this mapping.
	 * Any classes in such a package (or any of its subpackages) will be
	 * ignored by this HandlerMapping.
	 * <p>Default is to exclude the entire "org.springframework.web.servlet.mvc"
	 * package, including its subpackages, since none of Spring's out-of-the-box
	 * Controller implementations is a reasonable candidate for this mapping strategy.
	 * Such controllers are typically handled by a separate HandlerMapping,
	 * e.g. a {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping},
	 * alongside this ControllerClassNameHandlerMapping for application controllers.
	 */
	public void setExcludedPackages(String[] excludedPackages) {
		this.excludedPackages =
				(excludedPackages != null ? new HashSet(Arrays.asList(excludedPackages)) : Collections.EMPTY_SET);
	}

	/**
	 * Specify controller classes that should be excluded from this mapping.
	 * Any such classes will simply be ignored by this HandlerMapping.
	 */
	public void setExcludedClasses(Class[] excludedClasses) {
		this.excludedClasses =
				(excludedClasses != null ? new HashSet(Arrays.asList(excludedClasses)) : Collections.EMPTY_SET);
	}

	/**
	 * Set whether to apply case sensitivity to the generated paths,
	 * e.g. turning the class name "BuyForm" into "buyForm".
	 * <p>Default is "false", using pure lower case paths,
	 * e.g. turning the class name "BuyForm" into "buyform".
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * Specify a prefix to prepend to the path generated from the controller name.
	 * <p>Default is a plain slash ("/"). A path like "/mymodule" can be specified
	 * in order to have controller path mappings prefixed with that path, e.g.
	 * "/mymodule/buyform" instead of "/buyform" for the class name "BuyForm".
	 */
	public void setPathPrefix(String prefixPath) {
		this.pathPrefix = prefixPath;
		if (StringUtils.hasLength(this.pathPrefix)) {
			if (!this.pathPrefix.startsWith("/")) {
				this.pathPrefix = "/" + this.pathPrefix;
			}
			if (this.pathPrefix.endsWith("/")) {
				this.pathPrefix = this.pathPrefix.substring(0, this.pathPrefix.length() - 1);
			}
		}
	}

	/**
	 * Set the base package to be used for generating path mappings,
	 * including all subpackages underneath this packages as path elements.
	 * <p>Default is <code>null</code>, using the short class name for the
	 * generated path, with the controller's package not represented in the path.
	 * Specify a base package like "com.mycompany.myapp" to include subpackages
	 * within that base package as path elements, e.g. generating the path
	 * "/mymodule/buyform" for the class name "com.mycompany.myapp.mymodule.BuyForm".
	 * Subpackage hierarchies are represented as individual path elements,
	 * e.g. "/mymodule/mysubmodule/buyform" for the class name
	 * "com.mycompany.myapp.mymodule.mysubmodule.BuyForm".
	 */
	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
		if (StringUtils.hasLength(this.basePackage) && !this.basePackage.endsWith(".")) {
			this.basePackage = this.basePackage + ".";
		}
	}


	/**
	 * Calls the {@link #detectControllers()} method in addition to the
	 * superclass's initialization.
	 */
	protected void initApplicationContext() {
		super.initApplicationContext();
		detectControllers();
	}

	/**
	 * Detect all the {@link org.springframework.web.servlet.mvc.Controller} and
	 * {@link org.springframework.web.servlet.mvc.throwaway.ThrowawayController}
	 * beans registered in the {@link org.springframework.context.ApplicationContext}
	 * and register a URL path mapping for each one based on rules defined here.
	 * @throws BeansException if the controllers couldn't be obtained or registered
	 * @see #generatePathMapping(Class)
	 */
	protected void detectControllers() throws BeansException {
		registerControllers(Controller.class);
		registerControllers(ThrowawayController.class);
	}

	/**
	 * Register all controllers of the given type, searching the current
	 * DispatcherServlet's ApplicationContext for matching beans.
	 * @param controllerType the type of controller to search for
	 * @throws BeansException if the controllers couldn't be obtained or registered
	 */
	protected void registerControllers(Class controllerType) throws BeansException {
		String[] beanNames = getApplicationContext().getBeanNamesForType(controllerType);
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			Class beanClass = getApplicationContext().getType(beanName);
			if (isEligibleForMapping(beanName, beanClass)) {
				registerController(beanName, beanClass);
			}
		}
	}

	/**
	 * Determine whether the specified controller is excluded from this mapping.
	 * @param beanName the name of the controller bean
	 * @param beanClass the concrete class of the controller bean
	 * @return whether the specified class is excluded
	 * @see #setExcludedPackages
	 * @see #setExcludedClasses
	 */
	protected boolean isEligibleForMapping(String beanName, Class beanClass) {
		if (beanClass == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Excluding controller bean '" + beanName + "' from class name mapping " +
						"because its bean type could not be determined");
			}
			return false;
		}
		if (this.excludedClasses.contains(beanClass)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Excluding controller bean '" + beanName + "' from class name mapping " +
						"because its bean class is explicitly excluded: " + beanClass.getName());
			}
			return false;
		}
		String beanClassName = beanClass.getName();
		for (Iterator it = this.excludedPackages.iterator(); it.hasNext();) {
			String packageName = (String) it.next();
			if (beanClassName.startsWith(packageName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Excluding controller bean '" + beanName + "' from class name mapping " +
							"because its bean class is defined in an excluded package: " + beanClass.getName());
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Register the controller with the given name, as defined
	 * in the current application context.
	 * @param beanName the name of the controller bean
	 * @param beanClass the concrete class of the controller bean
	 * @throws BeansException if the controller couldn't be registered
	 * @throws IllegalStateException if there is a conflicting handler registered
	 * @see #getApplicationContext()
	 */
	protected void registerController(String beanName, Class beanClass) throws BeansException, IllegalStateException {
		String urlPath = generatePathMapping(beanClass);
		if (logger.isDebugEnabled()) {
			logger.debug("Registering Controller '" + beanName + "' as handler for URL path [" + urlPath + "]");
		}
		registerHandler(urlPath, beanName);
	}

	/**
	 * Generate the actual URL path for the given controller class.
	 * <p>Subclasses may choose to customize the paths that are generated
	 * by overriding this method.
	 * @param beanClass the controller bean class to generate a mapping for
	 * @return the URL path mapping for the given controller
	 */
	protected String generatePathMapping(Class beanClass) {
		StringBuffer pathMapping = buildPathPrefix(beanClass);
		String className = ClassUtils.getShortName(beanClass);
		String path = (className.endsWith(CONTROLLER_SUFFIX) ?
				className.substring(0, className.indexOf(CONTROLLER_SUFFIX)) : className);
		if (path.length() > 0) {
			if (this.caseSensitive) {
				pathMapping.append(path.substring(0, 1).toLowerCase() + path.substring(1));
			}
			else {
				pathMapping.append(path.toLowerCase());
			}
		}
		if (MultiActionController.class.isAssignableFrom(beanClass)) {
			pathMapping.append("/*");
		}
		else {
			pathMapping.append("*");
		}
		return pathMapping.toString();
	}

	/**
	 * Build a path prefix for the given controller bean class.
	 * @param beanClass the controller bean class to generate a mapping for
	 * @return the path prefix, potentially including subpackage names as path elements
	 */
	private StringBuffer buildPathPrefix(Class beanClass) {
		StringBuffer pathMapping = new StringBuffer();
		if (this.pathPrefix != null) {
			pathMapping.append(this.pathPrefix);
			pathMapping.append("/");
		}
		else {
			pathMapping.append("/");
		}
		if (this.basePackage != null) {
			String packageName = ClassUtils.getPackageName(beanClass);
			if (packageName.startsWith(this.basePackage)) {
				String subPackage = packageName.substring(this.basePackage.length()).replace('.', '/');
				pathMapping.append(this.caseSensitive ? subPackage : subPackage.toLowerCase());
				pathMapping.append("/");
			}
		}
		return pathMapping;
	}

}
