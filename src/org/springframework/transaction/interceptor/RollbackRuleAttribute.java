/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

/**
 * Rule determining whether or not a given exception (and any subclasses) should
 * cause a rollback. Multiple such rules can be applied to determine whether a
 * transaction should commit or rollback after an exception has been thrown.
 * @since 09-Apr-2003
 * @version $Id: RollbackRuleAttribute.java,v 1.4 2003-11-27 18:36:18 jhoeller Exp $
 * @author Rod Johnson
 * @see NoRollbackRuleAttribute
 */
public class RollbackRuleAttribute {
	
	public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS = new RollbackRuleAttribute("java.lang.RuntimeException");
	
	/**
	 * Could hold exception, resolving classname but would always require FQN.
	 * This way does multiple string comparisons, but how often do we decide
	 * whether to roll back a transaction following an exception?
	 */
	private final String exceptionName;

	/**
	 * Construct a new RollbackRule for the given exception name.
	 * This can be a substring, with no wildcard support at present.
	 * A value of "ServletException" would match ServletException and
	 * subclasses, for example.
	 * <p><b>NB: </b>Consider carefully how specific the pattern is, and whether
	 * to include package information (which isn't mandatory). For example,
	 * "Exception" will match nearly anything, and will probably hide other rules.
	 * "java.lang.Exception" would be correct if "Exception" was meant to define
	 * a rule for all checked exceptions. With more unusual Exception
	 * names such as "BaseBusinessException" there's no need to use a FQN.
	 * @param exceptionName the exception pattern
	 */
	public RollbackRuleAttribute(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	/**
	 * Return the pattern for the exception name.
	 */
	public String getExceptionName() {
		return exceptionName;
	}

	/**
	 * Return the depth to the superclass matching.
	 * 0 means t matches. Return -1 if there's no match.
	 * Otherwise, return depth. Lowest depth wins.
	 */
	public int getDepth(Throwable t) {
		return getDepth(t.getClass(), 0);
	}

	private int getDepth(Class exceptionClass, int depth) {
		if (exceptionClass.getName().indexOf(this.exceptionName) != -1) {
			// Found it!
			return depth;
		}
		// If we've gone as far as we can go and haven't found it...
		if (exceptionClass.equals(Throwable.class)) {
			return -1;
		}
		return getDepth(exceptionClass.getSuperclass(), depth + 1);
	}
	
	public String toString() {
		return "RollbackRule with pattern '" + this.exceptionName + "'";
	}

}
