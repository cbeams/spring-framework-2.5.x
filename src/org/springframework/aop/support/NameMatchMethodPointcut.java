package org.springframework.aop.support;

import java.lang.reflect.Method;

/**
 * Pointcut bean for simple method name matches,
 * as alternative to regexp patterns.
 * @author Juergen Hoeller
 * @since 11.02.2004
 * @see #isMatch
 */
public class NameMatchMethodPointcut extends StaticMethodMatcherPointcut {

	private String[] mappedNames = new String[0];

	/**
	 * Convenience method when we have only a single method name
	 * to match. Use either this method or setMappedNames(), not both.
	 * @see #setMappedNames
	 */
	public void setMappedName(String mappedName) {
		this.mappedNames = new String[] {mappedName};
	}

	/**
	 * Set the method names defining methods to match.
	 * Matching will be the union of all these; if any match,
	 * the pointcut matches.
	 */
	public void setMappedNames(String[] mappedNames) {
		this.mappedNames = mappedNames;
	}

	public boolean matches(Method m, Class targetClass) {
		for (int i = 0; i<this.mappedNames.length; i++) {
			String mappedName = this.mappedNames[i];
			if (mappedName.equals(m.getName()) || isMatch(m.getName(), mappedName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return if the given method name matches the mapped name.
	 * The default implementation checks for "xxx*" and "*xxx" matches.
	 * Can be overridden in subclasses.
	 * @param methodName the method name of the class
	 * @param mappedName the name in the descriptor
	 * @return if the names match
	 */
	protected boolean isMatch(String methodName, String mappedName) {
		return (mappedName.endsWith("*") && methodName.startsWith(mappedName.substring(0, mappedName.length() - 1))) ||
				(mappedName.startsWith("*") && methodName.endsWith(mappedName.substring(1, mappedName.length())));
	}

}
