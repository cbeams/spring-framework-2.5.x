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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.script.Script;
import org.springframework.beans.factory.script.ScriptNotFoundException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14
 *          16:20:14 trisberg Exp $
 */
public class GroovyScript implements Script {

	protected final Log log = org.apache.commons.logging.LogFactory
			.getLog(getClass());

	private String className;
	
	private long lastReloadTime;

	// TODO take a Resource?
	public GroovyScript(String className) {
		this.className = className;
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#getResourceString()
	 */
	public String getResourceString() {
		return className;
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#createObject()
	 */
	public Object createObject() throws BeansException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class groovyClass = null;
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(cl);
		//System.out.println(groovyCl);
		try {
			URL url = cl.getResource(className);

			if (url == null) {
				throw new ScriptNotFoundException("Script " + className
						+ " not found");
			}

			InputStream is = url.openStream();

			groovyClass = groovyClassLoader.parseClass(is);
			log.info("Loaded groovy class " + groovyClass);
			return (GroovyObject) groovyClass.newInstance();

		} 
		catch (CompilationFailedException ex) {
			throw new GroovyCompilationException("Error parsing " + className,
					ex);
		} 
		catch (IOException ex) {
			throw new ScriptNotFoundException("Error reading " + className, ex);
		} 
		catch (IllegalAccessException ex) {
			throw new BeanCreationException(
					"Error instantiating" + groovyClass, ex);
		} 
		catch (InstantiationException ex) {
			throw new BeanCreationException(
					"Error instantiating" + groovyClass, ex);
		}
		finally {
			lastReloadTime = System.currentTimeMillis();
		}
	}
	
	public boolean isChanged() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		URL url = cl.getResource(className);
		if (url == null) {
			log.error("Can't find file '" + className + "'");
			return true;
		} 
		else {
			log.info("Checking timestamp of file '" + url.getFile() + "'");
			File f = new File(url.getFile());
			return f.lastModified() > lastReloadTime;
		}
	}
	
	public long getLastReloadTime() {
		return lastReloadTime;
	}

	public String toString() {
		return "Groovy script: resource='" + className + "'";
	}

}