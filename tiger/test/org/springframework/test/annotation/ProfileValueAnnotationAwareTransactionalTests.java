/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.test.annotation;

/**
 * <p>
 * Verifies proper handling of {@link IfProfileValue @IfProfileValue} in
 * conjunction with {@link AbstractAnnotationAwareTransactionalTests}.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
public class ProfileValueAnnotationAwareTransactionalTests extends AbstractAnnotationAwareTransactionalTests {

	private static final String NAME = "ProfileValueAnnotationAwareTransactionalTests.profile_value.name";
	private static final String VALUE = "enigma";


	public ProfileValueAnnotationAwareTransactionalTests() throws Exception {
		this(null);
	}

	public ProfileValueAnnotationAwareTransactionalTests(final String name) throws Exception {
		super(name);
		System.setProperty(NAME, VALUE);
	}

	@Override
	protected String getConfigPath() {
		return "ProfileValueAnnotationAwareTransactionalTests-context.xml";
	}

	@NotTransactional
	@IfProfileValue(name = NAME, value = "")
	public void testIfProfileValueEmpty() {
		fail("The body of a disabled test should never be executed!");
	}

	@NotTransactional
	@IfProfileValue(name = NAME, value = VALUE + "X")
	public void testIfProfileValueDisabled() {
		fail("The body of a disabled test should never be executed!");
	}

	@NotTransactional
	@IfProfileValue(name = NAME, value = VALUE)
	public void testIfProfileValueEnabled() {
		/* no-op */
	}

	@NotTransactional
	public void testIfProfileValueNotConfigured() {
		/* no-op */
	}

}
