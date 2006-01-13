/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.scripting.groovy;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.BeanUtils;
import org.springframework.scripting.AbstractScript;
import org.springframework.scripting.CompilationException;
import org.springframework.scripting.ScriptSource;

import java.io.InputStream;

/**
 * Implementation of the {@link org.springframework.scripting.Script} interface
 * using the <a href="http://groovy.codehaus.org">Groovy</a> scripting language.
 * <p/>
 * Does <strong>not</strong> require interfaces to specified in the configuration
 * nor does it require a generated configuration interface.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @see org.springframework.scripting.AbstractScriptFactory
 * @see GroovyScriptFactory
 * @since 2.0M2
 */
public class GroovyScript extends AbstractScript {

	/**
	 * Creates a new instance using the supplied {@link ScriptSource}.
	 */
	protected GroovyScript(ScriptSource scriptSource) {
		super(scriptSource);
	}

	/**
	 * Compiles the script resource from the {@link InputStream} and compiles
	 * it into a <code>Class</code> using the {@link GroovyClassLoader}. An instance
	 * of this class is returned.
	 */
	protected Object doCreateObject(InputStream inputStream) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(cl);

		try {
			Class clazz = groovyClassLoader.parseClass(inputStream);
			return BeanUtils.instantiateClass(clazz);
		}
		catch (CompilationFailedException ex) {
			// TODO: proper exception and show location of script
			throw new CompilationException("Unable to compile.", ex);
		}
	}
}
