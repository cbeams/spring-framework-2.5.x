/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.rules.reporting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.rules.UnaryPredicate;
import org.springframework.rules.predicates.CompoundBeanPropertyExpression;
import org.springframework.rules.predicates.StringLengthConstraint;
import org.springframework.rules.predicates.UnaryAnd;
import org.springframework.rules.predicates.UnaryNot;
import org.springframework.rules.predicates.UnaryOr;
import org.springframework.rules.predicates.beans.BeanPropertiesExpression;
import org.springframework.rules.predicates.beans.BeanPropertyValueConstraint;
import org.springframework.rules.predicates.beans.ParameterizedBeanPropertyExpression;
import org.springframework.util.ClassUtils;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * @author Keith Donald
 */
public class PropertyResults {
    protected static final Log logger =
        LogFactory.getLog(PropertyResults.class);
    private String propertyName;
    private Object rejectedValue;
    private UnaryPredicate violatedConstraint;
    private Severity severity = Severity.ERROR;
    private MessageSourceResolvable[] resolvedArgs;

    public PropertyResults(
        String propertyName,
        Object rejectedValue,
        UnaryPredicate violatedConstraint) {
        this.propertyName = propertyName;
        this.rejectedValue = rejectedValue;
        this.violatedConstraint = violatedConstraint;
    }

    /**
     * @return Returns the propertyName.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return Returns the rejectedValue.
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * @return Returns the violatedConstraint.
     */
    public UnaryPredicate getViolatedConstraint() {
        return violatedConstraint;
    }

    public int getViolatedCount() {
        return new SummingVisitor(getViolatedConstraint()).sum();
    }

    /**
     * @return Returns the severity.
     */
    public Severity getSeverity() {
        return severity;
    }

    public String buildMessage(MessageSource source, Locale locale) {
        StringBuffer buf = new StringBuffer(255);
        MessageSourceResolvable[] args = getMessageArguments();
        for (int i = 0; i < args.length - 1; i++) {
            MessageSourceResolvable arg = args[i];
            buf.append(source.getMessage(arg, locale));
            buf.append(' ');
        }
        buf.append(source.getMessage(args[args.length - 1], locale));
        return buf.toString();
    }

    public MessageSourceResolvable[] getMessageArguments() {
        if (resolvedArgs == null) {
            this.resolvedArgs =
                new DefaultArgumentTranslator(this).resolveArguments();
            if (logger.isDebugEnabled()) {
                logger.debug(DefaultObjectStyler.evaluate(resolvedArgs));
            }
        }
        return resolvedArgs;
    }

    public static class DefaultArgumentTranslator implements Visitor {
        private ReflectiveVisitorSupport visitorSupport =
            new ReflectiveVisitorSupport();
        PropertyResults results;
        private List args = new ArrayList();

        public DefaultArgumentTranslator(PropertyResults results) {
            this.results = results;
        }

        public MessageSourceResolvable[] resolveArguments() {
            args.add(resolvableProperty(results.propertyName));
            visitorSupport.invokeVisit(this, results.violatedConstraint);
            return (MessageSourceResolvable[])args.toArray(
                new MessageSourceResolvable[0]);
        }

        void visit(CompoundBeanPropertyExpression rule) {
            visitorSupport.invokeVisit(this, rule.getPredicate());
        }

        void visit(BeanPropertiesExpression e) {
            add(
                getAsProperty(e.getPredicate()),
                new Object[] { resolvableProperty(e.getOtherPropertyName())},
                e.toString());
        }

        void visit(ParameterizedBeanPropertyExpression e) {
            add(
                getAsProperty(e.getPredicate()),
                new Object[] { e.getParameter()},
                e.toString());
        }

        private MessageSourceResolvable resolvableProperty(String propertyName) {
            return new DefaultMessageSourceResolvable(
                new String[] { propertyName },
                null,
                propertyName);
        }

        private void add(String code, Object[] args, String defaultMessage) {
            this.args.add(
                new DefaultMessageSourceResolvable(
                    new String[] { code },
                    args,
                    defaultMessage));
        }

        private String getAsProperty(Object o) {
            return ClassUtils.getShortNameAsProperty(o.getClass());
        }

        void visit(BeanPropertyValueConstraint valueConstraint) {
            visitorSupport.invokeVisit(this, valueConstraint.getPredicate());
        }

        void visit(UnaryAnd and) {
            Iterator it = and.iterator();
            while (it.hasNext()) {
                UnaryPredicate p = (UnaryPredicate)it.next();
                visitorSupport.invokeVisit(this, p);
                if (it.hasNext()) {
                    add("and", null, "add");
                }
            }
        }

        void visit(UnaryOr or) {
            Iterator it = or.iterator();
            while (it.hasNext()) {
                UnaryPredicate p = (UnaryPredicate)it.next();
                visitorSupport.invokeVisit(this, p);
                if (it.hasNext()) {
                    add("or", null, "or");
                }
            }
        }

        void visit(UnaryNot not) {
            add("not", null, "not");
            visitorSupport.invokeVisit(this, not.getPredicate());
        }

        //@HACK - please consider standard visitor here...
        void visit(StringLengthConstraint constraint) {
            add(getAsProperty(constraint), new Object[] { new Integer(2) }, constraint.toString());
        }

        void visit(UnaryPredicate constraint) {
            add(getAsProperty(constraint), null, constraint.toString());
        }

    }

    public static class SummingVisitor implements Visitor {
        private ReflectiveVisitorSupport visitorSupport =
            new ReflectiveVisitorSupport();
        private int sum;
        private UnaryPredicate constraint;

        public SummingVisitor(UnaryPredicate constraint) {
            this.constraint = constraint;
        }

        public int sum() {
            visitorSupport.invokeVisit(this, constraint);
            return sum;
        }

        void visit(CompoundBeanPropertyExpression rule) {
            visitorSupport.invokeVisit(this, rule.getPredicate());
        }

        void visit(BeanPropertiesExpression e) {
            sum++;
        }

        void visit(ParameterizedBeanPropertyExpression e) {
            sum++;
        }

        void visit(UnaryAnd and) {
            Iterator it = and.iterator();
            while (it.hasNext()) {
                UnaryPredicate p = (UnaryPredicate)it.next();
                visitorSupport.invokeVisit(this, p);
            }
        }

        void visit(UnaryOr or) {
            Iterator it = or.iterator();
            while (it.hasNext()) {
                UnaryPredicate p = (UnaryPredicate)it.next();
                visitorSupport.invokeVisit(this, p);
            }
        }

        void visit(UnaryPredicate constraint) {
            sum++;
        }

    }

}
