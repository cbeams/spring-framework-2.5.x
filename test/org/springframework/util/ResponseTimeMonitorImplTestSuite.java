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

package org.springframework.util;

import java.util.Date;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class ResponseTimeMonitorImplTestSuite extends TestCase {

	public void testGetAccessCount() {
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		assertEquals(impl.getAverageResponseTimeMillis(), 0);

		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		
		impl.recordResponseTime(300);
		impl.recordResponseTime(200);		
		
		assertEquals(impl.getAccessCount(), 5);
	}

	public void testGetUptime() {
		long begin = System.currentTimeMillis();
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		long upTime = impl.getUptimeMillis();
		// Well, we can probably assert on the a delta of 300 ms otherwise
		// the time is useless ;-)
		assertEquals(upTime, System.currentTimeMillis() - begin, 300);
	}

	public void testGetLoadDate() {
		Date now = new Date();
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();		
		Date d = impl.getLoadDate();
		assertEquals(now.getTime(), d.getTime(), 300);
	}

	public void testGetAverageResponseTimeMillis() {
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		assertEquals(impl.getAverageResponseTimeMillis(), 0);

		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		impl.recordResponseTime(100);		
		assertEquals(impl.getAverageResponseTimeMillis(), 100);
		
		impl.recordResponseTime(300);
		impl.recordResponseTime(200);		
		assertEquals(impl.getAverageResponseTimeMillis(), 160);
	}

	public void testGetBestResponseTimeMillis() {
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		assertEquals(impl.getAverageResponseTimeMillis(), 0);

		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		impl.recordResponseTime(100);		
		assertEquals(impl.getAverageResponseTimeMillis(), 100);

		impl.recordResponseTime(300);
		impl.recordResponseTime(200);		
		impl.recordResponseTime(60);
		impl.recordResponseTime(500);
		assertEquals(impl.getBestResponseTimeMillis(), 60);		
	}

	public void testGetWorstResponseTimeMillis() {
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		assertEquals(impl.getAverageResponseTimeMillis(), 0);

		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		impl.recordResponseTime(100);		
		assertEquals(impl.getAverageResponseTimeMillis(), 100);

		impl.recordResponseTime(300);
		impl.recordResponseTime(200);		
		impl.recordResponseTime(60);
		impl.recordResponseTime(500);
		assertEquals(impl.getWorstResponseTimeMillis(), 500);		
	}

}
