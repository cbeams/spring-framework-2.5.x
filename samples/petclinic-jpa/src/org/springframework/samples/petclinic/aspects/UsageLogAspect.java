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

package org.springframework.samples.petclinic.aspects;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Sample AspectJ annotation-style aspect that saves
 * every owner name requested to the clinic.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
@Aspect
public class UsageLogAspect {
	
	private int historySize = 100;
	
	// Of course saving all names is not suitable for
	// production use, but this is a simple sample
	private List<String> namesRequested = new ArrayList<String>(historySize);
	
	public void setHistorySize(int historySize) {
		this.historySize = historySize;
		namesRequested = new ArrayList<String>(historySize);
	}
	
	@Before("execution(* *.findOwners(String)) && args(name)")
	public synchronized void logNameRequest(String name) {
		// Not the most efficient implementation,
		// but we're aiming to illustrate the power of
		// @AspectJ AOP, not write perfect code here :-)
		if (namesRequested.size() > historySize) {
			namesRequested.remove(0);
		}
		namesRequested.add(name);
	}
	
	public List<String> getNamesRequested() {
		return namesRequested;
	}

}
