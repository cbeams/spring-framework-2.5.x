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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.dynamic.ExpirableObject;


/**
 * Interface to be implemented by threadsafe objects that can create
 * multiple object instances based on a single script location,
 * such as a beanshell or Groovy file location.
 * @author Rod Johnson
 */
public interface Script extends ExpirableObject {
	
	/**
	 * Resource as a String specifying resource location.
	 * @return
	 */
	String getResourceString(); 
	
	/**
	 * Create an instance of the object represented by the script.
	 * Should use the latest version of the script resource,
	 * which may have changed.
	 * @return
	 * @throws BeansException
	 */
	Object createObject() throws BeansException;
	
	/** 
	 * Return the interfaces implemented by the script.
	 * May return the empty interface; never returns null.
	 */
	Class[] getInterfaces();
	
	/**
	 * Add an interface that the script will implement.
	 * @param intf
	 */
	void addInterface(Class intf);

}
