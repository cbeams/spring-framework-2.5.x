package org.springframework.beans.factory.xml;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.LifecycleBean;

/**
 * Simple bean used to check constructor dependency checking.
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class DerivedConstructorDependenciesBean extends ConstructorDependenciesBean {

	public DerivedConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, LifecycleBean other, int age, String name) {
		super(spouse1, spouse2, other);
		setAge(age);
		setName(name);
	}

}
