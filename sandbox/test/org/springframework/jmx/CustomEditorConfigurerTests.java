package org.springframework.jmx;

import javax.management.ObjectName;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * @author robh
 */
public class CustomEditorConfigurerTests extends AbstractJmxTests {
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy/MMM/dd");

    public CustomEditorConfigurerTests(String name) {
        super(name);
    }

    protected String getApplicationContextPath() {
        return "org/springframework/jmx/customConfigurer.xml";
    }

    public void testDatesInJmx() throws Exception {
        ObjectName oname = new ObjectName("bean:name=dateRange");

        Date startJmx = (Date)server.getAttribute(oname, "startDate");
        Date endJmx = (Date)server.getAttribute(oname, "endDate");

        assertEquals("startDate ", getStartDate(), startJmx);
        assertEquals("endDate ", getEndDate(), endJmx);
    }

    public void testGetDates() throws Exception {
        DateRange dr = (DateRange) getContext().getBean("dateRange");

        assertEquals("startDate ", getStartDate(), dr.getStartDate());
        assertEquals("endDate ", getEndDate(), dr.getEndDate());
    }

    private Date getStartDate() throws ParseException {
        Date start = df.parse("2004/Oct/12");
        return start;
    }

    private Date getEndDate() throws ParseException {
        Date end = df.parse("2004/Nov/13");
        return end;
    }
}

