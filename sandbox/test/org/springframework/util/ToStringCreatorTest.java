/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class ToStringCreatorTest extends TestCase {
    private SomeObject s1, s2, s3;

    protected void setUp() throws Exception {
        s1 = new SomeObject() {
            public String toString() {
                return "A";
            }
        };
        s2 = new SomeObject() {
            public String toString() {
                return "B";
            }
        };
        s3 = new SomeObject() {
            public String toString() {
                return "C";
            }
        };
    }

    public static class SomeObject {

    }

    public void testDefaultStyleMap() {
        final Map map = getMap();
        Object stringy = new Object() {
            public String toString() {
                return new ToStringCreator(this).append("familyFavoriteSport",
                        map).toString();
            }
        };
        System.out.println(stringy.toString());
        assertEquals(
                "[ToStringCreatorTests.4@"
                        + ObjectUtils.getIdentityHexString(stringy)
                        + " familyFavoriteSport = map['Keri' -> 'Softball', 'Scot' -> 'Fishing', 'Keith' -> 'Flag Football']]",
                stringy.toString());
    }

    private Map getMap() {
        Map map = new HashMap();
        map.put("Keith", "Flag Football");
        map.put("Keri", "Softball");
        map.put("Scot", "Fishing");
        return map;
    }

    public void testDefaultStyleArray() {
        SomeObject[] array = new SomeObject[] { s1, s2, s3 };
        String str = new ToStringCreator(array).toString();
        System.out.println(str);
        assertEquals("[@" + ObjectUtils.getIdentityHexString(array)
                + " array<ToStringCreatorTests.SomeObject>[A, B, C]]", str);
    }

    public void testPrimitiveArrays() {
        int[] integers = new int[] { 0, 1, 2, 3, 4 };
        String str = new ToStringCreator(integers).toString();
        System.out.println(str);
        assertEquals("[@" + ObjectUtils.getIdentityHexString(integers)
                + " array<Object>[0, 1, 2, 3, 4]]", str);
    }

    public void testList() {
        List list = new ArrayList();
        list.add(s1);
        list.add(s2);
        list.add(s3);
        String str = new ToStringCreator(this).append("myLetters", list)
                .toString();
        System.out.println(str);
        assertEquals("[ToStringCreatorTests@"
                + ObjectUtils.getIdentityHexString(this)
                + " myLetters = list[A, B, C]]", str);
    }

    public void testSet() {
        Set set = new LinkedHashSet();
        set.add(s1);
        set.add(s2);
        set.add(s3);
        String str = new ToStringCreator(this).append("myLetters", set)
                .toString();
        System.out.println(str);
        assertEquals("[ToStringCreatorTests@"
                + ObjectUtils.getIdentityHexString(this)
                + " myLetters = set[A, B, C]]", str);
    }

    public void testClass() {
        String str = new ToStringCreator(this).append("myClass",
                this.getClass()).toString();
        System.out.println(str);
        assertEquals("[ToStringCreatorTests@"
                + ObjectUtils.getIdentityHexString(this)
                + " myClass = ToStringCreatorTests]", str);
    }

    public void testMethod() throws Exception {
        String str = new ToStringCreator(this).append("myMethod",
                this.getClass().getMethod("testMethod", null)).toString();
        System.out.println(str);
        assertEquals("[ToStringCreatorTests@"
                + ObjectUtils.getIdentityHexString(this)
                + " myMethod = testMethod@ToStringCreatorTests]", str);
    }

    public void testAppendProperties() throws Exception {
        Parent parent = new Parent();
        System.out.println(parent);
        assertEquals("[ToStringCreatorTests.Parent@"
                + ObjectUtils.getIdentityHexString(parent)
                + " child = [null], name = 'parent']", parent.toString());
    }

    public void testAppendPropertiesParentChildBidirectional() throws Exception {
        Parent parent = new Parent();
        Child child = new Child(parent);
        parent.setChild(child);
        System.out.println(parent);
        assertEquals("[ToStringCreatorTests.Parent@"
                + ObjectUtils.getIdentityHexString(parent)
                + " child = 'child', name = 'parent']", parent.toString());
    }

    private static class Parent {
        private String name = "parent";

        private Child child;

        public String getName() {
            return name;
        }

        public Child getChild() {
            return child;
        }
        
        public void setChild(Child child) {
            this.child = child;
        }

        public String toString() {
            return new ToStringCreator(this).appendProperties().toString();
        }

    }

    private static class Child {
        private String name = "child";

        private Parent parent;

        public Child() {
            
        }
        
        public Child(Parent parent) {
            this.parent = parent;
        }

        public String getName() {
            return name;
        }
        
        public Parent getParent() {
            return parent;
        }

        public String toString() {
            return new ToStringCreator(this).appendProperties().toString();
        }
    }
}