
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

package org.springframework.context;

/**
 * Add as a bean. Should also listen.
 */
public class BeanThatListens implements ApplicationListener {

	private int events;
	
	/**
	 * Constructor for BeanThatListeners.
	 */
	public BeanThatListens() {
		super();
	}
	
	public void zero() {
		events = 0;
	}

	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	public void onApplicationEvent(ApplicationEvent e) {
		++events;
		//System.out.println("Bean that listens heard event");
	}
	
	public int getEventCount() {
		return events;
	}

}
