/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * @author robh
 */
public class Main {

    public static void main(String[] args) throws Exception {
        MBeanServer server = MBeanServerFactory.createMBeanServer();
        HtmlAdaptorServer adapter = new HtmlAdaptorServer(9000);
       
        server.registerMBean(adapter, ObjectNameManager.getInstance("adapter:type=http"));
        adapter.start();

        ApplicationContext ctx = new FileSystemXmlApplicationContext(
                "./sandbox/test/org/springframework/jmx/applicationContext.xml");
        System.out.println("Running");
        System.in.read();
        adapter.stop();

    }
}