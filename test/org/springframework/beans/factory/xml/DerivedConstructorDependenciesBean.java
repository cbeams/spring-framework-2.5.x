package org.springframework.beans.factory.xml;

import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.TestBean;

/**
 * Simple bean used to check constructor dependency checking.
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class DerivedConstructorDependenciesBean extends ConstructorDependenciesBean {

	public DerivedConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, IndexedTestBean other) {
		super(spouse1, spouse2, other);
	}

	public DerivedConstructorDependenciesBean(TestBean spouse1, Object spouse2, IndexedTestBean other) {
		super(spouse1, null, other);
	}

	public DerivedConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, IndexedTestBean other, int age, int otherAge) {
		super(spouse1, spouse2, other);
	}

	public DerivedConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, IndexedTestBean other, int age, String name) {
		super(spouse1, spouse2, other);
		setAge(age);
		setName(name);
	}

}
