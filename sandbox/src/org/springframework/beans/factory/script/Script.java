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


/**
 * 
 * @author Rod Johnson
 * @version $Id: Script.java,v 1.1 2004-08-01 15:42:01 johnsonr Exp $
 */
public interface Script {
	
	/**
	 * Resource as a String specifying resource location.
	 * @return
	 */
	String getResourceString(); 
	
	Object createObject() throws BeansException;
	
	boolean isChanged();
	
	long getLastReloadTime(); 

}
