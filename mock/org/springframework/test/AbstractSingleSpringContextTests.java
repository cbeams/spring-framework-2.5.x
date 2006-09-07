/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Abstract test class that holds and exposes a single Spring ApplicationContext.
 *
 * <p>This class will cache contexts based on a <i>context key</i>:
 * normally the config locations String array describing the Spring resource
 * descriptors making up the context. Unless the <code>setDirty()</code> method
 * is called by a test, the context will not be reloaded, even across different
 * subclasses of this test. This is particularly beneficial if your context is
 * slow to construct, for example if you are using Hibernate and the time taken
 * to load the mappings is an issue.
 *
 * <p>If you don't want this behavior, you can override the <code>contextKey()</code>
 * method, most likely to return the test class. In conjunction with this you would
 * probably override the <code>getContext</code> method, which by default loads
 * the locations specified in the <code>getConfigLocations()</code> method.
 *
 * <p><b>WARNING:</b> When doing integration tests from within Eclipse, only use
 * classpath resource URLs. Else, you may see misleading failures when changing
 * context locations.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractSingleSpringContextTests extends AbstractSpringContextTests {

	/** Application context this test will run against */
	protected ConfigurableApplicationContext applicationContext;

	private int loadCount = 0;


	/**
	 * Default constructor for AbstractDependencyInjectionSpringContextTests.
	 */
	public AbstractSingleSpringContextTests() {
	}

	/**
	 * Constructor for AbstractDependencyInjectionSpringContextTests with a JUnit name.
	 * @param name the name of this text fixture
	 */
	public AbstractSingleSpringContextTests(String name) {
		super(name);
	}


	/**
	 * This implementation is final.
	 * Override <code>onSetUp</code> for custom behavior.
	 * @see #onSetUp()
	 */
	protected final void setUp() throws Exception {
		this.applicationContext = getContext(contextKey());
		prepareTestInstance();
		onSetUp();
	}

	/**
	 * Prepare this test instance, for example populating its fields.
	 * <p>This implementation does nothing.
	 */
	protected void prepareTestInstance() throws Exception {
	}

	/**
	 * Subclasses can override this method in place of the
	 * <code>setUp()</code> method, which is final in this class.
	 * This implementation does nothing.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onSetUp() throws Exception {
	}

	/**
	 * This implementation is final.
	 * Override <code>onTearDown</code> for custom behavior.
	 * @see #onTearDown()
	 */
	protected final void tearDown() throws Exception {
		onTearDown();
	}

	/**
	 * Subclasses can override this to add custom behavior on teardown.
	 * @throws Exception simply let any exception propagate
	 */
	protected void onTearDown() throws Exception {
	}


	/**
	 * Return a key for this context. Default is the config location array.
	 * @see #getConfigLocations()
	 */
	protected final Object contextKey() {
		return getConfigLocations();
	}

	/**
	 * This implementation loads a context from the given String array.
	 */
	protected final ConfigurableApplicationContext loadContext(Object key) {
		return loadContextLocations((String[]) key);
	}

	/**
	 * Load an ApplicationContext from the given config locations.
	 * @param locations the config locations
	 * @return the corresponding ApplicationContext instance (potentially cached)
	 */
	protected ConfigurableApplicationContext loadContextLocations(String[] locations) {
		++this.loadCount;
		if (logger.isInfoEnabled()) {
			logger.info("Loading context for: " + StringUtils.arrayToCommaDelimitedString(locations));
		}
		return new ClassPathXmlApplicationContext(locations);
	}

	/**
	 * Return the current number of context load attempts.
	 */
	public final int getLoadCount() {
		return loadCount;
	}

	/**
	 * Called to say that the "applicationContext" instance variable is dirty and
	 * should be reloaded. We need to do this if a test has modified the context
	 * (for example, by replacing a bean definition).
	 */
	protected void setDirty() {
		setDirty(contextKey());
	}


	/**
	 * Subclasses must implement this method to return the locations of their
	 * config files. A plain path will be treated as class path location.
	 * E.g.: "org/springframework/whatever/foo.xml". Note however that you may
	 * prefix path locations with standard Spring resource prefixes. Therefore,
	 * a config location path prefixed with "classpath:" with behave the same
	 * as a plain path, but a config location such as
	 * "file:/some/path/path/location/appContext.xml" will be treated as a
	 * filesystem location.
	 * @return an array of config locations
	 */
	protected abstract String[] getConfigLocations();

}
