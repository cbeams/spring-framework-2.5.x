package org.springframework.web.context.support;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Alef Arendsen
 */
public class PerformanceMonitorListenerTestSuite extends TestCase {

	public void testPerformanceMonitorListener() {
		// well, constructing it...
		PerformanceMonitorListener l = new PerformanceMonitorListener();
		// ...done ;-)
	}

	public void testOnApplicationEvent() 
	throws FileNotFoundException {
		PerformanceMonitorListener l = new PerformanceMonitorListener();
		l.onApplicationEvent(new ContextRefreshedEvent(new StaticApplicationContext()));
		assertEquals(0, l.responseTimeMonitor.getAccessCount());
		
		RequestHandledEvent evt = new RequestHandledEvent(new Object(), 
			"http://www.springframework.org", 200, "192.168.1.122", "GET", "servlet");
		l.onApplicationEvent(evt);
		assertEquals(1, l.responseTimeMonitor.getAccessCount());
		assertEquals(200, l.responseTimeMonitor.getAverageResponseTimeMillis());
		assertEquals(200, l.responseTimeMonitor.getBestResponseTimeMillis());
		assertEquals(200, l.responseTimeMonitor.getWorstResponseTimeMillis());	
	}

}
