package org.springframework.web.context.support;

import java.io.FileNotFoundException;

import org.springframework.context.support.ContextRefreshedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.MockLog4jAppender;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class PerformanceMonitorListenerTestSuite extends TestCase {

	/**
	 * Constructor for PerformanceMonitorListenerTestSuite.
	 * @param arg0
	 */
	public PerformanceMonitorListenerTestSuite(String name) {
		super(name);
	}

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
		
		Log4jConfigurer.initLogging(
			"test/org/springframework/web/context/support/testlog4jwarn.properties");		
		l.onApplicationEvent(evt);		
		assertEquals(0, MockLog4jAppender.loggingStrings.size());
		
		Log4jConfigurer.initLogging(
			"test/org/springframework/web/context/support/testlog4jdebug.properties");		
		l.onApplicationEvent(evt);		
		assertEquals(1, MockLog4jAppender.loggingStrings.size());

		
	}

}
