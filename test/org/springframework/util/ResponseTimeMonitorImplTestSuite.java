/*
 * Copyright (c) 2003 JTeam B.V.
 * www.jteam.nl
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JTeam B.V. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with JTeam.
 */
package org.springframework.util;

import java.util.Date;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class ResponseTimeMonitorImplTestSuite extends TestCase {

	/**
	 * Constructor for ResponseTimeMonitorImplTestSuite.
	 * @param arg0
	 */
	public ResponseTimeMonitorImplTestSuite(String name) {
		super(name);
	}

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
		long upTime = impl.getUptime();
		// well, we can probably assert on the a delta of 300 ms otherwise
		// the time is useless ;-)
		assertEquals(upTime, System.currentTimeMillis() - begin, 300);
		
		// well, that's about it!
	}

	public void testGetLoadDate() {
		Date now = new Date();
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();		
		Date d = impl.getLoadDate();
		assertEquals(now.getTime(), d.getTime(), 300);
		
		// ok, done!
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
		
		// ok done!
		
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

	public void testRecordResponseTime() {
		// well, this should have been tested enough you know!
	}

	public void testToString() {
		ResponseTimeMonitorImpl impl = new ResponseTimeMonitorImpl();
		assertEquals(impl.getAverageResponseTimeMillis(), 0);

		impl.recordResponseTime(100);
		impl.recordResponseTime(100);
		impl.recordResponseTime(100);		
		impl.recordResponseTime(300);
		impl.recordResponseTime(200);		
		impl.recordResponseTime(60);
		impl.recordResponseTime(500);
		
		assertEquals(impl.toString(), 
			"hits=" + impl.getAccessCount() + 
			"; avg=" + impl.getAverageResponseTimeMillis() +
			"; best=" + impl.getBestResponseTimeMillis() +
			"; worst=" + impl.getWorstResponseTimeMillis());
	}

}
