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

import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.dynamic.AbstractPoller;
import org.springframework.beans.factory.dynamic.DynamicObjectInterceptor;

/**
 * Introduction interceptor that provides DynamicScript implementation for all
 * Groovy objects. <br>
 * This class also handles reloads through implementing the BeanFactoryAware and
 * BeanNameAware interfaces, which will cause it to receive callbacks by the
 * BeanFactory. <br>
 * It also kicks of a background poller (ScriptReloader) if the
 * pollIntervalSeconds constructor argument is positive.
 * 
 * @author Rod Johnson
 * @version $Id: DynamicScriptInterceptor.java,v 1.1 2004/07/31 08:54:13
 *          johnsonr Exp $
 */
public class DynamicScriptInterceptor extends DynamicObjectInterceptor implements DynamicScript {

	private Script script;

	private ScriptPoller reloader;

	public DynamicScriptInterceptor(Script script,
			HotSwappableTargetSource targetSource, int pollIntervalSeconds) {
		super(targetSource, pollIntervalSeconds);
		this.script = script;
	}

	public DynamicScriptInterceptor(Script script,
			Object object, int pollIntervalSeconds) {
		super(object, pollIntervalSeconds);
		this.script = script;
	}

	
	public String getResourceString() {
		return script.getResourceString();
	}
	
	public Object createObject() throws BeansException {
		return script.createObject();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObjectInterceptor#createPoller()
	 */
	protected AbstractPoller createPoller() {
		return new ScriptPoller(this);
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#isChanged()
	 */
	public boolean isChanged() {
		return script.isChanged();
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#getLastReloadTime()
	 */
	public long getLastReloadTime() {
		return script.getLastReloadTime();
	}
	
	public Class[] getInterfaces() {
		return script.getInterfaces(); 
	}
	
	public void addInterface(Class intf) {
		throw new UnsupportedOperationException("Not supported on constructed scripts");
	}
}