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

package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.AbstractMatcher;
import org.easymock.MockControl;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mock.easymock.AbstractScalarMockTemplate;
import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 */
public class CustomScopeConfigurerTests extends TestCase {

	private static final String FOO_SCOPE = "fooScope";


	public void testWithNoScopes() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(ConfigurableListableBeanFactory factory) throws Exception {
				CustomScopeConfigurer figurer = new CustomScopeConfigurer();
				figurer.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
				figurer.postProcessBeanFactory(factory);
			}
		}.test();
	}

	public void testSunnyDayWithBonaFideScopeInstances() throws Exception {
		MockControl mockScope = MockControl.createControl(Scope.class);
		final Scope scope = (Scope) mockScope.getMock();
		mockScope.replay();
		new ConfigurableListableBeanFactoryMockTemplate() {
			public void setupExpectations(MockControl mockControl, ConfigurableListableBeanFactory factory) throws Exception {
				factory.registerScope(FOO_SCOPE, scope);
			}
			protected void doTest(ConfigurableListableBeanFactory factory) throws Exception {
				Map scopes = new HashMap();
				scopes.put(FOO_SCOPE, scope);
				CustomScopeConfigurer figurer = new CustomScopeConfigurer();
				figurer.setScopes(scopes);
				figurer.postProcessBeanFactory(factory);
			}
		}.test();
		mockScope.verify();
	}

	public void testSunnyDayWithScopeClass() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			public void setupExpectations(MockControl mockControl, ConfigurableListableBeanFactory factory) throws Exception {
				factory.registerScope(FOO_SCOPE, null);
				mockControl.setMatcher(new RegisterScopeArgumentsMatcher());
			}
			protected void doTest(ConfigurableListableBeanFactory factory) throws Exception {
				Map scopes = new HashMap();
				scopes.put(FOO_SCOPE, NoOpScope.class);
				CustomScopeConfigurer figurer = new CustomScopeConfigurer();
				figurer.setScopes(scopes);
				figurer.postProcessBeanFactory(factory);
			}
		}.test();
	}

	public void testSunnyDayWithScopeClassName() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			public void setupExpectations(MockControl mockControl, ConfigurableListableBeanFactory factory) throws Exception {
				factory.registerScope(FOO_SCOPE, null);
				mockControl.setMatcher(new RegisterScopeArgumentsMatcher());
			}
			protected void doTest(ConfigurableListableBeanFactory factory) throws Exception {
				Map scopes = new HashMap();
				scopes.put(FOO_SCOPE, NoOpScope.class.getName());
				CustomScopeConfigurer figurer = new CustomScopeConfigurer();
				figurer.setScopes(scopes);
				figurer.postProcessBeanFactory(factory);
			}
		}.test();
	}

	public void testWhereScopeMapHasNullScopeValueInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, null);
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasNonScopeInstanceInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, this); // <-- not a valid value...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasNonExistentClassNameInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, "What she asked of me, at the end of the day, Caligula would have blushed"); // <-- not a valid value...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasNonScopeTypeClassInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, getClass()); // <-- not a valid value...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasNonEmptyClassNameInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, ""); // <-- not a valid value...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasWhitespaceOnlyClassNameInEntrySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, "\t "); // <-- not a valid value...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasNonStringTypedScopeNameInKeySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(this, new NoOpScope()); // <-- not a valid value (the key)...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasScopeThatThrowsExceptionOnInstantiation() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(UnsupportedOperationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(FOO_SCOPE, new BadScope());
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasEmptyStringScopeNameInKeySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put("", new NoOpScope()); // <-- not a valid value (the key)...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}

	public void testWhereScopeMapHasWhitespaceStringScopeNameInKeySet() throws Exception {
		new ConfigurableListableBeanFactoryMockTemplate() {
			protected void doTest(final ConfigurableListableBeanFactory factory) throws Exception {
				new AssertThrows(BeanInitializationException.class) {
					public void test() throws Exception {
						Map scopes = new HashMap();
						scopes.put(" \t \n  ", new NoOpScope()); // <-- not a valid value (the key)...
						CustomScopeConfigurer figurer = new CustomScopeConfigurer();
						figurer.setScopes(scopes);
						figurer.postProcessBeanFactory(factory);
					}
				}.runTest();
			}
		}.test();
	}


	private static final class NoOpScope implements Scope {

		public String getConversationId() {
			return null;
		}

		public Object get(String name, ObjectFactory objectFactory) {
			throw new UnsupportedOperationException();
		}

		public Object remove(String name) {
			throw new UnsupportedOperationException();
		}

		public void registerDestructionCallback(String name, Runnable callback) {
		}
	}


	private static final class BadScope implements Scope {

		public BadScope() {
			// throwing exception by design (see test above)...
			throw new UnsupportedOperationException();
		}

		public String getConversationId() {
			throw new UnsupportedOperationException();
		}

		public Object get(String name, ObjectFactory objectFactory) {
			throw new UnsupportedOperationException();
		}

		public Object remove(String name) {
			throw new UnsupportedOperationException();
		}

		public void registerDestructionCallback(String name, Runnable callback) {
			throw new UnsupportedOperationException();
		}
	}


	private abstract class ConfigurableListableBeanFactoryMockTemplate extends AbstractScalarMockTemplate {

		public ConfigurableListableBeanFactoryMockTemplate() {
			super(ConfigurableListableBeanFactory.class);
		}


		public final void setupExpectations(MockControl mockControl, Object mockObject) throws Exception {
			setupExpectations(mockControl, (ConfigurableListableBeanFactory) mockObject);
		}

		public final void doTest(Object mockObject) throws Exception {
			doTest((ConfigurableListableBeanFactory) mockObject);
		}


		public void setupExpectations(MockControl mockControl, ConfigurableListableBeanFactory factory) throws Exception {
		}

		protected abstract void doTest(ConfigurableListableBeanFactory factory) throws Exception;

	}


	private static final class RegisterScopeArgumentsMatcher extends AbstractMatcher {
		
		public boolean matches(Object[] expectedArguments, Object[] actualArguments) {
			assertNotNull(actualArguments);
			assertEquals(2, actualArguments.length);
			Object firstArg = actualArguments[0];
			assertNotNull(firstArg);
			assertEquals(FOO_SCOPE, firstArg);
			Object secondArg = actualArguments[1];
			assertNotNull(secondArg);
			assertTrue(secondArg instanceof NoOpScope);
			return true;
		}

	}

}
