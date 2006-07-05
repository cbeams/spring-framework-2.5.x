package org.springframework.test.aj;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.springframework.test.aj.AjTestCaseLoader.loadTestCaseClass;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AjTestCaseLoaderTests extends TestCase {


    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(AjTestCaseLoaderTests.class);
        suite.addTestSuite(loadTestCaseClass(AbstractAdvicedTests.class, "org.springframework.test.aj.TestAspect"));

        return suite;
    }

    public void testCallTestIncrementOnSelf() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class advicedTestClass = loadTestCaseClass("org/springframework/test/aj/aop.xml", AbstractAdvicedTests.class);
        Object testCase = advicedTestClass.getDeclaredConstructor(new Class[0]).newInstance();
        ReflectionUtils.invokeMethod("testIncrementOnSelf", advicedTestClass, testCase, new Object[0], new Class[0]);
    }


    public void testListMethodsOnAdvicedTestCaseClass() {
        Class advicedTestClass = loadTestCaseClass("org/springframework/test/aj/aop.xml", AbstractAdvicedTests.class);
        ReflectionUtils.doWithMethods(advicedTestClass, new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                System.out.println(method);
            }
        });
    }


}
