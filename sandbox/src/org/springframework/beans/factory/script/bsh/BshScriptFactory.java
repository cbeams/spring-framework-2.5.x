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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.script.AbstractScriptFactory;
import org.springframework.beans.factory.script.Script;

/**
 * 
 * @author Rod Johnson
 */
public class BshScriptFactory extends AbstractScriptFactory {

	/**
	 * @see org.springframework.beans.factory.script.AbstractScriptFactory#createScript(java.lang.String)
	 */
	protected Script createScript(String location) throws BeansException {
		//if (geti)
		return new BshScript(location, this);
	}

	/**
	 * @see org.springframework.beans.factory.script.AbstractScriptFactory#requiresConfigInterface()
	 */
	protected boolean requiresConfigInterface() {
		return true;
	}

}
