/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.enums;

import junit.framework.TestCase;

import org.springframework.enums.support.StaticCodedEnumResolver;

/**
 * @author Rod Johnson
 */
public class CodedEnumTests extends TestCase {

    public static class Other extends ShortCodedEnum {
        public static Other THING1 = new Other(1, "Thing1");

        public static Other THING2 = new Other(2, "Thing2");

        public Other(int code, String name) {
            super(code, name);
        }
    }

    public static class Dog extends ShortCodedEnum {
        public static final Dog GOLDEN_RETRIEVER = new Dog(11, "Golden Retriever");

        public static final Dog BORDER_COLLIE = new Dog(13, "Border Collie");

        public static final Dog WHIPPET = new Dog(14, "Whippet");

        // Ignore this
        public static final Other THING1 = Other.THING1;

        private Dog(int code, String name) {
            super(code, name);
        }

    }

    public void testForCodeFound() {
        Dog golden = (Dog)StaticCodedEnumResolver.instance().getEnum(Dog.class, new Short((short)11));
        Dog borderCollie = (Dog)StaticCodedEnumResolver.instance().getEnum(Dog.class, new Short((short)13));
        assertSame(golden, Dog.GOLDEN_RETRIEVER);
        assertSame(borderCollie, Dog.BORDER_COLLIE);
    }

    public void testDoesNotMatchWrongClass() {
        Dog none = (Dog)StaticCodedEnumResolver.instance().getEnum(Dog.class, new Short((short)1));
        assertEquals(null, none);
    }

    public void testCanOnlyLookupEnumerations() {
        try {
            StaticCodedEnumResolver.instance().getEnum(String.class, new Short((short)11));
            fail("Not an enumeration");
        }
        catch (IllegalArgumentException ex) {
            // Ok
        }
    }

    public void testEquals() {
        assertEquals("Code equality means equals", Dog.GOLDEN_RETRIEVER, new Dog(11, "Golden"));
        assertFalse("Code inequality means notEquals", Dog.GOLDEN_RETRIEVER.equals(new Dog(12, "Golden")));
    }
}