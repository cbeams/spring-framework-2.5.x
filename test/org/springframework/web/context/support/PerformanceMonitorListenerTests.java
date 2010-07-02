/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.context.support;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Alef Arendsen
 */
public class PerformanceMonitorListenerTests extends TestCase {

	public void testPerformanceMonitorListener() {
		// well, constructing it...
		PerformanceMonitorListener l = new PerformanceMonitorListener();
		// ...done ;-)
	}

	public void testOnApplicationEvent()  throws FileNotFoundException {
		PerformanceMonitorListener listener = new PerformanceMonitorListener();
		listener.onApplicationEvent(new ContextRefreshedEvent(new StaticApplicationContext()));
		assertEquals(0, listener.responseTimeMonitor.getAccessCount());
		
		RequestHandledEvent evt = new ServletRequestHandledEvent(new Object(),
			"http://www.springframework.org", "192.168.1.122", "GET", "servlet", "", "", 200);
		listener.onApplicationEvent(evt);
		assertEquals(1, listener.responseTimeMonitor.getAccessCount());
		assertEquals(200, listener.responseTimeMonitor.getAverageResponseTimeMillis());
		assertEquals(200, listener.responseTimeMonitor.getBestResponseTimeMillis());
		assertEquals(200, listener.responseTimeMonitor.getWorstResponseTimeMillis());
	}

}
