/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.transaction.interceptor;

import java.io.Serializable;

import org.springframework.aop.framework.AopConfigException;

/**
 * Rule determining whether or not a given exception (and any subclasses) should
 * cause a rollback. Multiple such rules can be applied to determine whether a
 * transaction should commit or rollback after an exception has been thrown.
 * @since 09-Apr-2003
 * @author Rod Johnson
 * @see NoRollbackRuleAttribute
 */
public class RollbackRuleAttribute implements Serializable{
	
	public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS = new RollbackRuleAttribute("java.lang.RuntimeException");
	
	/**
	 * Could hold exception, resolving classname but would always require FQN.
	 * This way does multiple string comparisons, but how often do we decide
	 * whether to roll back a transaction following an exception?
	 */
	private final String exceptionName;
	
	/**
	 * Preferred way to construct a RollbackRule, matching
	 * the exception class and subclasses. The exception class must be
	 * Throwable or a subclass of Throwable.
	 * @param clazz throwable class
	 */
	public RollbackRuleAttribute(Class clazz) {
		if (!Throwable.class.isAssignableFrom(clazz))
			throw new AopConfigException("Cannot construct rollback rule from " + clazz + "; " +
					"it's not a Throwable");
		this.exceptionName = clazz.getName();
	}

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
	
	public boolean equals(Object o) {
		if ( !(o instanceof RollbackRuleAttribute) )
			return false;

		RollbackRuleAttribute rhs = (RollbackRuleAttribute) o;
		
		// no possibility of null
		return this.exceptionName.equals(rhs.exceptionName);
	}
	
	public int hashCode() {
		return exceptionName.hashCode();
	}
	
}
