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

public class ToStringBuilderTests extends TestCase {
    private SomeObject s1, s2, s3;

    public void testDefaultStyleMap() {
        final Map map = getMap();
        Object stringy = new Object() {
            public String toString() {
                return new ToStringBuilder(this)
                    .append("familyFavoriteSport", map)
                    .toString();
            }
        };
        System.out.println(stringy.toString());
        assertEquals(
            "[ToStringBuilderTests.1@"
                + ObjectUtils.getIdentity(stringy)
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
        String str = new ToStringBuilder(array).toString();
        System.out.println(str);
        assertEquals(
            "[@"
                + ObjectUtils.getIdentity(array)
                + " array<ToStringBuilderTests.SomeObject>[A, B, C]]",
            str);
    }

    public void testPrimitiveArrays() {
        int[] integers = new int[] { 0, 1, 2, 3, 4 };
        String str = new ToStringBuilder(integers).toString();
        System.out.println(str);
        assertEquals(
            "[@"
                + ObjectUtils.getIdentity(integers)
                + " array<Object>[0, 1, 2, 3, 4]]",
            str);
    }

    public void testList() {
        List list = new ArrayList();
        list.add(s1);
        list.add(s2);
        list.add(s3);
        String str =
            new ToStringBuilder(this).append("myLetters", list).toString();
        System.out.println(str);
        assertEquals(
            "[ToStringBuilderTests@"
                + ObjectUtils.getIdentity(this)
                + " myLetters = list[A, B, C]]",
            str);
    }

    public void testSet() {
        Set set = new LinkedHashSet();
        set.add(s1);
        set.add(s2);
        set.add(s3);
        String str =
            new ToStringBuilder(this).append("myLetters", set).toString();
        System.out.println(str);
        assertEquals(
            "[ToStringBuilderTests@"
                + ObjectUtils.getIdentity(this)
                + " myLetters = set[A, B, C]]",
            str);
    }

    public void testClass() {
        String str =
            new ToStringBuilder(this)
                .append("myClass", this.getClass())
                .toString();
        System.out.println(str);
        assertEquals(
            "[ToStringBuilderTests@"
                + ObjectUtils.getIdentity(this)
                + " myClass = ToStringBuilderTests]",
            str);
    }

    public void testMethod() throws Exception {
        String str =
            new ToStringBuilder(this)
                .append(
                    "myMethod",
                    this.getClass().getMethod("testMethod", null))
                .toString();
        System.out.println(str);
        assertEquals(
            "[ToStringBuilderTests@"
                + ObjectUtils.getIdentity(this)
                + " myMethod = testMethod@ToStringBuilderTests]",
            str);
    }

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

}
