/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import junit.framework.TestCase;

import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.functions.StringLength;
import org.springframework.rules.predicates.BeanPropertiesExpression;
import org.springframework.rules.predicates.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.EqualTo;
import org.springframework.rules.predicates.GreaterThan;
import org.springframework.rules.predicates.GreaterThanEqualTo;
import org.springframework.rules.predicates.LessThan;
import org.springframework.rules.predicates.LessThanEqualTo;
import org.springframework.rules.predicates.ParameterizedBeanPropertyExpression;
import org.springframework.rules.predicates.ParameterizedBinaryPredicate;
import org.springframework.rules.predicates.Range;
import org.springframework.rules.predicates.Required;
import org.springframework.rules.predicates.StringLengthConstraint;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryFunctionResultConstraint;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;

/**
 * @author Keith Donald
 */
public class RulesTestSuite extends TestCase {

    public void testRelationalPredicates() {
        Number n1 = new Integer(25);
        Number n11 = new Integer(25);
        Number n2 = new Integer(30);
        Number n3 = new Integer(-15);
        Number n4 = new Integer(26);
        BinaryPredicate p = GreaterThan.instance();
        assertTrue(p.test(n2, n1));
        assertFalse(p.test(n3, n2));

        p = GreaterThanEqualTo.instance();
        assertTrue(p.test(n2, n2));
        assertFalse(p.test(n1, n4));
        assertTrue(p.test(n4, n1));

        p = LessThan.instance();
        assertTrue(p.test(n1, n2));
        assertFalse(p.test(n2, n3));

        p = LessThanEqualTo.instance();
        assertTrue(p.test(n2, n2));
        assertFalse(p.test(n4, n1));
        assertTrue(p.test(n1, n4));

        p = EqualTo.instance();
        assertTrue(p.test(n1, n11));
        assertTrue(p.test(n2, n2));
        assertFalse(p.test(n1, n2));
    }

    public void testParameterizedBinaryPredicate() {
        Integer number = new Integer(25);
        ParameterizedBinaryPredicate p =
            new ParameterizedBinaryPredicate(GreaterThan.instance(), number);
        assertTrue(p.test(new Integer(26)));
        assertFalse(p.test(new Integer(24)));
    }

    public void testFunctionResultConstraint() {
        String s = "12345";
        UnaryFunction lengther = StringLength.instance();
        UnaryFunctionResultConstraint p =
            new UnaryFunctionResultConstraint(
                lengther,
                Constraints.bind(EqualTo.instance(), new Integer(s.length())));
        assertTrue(p.test(s));
        assertFalse(p.test("1234567"));
    }

    public void testRequired() {
        UnaryPredicate req = Required.instance();
        assertFalse(req.test(""));
        assertFalse(req.test(null));
        assertTrue(req.test(new Integer(25)));
        assertTrue(req.test("25"));
    }

    public void testMaxLengthConstraint() {
        UnaryPredicate p = new StringLengthConstraint(5);
        assertTrue(p.test(null));
        assertTrue(p.test(new Integer(12345)));
        assertFalse(p.test(new Integer(123456)));
        assertTrue(p.test("12345"));
        assertFalse(p.test("123456"));
    }

    public void testMinLengthConstraint() {
        UnaryPredicate p =
            new StringLengthConstraint(
                RelationalOperator.GREATER_THAN_EQUAL_TO,
                5);
        assertFalse(p.test(null));
        assertTrue(p.test(new Integer(12345)));
        assertFalse(p.test(new Integer(1234)));
        assertTrue(p.test("1234567890"));
        assertFalse(p.test("1234"));
    }

    public void testRangeConstraint() {
        UnaryPredicate p = new Range(new Integer(0), new Integer(10));
        assertTrue(p.test(new Integer(0)));
        assertTrue(p.test(new Integer(10)));
        assertFalse(p.test(new Integer(-1)));
        assertFalse(p.test(new Integer(11)));
    }

    public void testAnd() {
        UnaryAnd and = new UnaryAnd();
        and.add(Required.instance());
        and.add(new StringLengthConstraint(5));
        assertTrue(and.test("12345"));
        assertFalse(and.test("123456"));
        assertFalse(and.test(""));
    }

    public void testOr() {
        UnaryOr or = new UnaryOr();
        or.add(Required.instance());
        or.add(new StringLengthConstraint(5));
        assertTrue(or.test("12345"));
        assertTrue(or.test("123456"));
        assertFalse(or.test("           "));
    }

    public void testNot() {
        Number n = new Integer("25");
        UnaryPredicate p = Constraints.bind(EqualTo.instance(), n);
        UnaryNot not = new UnaryNot(p);
        assertTrue(not.test(new Integer(24)));
        assertFalse(not.test(new Integer("25")));
    }

    public void testGetProperty() {
        UnaryFunction fn = new GetProperty(new TestBean());
        assertEquals("testValue", fn.evaluate("test"));
    }

    public class TestBean {
        public String getTest() {
            return "testValue";
        }

        public String getTest2() {
            return "test2Value";
        }

        public String getConfirmTest() {
            return "testValue";
        }

        public int getMax() {
            return 25;
        }

        public int getNumber() {
            return 15;
        }

        public int getMin() {
            return 10;
        }
    }

    public void testBeanPropertyValueConstraint() {
        UnaryAnd p = Constraints.conjunction();
        p.add(Constraints.required());
        p.add(Constraints.maxLength(9));
        System.out.println(p);
        BeanPropertyExpression e = new BeanPropertyValueConstraint("test", p);
        assertTrue(e.test(new TestBean()));

        p = Constraints.conjunction();
        e = new BeanPropertyValueConstraint("test", p);
        p.add(Constraints.required());
        p.add(Constraints.maxLength(3));
        assertFalse(e.test(new TestBean()));
    }

    public void testBeanPropertiesExpression() {
        BeanPropertiesExpression p =
            new BeanPropertiesExpression(
                "test",
                EqualTo.instance(),
                "confirmTest");
        assertTrue(p.test(new TestBean()));
        p = new BeanPropertiesExpression("test", EqualTo.instance(), "min");
        assertFalse(p.test(new TestBean()));
    }

    public void testParameterizedBeanPropertyExpression() {
        ParameterizedBeanPropertyExpression p =
            new ParameterizedBeanPropertyExpression(
                "test",
                EqualTo.instance(),
                "testValue");
        assertTrue(p.test(new TestBean()));

        p =
            new ParameterizedBeanPropertyExpression(
                "test",
                EqualTo.instance(),
                "test2Value");
        assertFalse(p.test(new TestBean()));
    }

    public void testNoRules() {
        Rules r = Rules.createRules(TestBean.class);
        assertTrue(r.test(new TestBean()));
    }

    public void testMinMaxRules() {
        Rules r = Rules.createRules(TestBean.class);
        r.add(Constraints.inRangeProperties("number", "min", "max"));
        assertTrue(r.test(new TestBean()));
    }

    public void testBasicCompoundRules() {
        Rules r = Rules.createRules(TestBean.class);
        r.add(Constraints.inRangeProperties("number", "min", "max")).add(
            Constraints.eqProperty("test", "confirmTest"));
        assertTrue(r.test(new TestBean()));
        r.add("test2", Constraints.maxLength(4));
        assertFalse(r.test(new TestBean()));
    }

}
