package org.springframework.test;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.HashMap;

/**
 * <p>Convenience class for keeping track of the number of invocations of
 * a method or constructor.
 * This class is ideal for marking invocations in an AspectJ aspect and
 * for reading the number of invocations in a test case.
 * See {@link org.springframework.test.aj.AjTestCaseLoader}.
 *
 * <p>The class provides for public operations:
 *
 * <ul>
 *   <li><code>increment</code>: increase the count of an operation with one
 *   <li><code>getCounter</code>: return the number of increment calls for an operation
 *   <li><code>reset</code>: reset the counter for an operation
 *   <li><code>resetAll</code>: reset all counters
 * </ul>
 *
 * <p>Operations are specified by string names. The code below marks an invocation:
 *
 * <pre>
 * CallMonitor.increment("List.get");
 * </pre>
 *
 * <p>The next statement gets the number of invocations for the <code>List.get</code> operation:
 *
 * <pre>
 * int counter = CallMonitor.getCounter("List.get");
 * </pre>
 *
 * <p>To reset a counter call the <code>reset</code> method with a operation name:
 *
 * <pre>
 * CallMonitor.reset("List.get");
 * </pre>
 *
 * <p>To reset all counters call the <code>resetAll</code> method:
 *
 * <pre>
 * CallMonitor.resetAll();
 * </pre>
 *
 * <p>Internally this class keeps track of invocations via a {@link ThreadLocal} variable. It is advised
 * to call the <code>resetAll</code> method in the <code>setUp</code> method of test case to
 * reset all counters to make sure readings are correct.
 *
 * @author Steven Devijver
 * @see org.springframework.test.aj.AjTestCaseLoader
 */
public class CallMonitor {
    private static ThreadLocal callMap = new ThreadLocal();

    public static final String PREVIOUS_EXTENSION = ".previous";

    private static void initializeCallMap() {
        if (callMap.get() == null) {
            callMap.set(new HashMap());
        }
    }

    private static Map getCallMap() {
        initializeCallMap();
        return (Map)callMap.get();
    }

    private static void setCounterForOperation(int counter, String operation) {
        Assert.hasText(operation, "Operation parameter is required!");
        getCallMap().put(operation, new Integer(counter));
    }

    private static int getCounterForOperation(String operation) {
        Assert.hasText(operation, "Operation parameter is required!");
        if (!getCallMap().containsKey(operation)) {
            return 0;
        } else {
            return ((Integer) getCallMap().get(operation)).intValue();
        }
    }

    public static void increment(String operation) {
        int counter = getCounterForOperation(operation);
        setCounterForOperation(counter, operation + PREVIOUS_EXTENSION);
        counter++;
        setCounterForOperation(counter, operation);
    }

    public static int getCounter(String operation) {
        return getCounterForOperation(operation);
    }

    public static void assertCounter(int counter, String operation) {
        junit.framework.Assert.assertEquals("operation:" + operation, counter, getCounter(operation));
    }

    public static void assertOneExecution(String operation) {
//        junit.framework.Assert.assertEquals("operation [" + operation + "] previously ")
    }

    public static void reset(String operation) {
        getCallMap().remove(operation);
    }

    public static void resetAll() {
        getCallMap().clear();
    }
}
