/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author robh
 */
public class Main {

    public static void main(String[] args) {
        MBeanServerFactory.createMBeanServer();
        ApplicationContext ctx = new FileSystemXmlApplicationContext("./sandbox/test/org/springframework/jmx/applicationContext.xml");
        
    }
}
