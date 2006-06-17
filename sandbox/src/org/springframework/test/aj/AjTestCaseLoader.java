package org.springframework.test.aj;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.springframework.instrument.classloading.SimpleInstrumentableClassLoader;
import org.springframework.instrument.classloading.ShadowingClassLoader;
import org.springframework.util.StringUtils;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Class loader manipulation helper class that sets up an isolated
 * environment for AspectJ load-time weaving. This class makes the usage
 * of AspectJ in JUnit tests much more flexible. This class requires
 * Java 1.5 or higher.
 *
 * <p>This class is more flexible that specifying the AspectJ JavaAgent as
 * an argument when starting the VM since a sets up a local load-time weaving
 * environment where required.
 *
 * <p>Specifically, this class targets the use of AspectJ aspects where
 * mock frameworks like EasyMock don't offer functionalities. Specifically,
 * this class allows verification of constructor and static method calls.
 *
 * <p>The <code>loadTestCaseClass</code> methods will weave classes that
 * extend <code>junit.framework.TestCase</code>. Other classes cannot be
 * loaded by these methods.
 *
 * <p>The functionalities provided by the <code>loadClass</code> methods
 * are two-fold:
 * <ul>
 *   <li>Read AjectJ XML load-time weaving configuration files from custom locations
 *   <li>Weave almost all classes via AspectJ load-time weaver
 * </ul>
 *
 * <h3>Read AspectJ XML load-time weaving configuration files from custom locations</h3>
 *
 * <p>The AspectJ load-time weaver is configured via an XML file. Normally this XML file
 * is read from <code>/META-INF/aop.xml</code> from the class path.
 *
 * <p>This class overwrites the default behaviour and allows reading this configuration
 * file from custom locations on the class path. There is a convenience convention provided
 * that loads the <code>aop.xml</code> file from the package of the test case that is loaded.
 *
 * <p>To help you get started with this class consider this AspectJ advice using Java 5 annotations:
 *
 * <pre>
 * package com.mycompany;
 *
 * import org.aspectj.lang.annotation.Aspect;
 * import org.aspectj.lang.annotation.Before;
 *
 * import org.springframework.test.CallMonitor;
 *
 * @Aspect
 * public class MyAspect {
 *    @Before("execution(* org.springframework.util.ClassUtils.getDefaultClassLoader())")
 *    public void increment() {
 *       CallMonitor.increment("ClassUtils.getDefaultClassLoader");
 *    }
 * }
 * </pre>
 *
 * <p>This aspect must be configured in an AspectJ load-time weaving XML configuration file.
 * The file for this example is <code>com/mycompany/aop.xml</code> on the class path shown below:
 *
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 *
 * &lt;aspectj&gt;
 *
 *	&lt;aspects&gt;
 *        &lt;aspect name="com.mycompany.MyAspect"/&gt;
 *    &lt;/aspects&gt;
 *
 * &lt;/aspectj&gt;
 * </pre>
 *
 * <p>See the <code>loadTestCaseClass</code> methods for more details on how to use load-time
 * weaving XML configuration files with this class.
 *
 * <h3>Weave almost all classes via AspectJ load-time weaver</h3>
 *
 * <p>Almost all classes can be woven by AspectJ with this class. Exceptions are:
 *
 * <ul>
 *   <li>All classes in the <code>java.</code> package and its sub packages
 *   <li>All classes in the <code>javax.</code> package and its sub packages
 *   <li>All classes in the <code>sun.</code> package and its sub packages
 *   <li>All classes in the <code>org.xml.sax.</code> package and its sub packages
 *   <li>All classes in the <code>org.w3c.</code> package and its sub packages
 *   <li>All classes in the <code>junit.</code> package and its sub packages
 *   <li>All classes in the <code>org.apache.commons.logging.</code> package and
 * its sub packages
 * </ul>
 *
 * <p>All other classes can be woven at load-time by AspectJ. This class creates isolated
 * <em>throw-away</em> ClassLoaders that allow for easy on-the-fly weaving in any environment
 * (e.g. build script or IDE). The ClassLoader that's used internally will not delegate calls
 * to its parent ClassLoader except for classes in the packages mentioned above.
 *
 * <p>Instead it will reload the TestCase class and all its dependent classes from the class path
 * and have them re-woven by AspectJ on each call to the <code>loadTestCaseClass</code> methods.
 * This behavior goes against the {@link ClassLoader} contract but is perfectly safe within the scope
 * of unit tests.
 *
 * <p>There is however on consequence of this way of working: classes loaded by via the internal ClassLoader
 * are not unique in the ClassLoader hierarchy. It will have been loaded once by the parent
 * ClassLoader or one of its parents and once by the internal ClassLoader. Classes or frameworks that inspect
 * the ClassLoader hierarchy and require specific classes to be unique in this hierarchy may fail and this
 * may seem unexpected. One notable example is the Apache Commons Logging framework which is why its classes
 * have been excluded from load-time weaving by this class.
 *
 * <h3>Using this class in JUnit test cases</h3>
 *
 * <p>To use this class with JUnit test cases the weaving ClassLoader must be used and the resulting TestCase class
 * must be passed to a JUnit <code>junit.framework.TestSuite</code> object. Not that the TestCase class itself may not
 * have been modified by the AspectJ load-time weaver but classes it depends on (either directly or indirectly)
 * probably will have be modified.
 *
 * <p>Below is an example of how to use this class in a JUit test case:
 *
 * <pre>
 * package com.mycompany;
 *
 * import junit.framework.TestCase;
 * import junit.framework.TestSuite;
 *
 * import org.springframework.test.CallMonitor;
 * import static org.springframework.test.aj.AjTestCaseLoader.createSuiteFor;
 *
 * import org.springframework.util.ClassUtils;
 *
 * public class MyTests extends TestCase {
 *    protected void setUp() throws Exception {
 *       CallMonitor.resetAll();
 *    }
 *
 *    public static TestSuite suite() {
 *       return createSuiteFor(MyTests.class);
 *    }
 *
 *    public void testClassUtilsGetDefaultClassLoader() {
 *       assertEquals(0, CallMonitor.getCounter("ClassUtils.getDefaultClassLoader");
 *       ClassUtils.getDefaultClassLoader();
 *       assertEquals(1, CallMonitor.getCounter("ClassUtils.getDefaultClassLoader");
 *    }
 * }
 * </pre>
 *
 * <p>The static <code>suite</code> method as shown above will be recognized by the JUnit framework.
 *
 * <h3>Other usages</h3>
 *
 * <p>This class is striclty meant to be used in unit tests. Therefor the <code>loadTestCaseClass</load> will
 * only weave classes that extend <code>junit.framework.TestCase</code>. While this class could potentionally be
 * used for other purposes outside of testing there is a small but significant issue with thread-safety which
 * prevents this approach to be used in production code.
 *
 * @author Steven Devijver
 * @see org.springframework.test.CallMonitor
 */
public class AjTestCaseLoader {
    private static final String AOP_CONFIG_LOCATION_PROPERTY = "org.aspectj.weaver.loadtime.configuration";

    /**
     * <p>Convenience method that creates a <code>junit.framework.TestSuite</code> object.
     * All class arguments are passed to the <code>loadTestCaseClass</code> method.
     *
     * <p>This method allows you to write a more concise <code>suite</code> method in JUnit test
     * cases:
     *
     * <pre>
     * package com.mycompany;
     *
     * import junit.framework.TestCase;
     * import junit.framework.TestSuite;
     *
     * import org.springframework.test.aj.AjTestCaseLoader;
     *
     * public class MyTestCase extends TestCase {
     *    public static TestSuite suite() {
     *       return AjTestCaseLoader.createSuiteFor(MyTestCase.class);
     *    }
     *
     *    public void testMyTest() {
     *
     *    }
     * }
     * </pre>
     *
     * @param classes TestCases that have to be woven
     * @return <code>junit.framework.TestSuite</code> instance
     */
    public static TestSuite createSuiteFor(Class... classes) {
        TestSuite suite = new TestSuite();
        if (classes != null) {
            for (Class clazz : classes) {
                suite.addTestSuite(loadTestCaseClass(clazz));
            }
        }
        return suite;
    }

    /**
     * <p>This method uses the AspectJ load-time weaver to modify the <code>clazz</code> <code>TestCase</code> class
     * or any of the classes it depends on. It loads the <code>aop.xml</code> file that's located in the same package
     * as the <code>TestCase</code> class and will fail if it cannot be found.
     *
     * @param clazz must be type-compatible with <code>junit.framework.TestCase</code>
     * @return <code>Class</code> woven by the AspectJ load-time weaver
     */
    public static Class loadTestCaseClass(Class clazz) {
        Assert.notNull(clazz, "Class must not be null!");
        String packageName = clazz.getPackage().getName();
        packageName = StringUtils.replace(packageName, ".", "/");
        return loadTestCaseClass(clazz, packageName + "/aop.xml");
    }

    /**
     *
     * @param clazz must be type-compatible with <code>junit.framework.TestCase</code>
     * @param aopConfigLocation
     * @return
     */
    public static Class loadTestCaseClass(Class clazz, String aopConfigLocation) {
        Assert.notNull(clazz, "Class must not be null!");
        Assert.hasLength(aopConfigLocation, "AOP XML configuration file location on classpath must be specified!");
        if (!(TestCase.class.isAssignableFrom(clazz))) {
            throw new RuntimeException("Cannot load class that is not type-compatible with " + TestCase.class.getName());
        }
        return loadClassWithAspectJ(clazz, aopConfigLocation);
    }

    protected static Class loadClassWithAspectJ(Class clazz, String aopConfigLocation) {
        Assert.notNull(clazz, "Class must not be null!");
        Assert.hasLength(aopConfigLocation, "AOP XML configuration file location on classpath must be specified!");

        String originalAopConfigLocation = System.getProperty(AOP_CONFIG_LOCATION_PROPERTY);
        System.setProperty(AOP_CONFIG_LOCATION_PROPERTY, aopConfigLocation);

        ShadowingClassLoader classLoader = new InternalInstrumentableClassLoader(clazz.getClassLoader());
        classLoader.addTransformer(new ClassPreProcessorAgentAdapter());
        try {
            return classLoader.loadClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class [" + clazz.getName() + "]", e);
        } finally {
            if (originalAopConfigLocation != null) {
                System.setProperty(AOP_CONFIG_LOCATION_PROPERTY, originalAopConfigLocation);
            } else {
                System.setProperty(AOP_CONFIG_LOCATION_PROPERTY, "");
            }
        }
    }

    private static class InternalInstrumentableClassLoader
        extends ShadowingClassLoader {
        public InternalInstrumentableClassLoader(ClassLoader enclosingClassLoader) {
            super(enclosingClassLoader);
        }

        protected boolean isClassNameExcludedFromShadowing(String className) {
            return className.startsWith("junit.framework.");
        }
    }
}
