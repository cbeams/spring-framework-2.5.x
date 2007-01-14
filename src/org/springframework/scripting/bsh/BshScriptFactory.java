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

package org.springframework.scripting.bsh;

import java.io.IOException;

import bsh.EvalError;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link org.springframework.scripting.ScriptFactory} implementation
 * for a BeanShell script.
 *
 * <p>Typically used in combination with a
 * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor};
 * see the latter's
 * {@link org.springframework.scripting.support.ScriptFactoryPostProcessor javadoc}
 * for a configuration example.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see BshScriptUtils
 * @see org.springframework.scripting.support.ScriptFactoryPostProcessor
 */
public class BshScriptFactory implements ScriptFactory, BeanClassLoaderAware {

	private final String scriptSourceLocator;

	private final Class[] scriptInterfaces;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * Create a new BshScriptFactory for the given script source.
	 * <p>With this <code>BshScriptFactory</code> variant, the script needs to
	 * declare a full class or return an actual instance of the scripted object.
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 */
	public BshScriptFactory(String scriptSourceLocator) {
		this(scriptSourceLocator, null);
	}

	/**
	 * Create a new BshScriptFactory for the given script source.
	 * <p>The script may either be a simple script that needs a corresponding proxy
	 * generated (implementing the specified interfaces), or declare a full class
	 * or return an actual instance of the scripted object (in which case the
	 * specified interfaces, if any, need to be implemented by that class/instance).
	 * @param scriptSourceLocator a locator that points to the source of the script.
	 * Interpreted by the post-processor that actually creates the script.
	 * @param scriptInterfaces the Java interfaces that the scripted object
	 * is supposed to implement (may be <code>null</code>)
	 */
	public BshScriptFactory(String scriptSourceLocator, Class[] scriptInterfaces) {
		Assert.hasText(scriptSourceLocator, "'scriptSourceLocator' must not be empty");
		this.scriptSourceLocator = scriptSourceLocator;
		this.scriptInterfaces = scriptInterfaces;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}


	public String getScriptSourceLocator() {
		return this.scriptSourceLocator;
	}

	public Class[] getScriptInterfaces() {
		return this.scriptInterfaces;
	}

	/**
	 * BeanShell scripts do require a config interface.
	 * @return <code>true</code> always
	 */
	public boolean requiresConfigInterface() {
		return true;
	}

	/**
	 * Load and parse the BeanShell script via {@link BshScriptUtils}.
	 * @see BshScriptUtils#createBshObject(String, Class[], ClassLoader)
	 */
	public Object getScriptedObject(ScriptSource actualScriptSource, Class[] actualInterfaces)
			throws IOException, ScriptCompilationException {
		try {
			return BshScriptUtils.createBshObject(
					actualScriptSource.getScriptAsString(), actualInterfaces, this.beanClassLoader);
		}
		catch (EvalError ex) {
			throw new ScriptCompilationException("Could not compile BeanShell script: " + actualScriptSource, ex);
		}
	}

}
