/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.autobuilds.ant;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * AppTestLauncher
 * 
 * Kick off the application tests for a Spring sample application.  This simple
 * launcher class avoids all the tedious mucking about with ANT's optional.jar
 * which must be correctly built with the junit classes, and also match
 * whatever version of ANT happens to be installed by the user of those
 * classes.
 * 
 * Our testing output requirement is limited to the text file offering from 
 * junit.
 * 
 * @author Darren Davison
 * @version $Id: AppTestLauncher.java,v 1.1 2003-12-19 01:44:36 davison Exp $
 */
public class AppTestLauncher extends Task {

	private String application;
	private int port = 80;
	
	/**
	 * supply the target application and target server label as args
	 * 
	 * @param args
	 */
	public void execute() throws BuildException {
    	if (application == null || application.length() < 2)
    		throw new BuildException("Application attributes must be supplied and be a valid sample application name");   		
    	
    	if (port < 1 || port > 65535)
    		throw new BuildException("Port is invalid or missing.  Please supply a valid port number for the http server");
    		
    	System.setProperty("autobuilds.server.port", String.valueOf(port));
    	
    	Class clazz;
    	String className = "org.springframework.apptests." + application + ".AllTests";
    	
        try {
            clazz = Class.forName(className);
                
            TestSuite suite = new TestSuite(clazz);	
			TestRunner.run(clazz);
			
        } catch (ClassNotFoundException e) {
			throw new BuildException("Failed to find target class " + className + ".  Please check it is on the classpath.");
        } 
	}
	
    /**
     * @param string
     */
    public void setApplication(String string) {
        application = string;
    }

    /**
     * @param i
     */
    public void setPort(int i) {
        port = i;
    }

}
