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

package org.springframework.beans.factory.dynamic;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO need just one poller for all scripts
 * 
 * Helper that regularly polls a resource
 * @author Rod Johnson
 */
public abstract class DynamicObjectPoller extends TimerTask {
	
	// TODO need way of shutting down--put in interceptor, with DisposableBean
	// impl?
	private Timer timer;

	protected final Log log = LogFactory.getLog(getClass());

	private int secs;

	private DynamicObject dynamicObject;

	public DynamicObjectPoller(DynamicObject dynamicObject) {
		this.dynamicObject = dynamicObject;
		//this.secs = dynamicObject.getPollIntervalSeconds();
		timer = new Timer(true);
		timer.schedule(this, secs * 1000, secs * 1000);
	}

	public int getPollIntervalSecs() {
		return secs;
	}
	
	public DynamicObject getDynamicObject() {
		return dynamicObject;
	}
	
	/**
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		if (dynamicObject.isModified()) {
			dynamicObject.refresh();
		} 
		else {
			//log.info("File '" + script.getClassName() + "' unchanged");
		}
	}

}
