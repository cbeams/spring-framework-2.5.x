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

package org.springframework.beans.factory.script;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.dynamic.DynamicObjectInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ControlFlowFactory;
import org.springframework.core.io.ResourceLoader;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AbstractScriptFactory.java,v 1.1 2004-08-02 17:01:59 johnsonr Exp $
 */
public abstract class AbstractScriptFactory implements ScriptContext, ApplicationContextAware {
	
	private ResourceLoader resourceLoader;
	
	private int pollIntervalSeconds;
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public void setApplicationContext(ApplicationContext ac) {
		this.resourceLoader = ac;
	}
	
	/**
	 * Alternative to ApplicationContextAware
	 * @param resourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	/**
	 * @return Returns the defaultPollIntervalSeconds.
	 */
	public int getPollIntervalSeconds() {
		return pollIntervalSeconds;
	}
	/**
	 * @param defaultPollIntervalSeconds The defaultPollIntervalSeconds to set.
	 */
	public void setPollIntervalSeconds(int defaultPollIntervalSeconds) {
		this.pollIntervalSeconds = defaultPollIntervalSeconds;
	}
	
	
	public Object staticObject(String className) throws BeansException {
		return staticObject(className, new String[0]);
	}
	
	public Object staticObject(String className, String[] interfaceNames) throws BeansException {
		Script script = configuredScript(className, interfaceNames);
		return script.createObject();
	}
	

	public Object dynamicObject(String className) throws BeansException {
		return dynamicObject(className, new String[0]);
	}
	
	public Object dynamicObject(String className, String[] interfaceNames) throws BeansException {
		Script script = configuredScript(className, interfaceNames);
		Object o = script.createObject();
		
		// TODO args?
		if (ControlFlowFactory.createControlFlow().under(DynamicObjectInterceptor.class)) {
			// Tricky...
			// If the DynamicScriptInterceptor requests this bean,
			// do NOT wrap it again.
			log.info("Script bean being reloaded by proxy: don't wrap in proxy");
			return staticObject(className);
		}
		
		// Wrap the bean in an AOP proxy to allow use of the
		// HotSwappableTargetSource
		// Requires CGLIB
		return new DynamicScriptInterceptor(script, o, pollIntervalSeconds).createProxy();
	}
		
	
	protected Script configuredScript(String location, String[] interfaceNames) throws BeansException {
		Script script = createScript(location);
		
		// Add interfaces
		try {
			Class[] interfaces = AopUtils.toInterfaceArray(interfaceNames);
			for (int i = 0; i < interfaces.length; i++) {
				// TODO what loader
				script.addInterface(interfaces[i]);
			}
			return script;
		}
		catch (ClassNotFoundException ex) {
			throw new ScriptException("No interface found", ex) {};
		}
	}

	/**
	 * TODO can you cache a resource?
	 */
	protected abstract Script createScript(String location) throws BeansException;

}
