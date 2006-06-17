package org.springframework.test.aj;

import junit.framework.TestCase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.ClassUtils;
import org.springframework.test.CallMonitor;

public class AbstractAdvicedTests extends TestCase {
    protected void setUp() throws Exception {
        CallMonitor.resetAll();
    }

    public void testIncrementOnSelf() {
        assertEquals(1, CallMonitor.getCounter("testIncrementOnSelf"));
    }

    public void testCreateJdbcTemplate() {
        assertEquals(0, CallMonitor.getCounter("new JdbcTemplate"));
        new JdbcTemplate(new DriverManagerDataSource());
        assertEquals(0, CallMonitor.getCounter("new JdbcTemplate"));
        new JdbcTemplate();
        assertEquals(1, CallMonitor.getCounter("new JdbcTemplate"));
    }

    public void testCallClassUtilsGetDefaultClassLoader() {
        assertEquals(0, CallMonitor.getCounter("ClassUtils.getDefaultClassLoader"));
        ClassUtils.getDefaultClassLoader();
        assertEquals(1, CallMonitor.getCounter("ClassUtils.getDefaultClassLoader"));
    }
}
