/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.rules.factory.Constraints;
import org.springframework.rules.functions.GetProperty;
import org.springframework.rules.functions.StringLength;
import org.springframework.rules.predicates.EqualTo;
import org.springframework.rules.predicates.GreaterThan;
import org.springframework.rules.predicates.GreaterThanEqualTo;
import org.springframework.rules.predicates.LessThan;
import org.springframework.rules.predicates.LessThanEqualTo;
import org.springframework.rules.predicates.ParameterizedBinaryPredicate;
import org.springframework.rules.predicates.Range;
import org.springframework.rules.predicates.Required;
import org.springframework.rules.predicates.StringLengthConstraint;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryFunctionResultConstraint;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.beans.ParameterizedBeanPropertyExpression;

/**
 * @author Keith Donald
 */
public class RulesTestSuite extends TestCase {

    private static final Constraints constraints = Constraints.instance();

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
        ParameterizedBinaryPredicate p = new ParameterizedBinaryPredicate(
                GreaterThan.instance(), number);
        assertTrue(p.test(new Integer(26)));
        assertFalse(p.test(new Integer(24)));
    }

    public void testFunctionResultConstraint() {
        String s = "12345";
        UnaryFunctionResultConstraint p = new UnaryFunctionResultConstraint(
                StringLength.instance(), constraints.bind(EqualTo.instance(),
                        new Integer(s.length())));
        assertTrue(p.test(s));
        assertFalse(p.test("1234567"));
    }

    public void testInGroup() {
        String o1 = "o1";
        String o2 = "o2";
        String o3 = "o3";
        Set group = new HashSet();
        group.add(o1);
        group.add(o2);
        group.add(o3);
        UnaryPredicate p = constraints.inGroup(group);
        assertTrue(p.test("o1"));
        assertTrue(p.test(o1));
        assertFalse(p.test("o4"));
        p = constraints.inGroup(new Object[] { o1, o2, o1, o3 });
        assertTrue(p.test("o1"));
        assertTrue(p.test(o1));
        assertFalse(p.test("o4"));
    }

    public void testLike() {
        String keithDonald = "keith donald";
        String keith = "keith";
        String donald = "donald";
        UnaryPredicate p = constraints.like(keithDonald);
        assertTrue(p.test("keith donald"));
        assertFalse(p.test("Keith Donald"));

        p = constraints.like("%keith donald%");
        assertTrue(p.test("keith donald"));
        assertFalse(p.test("Keith Donald"));

        p = constraints.like("keith%");
        assertTrue(p.test(keithDonald));
        assertTrue(p.test(keith));
        assertFalse(p.test(donald));

        p = constraints.like("%donald");
        assertTrue(p.test(keithDonald));
        assertTrue(p.test(donald));
        assertFalse(p.test(keith));

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
        UnaryPredicate p = new StringLengthConstraint(
                RelationalOperator.GREATER_THAN_EQUAL_TO, 5);
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
        UnaryPredicate p = constraints.bind(EqualTo.instance(), n);
        UnaryNot not = new UnaryNot(p);
        assertTrue(not.test(new Integer(24)));
        assertFalse(not.test(new Integer("25")));
    }

    public void testGetProperty() {
        UnaryFunction getter = new GetProperty(new TestBean());
        assertEquals("testValue", getter.evaluate("test"));
    }

    public class TestBean {
        private String test = "testValue";
        private String confirmTest = "testValue";
        private String test2 = "test2Value";
        private int number = 15;
        private int min = 10;
        private int max = 25;

        public String getTest() {
            return test;
        }

        public String getTest2() {
            return test2;
        }

        public String getConfirmTest() {
            return confirmTest;
        }

        public int getNumber() {
            return number;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }
    }

    public void testBeanPropertyValueConstraint() {
        UnaryAnd p = constraints.conjunction();
        p.add(constraints.required());
        p.add(constraints.maxLength(9));
        BeanPropertyExpression e = new BeanPropertyValueConstraint("test", p);
        assertTrue(e.test(new TestBean()));

        p = constraints.conjunction();
        e = new BeanPropertyValueConstraint("test", p);
        p.add(constraints.required());
        p.add(constraints.maxLength(3));
        assertFalse(e.test(new TestBean()));
    }

    public void testBeanPropertiesExpression() {
        BeanPropertiesExpression p = new BeanPropertiesExpression("test",
                EqualTo.instance(), "confirmTest");
        assertTrue(p.test(new TestBean()));

        p = new BeanPropertiesExpression("test", EqualTo.instance(), "min");
        assertFalse(p.test(new TestBean()));
    }

    public void testParameterizedBeanPropertyExpression() {
        ParameterizedBeanPropertyExpression p = new ParameterizedBeanPropertyExpression(
                "test", EqualTo.instance(), "testValue");
        assertTrue(p.test(new TestBean()));

        p = new ParameterizedBeanPropertyExpression("test", EqualTo.instance(),
                "test2Value");
        assertFalse(p.test(new TestBean()));
    }

    public void testNoRules() {
        Rules r = Rules.createRules(TestBean.class);
        assertTrue(r.test(new TestBean()));
    }

    public void testMinMaxRules() {
        Rules r = Rules.createRules(TestBean.class);
        r.add(constraints.inRangeProperties("number", "min", "max"));
        assertTrue(r.test(new TestBean()));
        TestBean b = new TestBean();
        b.number = -1;
        assertFalse(r.test(b));
    }

    public void testBasicCompoundRules() {
        Rules r = Rules.createRules(TestBean.class);
        r.add(constraints.inRangeProperties("number", "min", "max")).add(
                constraints.eqProperty("test", "confirmTest"));
        assertTrue(r.test(new TestBean()));

        r.add("test2", constraints.maxLength(4));
        assertFalse(r.test(new TestBean()));
    }

    public void testCompoundRules() {
        Rules r = Rules.createRules(TestBean.class);
        // test must be required, and have a length in range 3 to 25
        // or test must just equal confirmTest
        UnaryPredicate rules = constraints.or(constraints.all("test",
                new UnaryPredicate[] { constraints.required(),
                        constraints.maxLength(25),
                        constraints.minLength(3) }), constraints.eqProperty(
                "test", "confirmTest"));
        r.add(rules);
        assertTrue(r.test(new TestBean()));
        TestBean b = new TestBean();
        b.test = "a";
        b.confirmTest = "a";
        assertTrue(r.test(b));

        b.test = null;
        b.confirmTest = null;
        assertTrue(r.test(b));

        b.test = "hi";
        assertFalse(r.test(b));
    }

    public void testDefaultRulesSource() {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(
                "org/springframework/rules/rules-context.xml");
        RulesSource rulesSource = (RulesSource)ac.getBean("rulesSource");
        Rules rules = rulesSource.getRules(Person.class);
        assertTrue(rules != null);
        Person p = new Person();
        assertFalse(rules.test(p));
        p.setFirstName("Keith");
        p.setLastName("Donald");
        assertTrue(rules.test(p));
        p.setLastName("Keith");
        assertFalse(rules.test(p));
    }

}