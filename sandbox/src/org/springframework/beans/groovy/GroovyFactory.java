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

package org.springframework.beans.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.core.ControlFlowFactory;

/**
 * Class containing static factory methods for wrapping
 * Groovy bean definitions.
 * These methods are intended for use with the Spring 1.1
 * IoC container's "factory-method" feature.
 * <p>The dynamicObject() methods return objects that can be cast to
 * DynamicScript to allow query and manipulation of the script status.
 * <p>
 * Groovy objects must have a no-arg constructor. You can use
 * Setter Injection on them, not Constructor or Method
 * Injection.
 * 
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14
 *          16:20:14 trisberg Exp $
 */
public class GroovyFactory {
	
	// TODO className should be a resource
	
	protected final static Log log = LogFactory.getLog(GroovyFactory.class);
	
	/**
	 * Return a reloadable object but don't poll
	 * @param className
	 * @return
	 * @throws GroovyScriptException
	 */
	public static GroovyObject dynamicObject(String className) throws GroovyScriptException {
		return dynamicObject(className, 0);
	}

	/**
	 * Create a dynamic Groovy Bean.
	 * This method can only be called in a BeanFactory.
	 * @param className
	 * @param checkInterval
	 * @return
	 * @throws GroovyScriptException
	 */
	public static GroovyObject dynamicObject(String className, int pollIntervalSeconds) throws GroovyScriptException {
		GroovyObject groovyObject = staticObject(className);
		
		if (ControlFlowFactory.createControlFlow().under(DynamicScriptInterceptor.class)) {
			// Tricky...
			// If the DynamicScriptInterceptor requests this bean,
			// do NOT wrap it again.
			log.info("Groovy bean being reloaded by proxy: don't wrap in proxy");
			return groovyObject;
		}
		
		// Wrap the bean in an AOP proxy to allow use of the
		// HotSwappableTargetSource
		// Requires CGLIB
		
		// Need GroovyTargetSource??
		HotSwappableTargetSource gts = new HotSwappableTargetSource(groovyObject);
		
		ProxyFactory pf = new ProxyFactory();
		
		// Force the use of CGLIB
		pf.setProxyTargetClass(true);
		
		// Set the HotSwappableTargetSource
		pf.setTargetSource(gts);
		
		// Add the DynamicScript introduction
		pf.addAdvisor(new DefaultIntroductionAdvisor(new DynamicScriptInterceptor(className, gts, pollIntervalSeconds)));
		
		GroovyObject wrapped = (GroovyObject) pf.getProxy();
		
		return wrapped;
	}
		
	
	/**
	 * Create a non-dynamic Groovy bean.
	 */
	public static GroovyObject staticObject(String className) throws GroovyScriptException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class groovyClass = null;
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(cl);
		//System.out.println(groovyCl);
		try {
			URL url = cl.getResource(className);
						
			if (url == null) {
				throw new ScriptNotFoundException("Script " + className + " not found");
			}
			//System.out.println(url.getFile());
			InputStream is = url.openStream();
			
			groovyClass = groovyClassLoader
					.parseClass(is);
			log.info("Loaded groovy class " + groovyClass);
			return (GroovyObject) groovyClass.newInstance();

		} 
		catch (CompilationFailedException ex) {
			throw new CompilationException("Error parsing " + className, ex);
		} 
		catch (IOException ex) {
			// TODO diff one
			throw new ScriptNotFoundException("Error reading " + className, ex);
		}
		catch (IllegalAccessException ex) {
			// TODO break up
			throw new CannotInstantiateGroovyClassException("Error instantiating" + groovyClass, ex);
		}
		catch (InstantiationException ex) {
			// TODO break up
			throw new CannotInstantiateGroovyClassException("Error instantiating" + groovyClass, ex);
		}

	}

}