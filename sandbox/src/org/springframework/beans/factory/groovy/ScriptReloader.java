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

package org.springframework.beans.factory.groovy;

import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object to run in the background polling for file
 * modifications. Works only if we can get to a File--
 * probably won't work in a Jar.
 * @author Rod Johnson
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14
 *          16:20:14 trisberg Exp $
 */
public class ScriptReloader extends TimerTask {

	// TODO need way of shutting down--put in interceptor, with DisposableBean
	// impl?
	private Timer timer;

	private Log log = LogFactory.getLog(getClass());

	private int secs;

	private DynamicScript script;

	public ScriptReloader(DynamicScript script, int secs) {
		// TODO check if it's reloaded
		this.script = script;
		this.secs = secs;
		timer = new Timer(true);
		timer.schedule(this, secs * 1000, secs * 1000);
	}

	/**
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		log.info("------- Polling script");
		boolean dirty = true;
		//try {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		URL url = cl.getResource(script.getClassName());
		if (url == null) {
			log.error("Can't find file '" + script.getClassName() + "'");
		} 
		else {
			log.info("Checking timestamp of file '" + url.getFile() + "'");
			File f = new File(url.getFile());
			dirty = f.lastModified() > script.getLastReloadMillis();
		}

		if (dirty) {
			script.reload();
		} 
		else {
			log.info("File '" + script.getClassName() + "' unchanged");
		}
	}

}