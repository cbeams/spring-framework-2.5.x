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

package org.springframework.beans.factory.groovy;

import groovy.lang.GroovyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.dynamic.DynamicObjectInterceptor;
import org.springframework.beans.factory.script.DynamicScriptInterceptor;
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
	
	protected final static Log log = LogFactory.getLog(GroovyFactory.class);
	
	/**
	 * Return a reloadable object but don't poll
	 * @param className
	 * @return
	 * @throws GroovyScriptException
	 */
	public static GroovyObject dynamicObject(String className) throws BeansException {
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
	public static GroovyObject dynamicObject(String className, int pollIntervalSeconds) throws BeansException {
		GroovyScript script = new GroovyScript(className);
		GroovyObject groovyObject = (GroovyObject) script.createObject();
		
		// TODO args?
		if (ControlFlowFactory.createControlFlow().under(DynamicObjectInterceptor.class)) {
			// Tricky...
			// If the DynamicScriptInterceptor requests this bean,
			// do NOT wrap it again.
			log.info("Groovy bean being reloaded by proxy: don't wrap in proxy");
			return groovyObject;
		}
		
		// Wrap the bean in an AOP proxy to allow use of the
		// HotSwappableTargetSource
		// Requires CGLIB
		return (GroovyObject) new DynamicScriptInterceptor(script, groovyObject, pollIntervalSeconds).createProxy();
	}
		
	
	/**
	 * Create a non-dynamic Groovy bean.
	 */
	public static GroovyObject staticObject(String className) throws BeansException {
		return (GroovyObject) new GroovyScript(className).createObject();
	}

}