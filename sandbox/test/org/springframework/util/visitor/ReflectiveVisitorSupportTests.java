package org.springframework.util.visitor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitable;
import org.springframework.util.visitor.Visitor;

/**
 * Tests the various functionality of the ReflectiveVisitorSupport class.
 * 
 * @author R.J. Lorimer
 */
public class ReflectiveVisitorSupportTests extends TestCase {

    private ReflectiveVisitorSupport support;

    public void testDirectDefaultLookup() {
        ensureVisit(new DirectMockVisitor(), new ArrayList());
    }

    public void testSuperclassDefaultLookup() {
        ensureVisit(new SuperclassMockVisitor(), new ArrayList());
    }

    public void testInterfaceDefaultLookup() {
        ensureVisit(new InterfaceMockVisitor(), new ArrayList());
    }
    
    public void testInnerClassArgument() {
        Map.Entry entry = new Map.Entry() {
            public Object getKey() {
                return null;
            }
            public Object getValue() {
                return null;
            }
            public Object setValue(Object value) {
                return null;
            }
        };
        ensureVisit(new DirectMockVisitor(), entry);
    }

    
    //public void testInheritedValueLookup() {
    //    ensureVisit(new InheritedArgMockVisitor(), new ArrayList());
    //}

    public void testVisitableHook() {
        ensureVisit(new CustomVisitor(), new VisitorAwareVisitable());
    }

    public void testNullArgument() {
        ensureVisit(new NullVisitor(), null);
    }

    // Should this pass (if security permits)?
    // possible approach:
    // Method m = ... <find method algorithm>
    // try {
    //   m.setAccessible(false);
    //   // ... invoke
    //   m.setAccessible(true);
    // }
    // catch(IllegalAccessException e) {
    //	 // look for superclass/interface method here?
    //   throw new RuntimeException("Method Visibility is not sufficient.", e);
    // }
    public void testInvisibleClassVisitor() {
        ensureVisit(new InvisibleClassVisitor(), new ArrayList());
    }

    // Should this pass? (see comments above)
    //public void testInvisibleMethodVisitor() {
    //    ensureVisit(new InvisibleMethodVisitor(), new ArrayList());
    //}

    public void testNullVisitor() {
        try {
            support.invokeVisit(null, new Object());
            fail("null visitor was accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior.
        } catch (Exception e) {
            fail("null visitor was not handled properly");
        }
    }

    private void ensureVisit(AbstractMockVisitor visitor, Object argument) {
        support.invokeVisit(visitor, argument);
        assertTrue("Visitor was not visited!", visitor.visited);
    }

    public void setUp() {
        support = new ReflectiveVisitorSupport();
    }

    public abstract class AbstractMockVisitor implements Visitor {
        boolean visited;
    }

    public class DirectMockVisitor extends AbstractMockVisitor {
        public void visitArrayList(ArrayList value) {
            visited = true;
        }
        public void visitMapEntry(Map.Entry entry) {
            visited = true;
        }
    }

    /*
    public class InheritedArgMockVisitor extends AbstractMockVisitor {
        public void visitArrayList(Object value) {
            visited = true;
        }
    }
    */

    public class SuperclassMockVisitor extends AbstractMockVisitor {
        public void visitAbstractList(AbstractList value) {
            visited = true;
        }
    }
    public class InterfaceMockVisitor extends AbstractMockVisitor {
        public void visitList(List value) {
            visited = true;
        }
    }

    public class CustomVisitor extends AbstractMockVisitor {
        public void doVisitMe(VisitorAwareVisitable value) {
            visited = true;
        }
    }

    private class InvisibleClassVisitor extends AbstractMockVisitor {
        public void visitArrayList(ArrayList list) {
            visited = true;
        }
    }

    public class NullVisitor extends AbstractMockVisitor {
        public void visitNull() {
            visited = true;
        }
    }

    /*
    public class InvisibleMethodVisitor extends AbstractMockVisitor {
        private void visitArrayList(ArrayList list) {
            visited = true;
        }
    }
    */
    public class VisitorAwareVisitable implements Visitable {
        public void accept(Visitor visitor) {
            CustomVisitor custom = (CustomVisitor)visitor;
            custom.doVisitMe(this);
        }
    }

}
