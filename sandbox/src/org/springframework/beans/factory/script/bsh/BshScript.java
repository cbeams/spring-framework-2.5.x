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

package org.springframework.beans.factory.script.bsh;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.script.AbstractScript;
import org.springframework.beans.factory.script.CompilationException;
import org.springframework.beans.factory.script.ScriptContext;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 
 * 
 * @author Rod Johnson
 */
public class BshScript extends AbstractScript {


	public BshScript(String location, ScriptContext ctx) {
		super(location, ctx); 
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#createObject()
	 */
	public Object createObject(InputStream is) throws IOException, BeansException {
		try {
			Interpreter bsh = new Interpreter();
			
			bsh.eval(new InputStreamReader(is));
			
			if (getInterfaces().length != 1) {
				throw new BeanDefinitionStoreException("bsh script must implement exactly one interface");
			}
			
			// Get a reference to the script object (implementing the interface)
			return bsh.eval("return (" + getInterfaces()[0].getName() + ")this");
		} 
		catch (EvalError ex) {
			throw new CompilationException("bsh error", ex);
		}

	}

}