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

import org.springframework.beans.factory.dynamic.AbstractPoller;

/**
 * Object to run in the background polling for file
 * modifications. Works only if we can get to a File--
 * probably won't work in a Jar.
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14
 *          16:20:14 trisberg Exp $
 */
public class ScriptPoller extends AbstractPoller {

	public ScriptPoller(DynamicScript script) {
		super(script);
	}

	protected boolean isDirty() {
		return ((DynamicScript) getDynamicObject()).isChanged();
	}
}