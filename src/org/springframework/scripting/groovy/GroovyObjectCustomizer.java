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

import groovy.lang.GroovyObject;

/**
 * Strategy used by GroovyScriptFactory to allow custom
 * a MetaClass to be specified for a Groovy bean,
 * or other customization of the created GroovyObject. This is
 * useful to allow the authoring of DSLs, replacing missing
 * methods etc.
 *  
 * @author Rod Johnson
 * @since 2.0.2
 */
public interface GroovyObjectCustomizer {
	
	/**
	 * Customize the GroovyObject created by
	 * GroovyScriptFactory if required. For example,
	 * this can be used to set a custom metaclass to
	 * handle missing methods.
	 * @param goo GroovyObject created by GroovyScriptFactory 
	 */
	void customize(GroovyObject goo);

}