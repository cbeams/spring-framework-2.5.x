/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Test the basic functionality of the ServiceManager
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class TestServiceManager extends TestCase
{

    /**
     * Simple flag for app context initialization
     */
    private static boolean INIT = false;

    /**
     * The shared app context
     */
    private static ApplicationContext CTX;

    /**
     * The spring configuration file.
     */
    private static String CONFIGFILE =
        "sandbox/test/org/springframework/service/test.xml";

    /**
     * Constructor for TestServiceManager.
     * @param name
     */
    public TestServiceManager(String name)
    {
        super(name);
    }

    /**
     * Set the context to look up beans.
     */
    public void setUp()
    {
        if (!INIT)
        {
            INIT = true;
            CTX = new FileSystemXmlApplicationContext(CONFIGFILE);

        }
    }

    /**
     * Test execution of the lifecycle methods that are registered with spring.
     * Make sure they are all singletons.
     * @throws Exception
     */
    public void testServiceManager() throws Exception
    {

        ServiceManager mgr = (ServiceManager) CTX.getBean("serviceManager");

        mgr.initialize();
        mgr.start();
        mgr.stop();
        mgr.dispose();

        FirstService service = (FirstService) CTX.getBean("myFirstService");
        String calledString = BaseService.getCalledString();
        //System.out.println(calledString);
        assertEquals(
            "Calling lifecycle sequence is not correct",
            "1init-2init-1start-2start-2stop-1stop-2dispose-1dispose-",
            calledString);
            
        assertTrue("Services are not all singletons", mgr.validateSingleton());


    }

}
