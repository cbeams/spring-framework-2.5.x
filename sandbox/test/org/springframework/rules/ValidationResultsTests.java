/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * @author Keith Donald
 */
public class ValidationResultsTests extends TestCase {
    static ClassPathXmlApplicationContext ac;
    static RulesSource rulesSource;
    static Rules rules;
    private static final Constraints constraints = Constraints.instance();

    static {
        ac = new ClassPathXmlApplicationContext(
                "org/springframework/rules/rules-context.xml");
        rulesSource = (RulesSource)ac.getBean("rulesSource");
        rules = rulesSource.getRules(Person.class);
    }

    public void testValidationResultsPropertyConstraint() {
        Person p = new Person();
        
        ValidationResults r = new ValidationResults(p, new BindException(p,
                "Keith"));
        Errors e = r.collectResults(rules.getRules("lastName"));
        assertEquals(1, e.getErrorCount());
        
        p.setLastName("Donald");
        r = new ValidationResults(p, new BindException(p, "Keith"));
        e = r.collectResults(rules.getRules("lastName"));
        assertEquals(0, e.getErrorCount());
    }
    
    public void testNestedValidationResultsPropertyConstraint() {
        Person p = new Person();
        
        ValidationResults r = new ValidationResults(p, new BindException(p,
                "Keith"));
        Rules rules = Rules.createRules(Person.class);
        UnaryPredicate constraint = constraints.or(
                constraints.all("firstName",
                        new UnaryPredicate[] {
                            constraints.required(),
                            constraints.maxLength(2) }
                        ),
                constraints.not(
                        constraints.eqProperty("firstName", "lastName")));
        rules.add(constraint);
        Errors e = r.collectResults(rules.getRules("firstName"));
        assertEquals(2, e.getErrorCount());
        
    }
}