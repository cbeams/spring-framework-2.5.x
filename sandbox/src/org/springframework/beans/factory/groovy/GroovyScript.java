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

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.script.AbstractScript;
import org.springframework.beans.factory.script.ScriptContext;

/**
 * 
 * @author Rod Johnson
 */
public class GroovyScript extends AbstractScript {

	public GroovyScript(String className, ScriptContext context) {
		super(className, context);
		addInterface(GroovyObject.class);
	}

	
	protected Object createObject(InputStream is) throws IOException, BeansException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class groovyClass = null;
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(cl);
	
		try {
			groovyClass = groovyClassLoader.parseClass(is);
			log.info("Loaded groovy class " + groovyClass);
			return (GroovyObject) groovyClass.newInstance();

		} 
		catch (CompilationFailedException ex) {
			throw new GroovyCompilationException("Error parsing " + getLocation(),
					ex);
		} 
		catch (IllegalAccessException ex) {
			throw new BeanCreationException(
					"Error instantiating" + groovyClass, ex);
		} 
		catch (InstantiationException ex) {
			throw new BeanCreationException(
					"Error instantiating" + groovyClass, ex);
		}
	}

}