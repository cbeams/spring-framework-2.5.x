/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.junit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.Employee;
import org.springframework.beans.Pet;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

/**
 * <p>
 * SpringJUnit4ClassRunnerAppCtxTests serves as a <em>proof of concept</em>
 * JUnit 4 based test class, which verifies the expected functionality of
 * {@link SpringJUnit4ClassRunner} in conjunction with
 * {@link ContextConfiguration} and Spring's {@link Autowired} annotation.
 * </p>
 * <p>
 * Since no {@link ContextConfiguration#locations() locations} are explicitly
 * defined and
 * {@link ContextConfiguration#generateDefaultLocations() generateDefaultLocations}
 * is left set to its default value of <code>true</code>, this test class's
 * dependencies will be injected via
 * {@link Autowired annotation-based autowiring} from beans defined in the
 * {@link ApplicationContext} loaded from the default classpath resource: &quot;<code>/org/springframework/test/context/junit/SpringJUnit4ClassRunnerAppCtxTests-context.xml</code>&quot;.
 * </p>
 *
 * @see AbsolutePathSpringJUnit4ClassRunnerAppCtxTests
 * @see RelativePathSpringJUnit4ClassRunnerAppCtxTests
 * @see InheritedConfigSpringJUnit4ClassRunnerAppCtxTests
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class SpringJUnit4ClassRunnerAppCtxTests implements ApplicationContextAware, BeanNameAware, InitializingBean {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Default resource path for the test application context configuration for
	 * {@link SpringJUnit4ClassRunnerAppCtxTests}:
	 * <code>&quot;/org/springframework/test/context/junit4/SpringJUnit4ClassRunnerAppCtxTests-context.xml&quot;</code>
	 */
	public static final String	DEFAULT_CONTEXT_RESOURCE_PATH	= "/org/springframework/test/context/junit4/SpringJUnit4ClassRunnerAppCtxTests-context.xml";

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(SpringJUnit4ClassRunnerAppCtxTests.class);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private ApplicationContext	applicationContext;

	private boolean				beanInitialized	= false;

	private String				beanName		= "replace me with null";

	private Employee			employee;

	@Autowired
	private Pet					pet;

	@Autowired(required = false)
	protected Long				nonrequiredLong;

	@Resource()
	protected String			foo;

	protected String			bar;

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public final void afterPropertiesSet() throws Exception {

		this.beanInitialized = true;
	}

	/**
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

		this.applicationContext = applicationContext;
	}

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public final void setBeanName(final String beanName) {

		this.beanName = beanName;
	}

	@Autowired
	protected final void setEmployee(final Employee employee) {

		this.employee = employee;
	}

	@Resource
	protected final void setBar(final String bar) {

		this.bar = bar;
	}

	// ------------------------------------------------------------------------|

	@Test
	public final void verifyApplicationContext() {

		assertNotNull("The application context should have been set due to ApplicationContextAware semantics.",
				this.applicationContext);
	}

	@Test
	public final void verifyBeanInitialized() {

		assertTrue("This test bean should have been initialized due to InitializingBean semantics.",
				this.beanInitialized);
	}

	@Test
	public final void verifyBeanNameSet() {

		assertEquals("The bean name of this test instance should have been set to the fully qualified class name "
				+ "due to BeanNameAware semantics.", getClass().getName(), this.beanName);
	}

	@Test
	public final void verifyAnnotationAutowiredFields() {

		assertNull("The nonrequiredLong property should NOT have been autowired.", this.nonrequiredLong);
		assertNotNull("The pet field should have been autowired.", this.pet);
		assertEquals("Fido", this.pet.getName());
	}

	@Test
	public final void verifyAnnotationAutowiredMethods() {

		assertNotNull("The employee setter method should have been autowired.", this.employee);
		assertEquals("John Smith", this.employee.getName());
	}

	@Test
	public final void verifyResourceAnnotationWiredFields() {

		assertEquals("The foo field should have been wired via @Resource.", "Foo", this.foo);
	}

	@Test
	public final void verifyResourceAnnotationWiredMethods() {

		assertEquals("The bar method should have been wired via @Resource.", "Bar", this.bar);
	}

	// ------------------------------------------------------------------------|

}
