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

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * <p>
 * General testing utility methods for working with
 * {@link ProfileValueSource ProfileValueSources}.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
public abstract class ProfileValueUtils {

	/**
	 * <p>
	 * Searches for a unique {@link ProfileValueSource} in the supplied
	 * {@link ApplicationContext}.
	 * </p>
	 *
	 * @param applicationContext the ApplicationContext in which to search for
	 *        the ProfileValueSource.
	 * @return the unique ProfileValueSource; or <code>null</code> if not
	 *         found or if multiple ProfileValueSources were found.
	 */
	public static final ProfileValueSource findUniqueProfileValueSource(final ApplicationContext applicationContext) {

		final Map<?, ?> beans = applicationContext.getBeansOfType(ProfileValueSource.class);
		if (beans.size() == 1) {
			return (ProfileValueSource) beans.values().iterator().next();
		}
		return null;
	}

	/**
	 * <p>
	 * Determines if the test for the supplied <code>testMethod</code> is
	 * <em>enabled</em> in the current environment, as specified by the
	 * {@link IfProfileValue @IfProfileValue} annotation, which may be present
	 * on the test method itself or at the class-level. Defaults to
	 * <code>true</code> if no {@link IfProfileValue @IfProfileValue}
	 * annotation is present.
	 * </p>
	 *
	 * @param profileValueSource the ProfileValueSource to use to determine if
	 *        the test is enabled.
	 * @param testMethod the test method.
	 * @return <code>true</code> if the test is <em>enabled</em> in the
	 *         current environment.
	 */
	public static final boolean isTestEnabledInThisEnvironment(final ProfileValueSource profileValueSource,
			final Method testMethod) {

		boolean enabled = true;

		IfProfileValue inProfile = testMethod.getAnnotation(IfProfileValue.class);
		if (inProfile == null) {
			inProfile = testMethod.getDeclaringClass().getAnnotation(IfProfileValue.class);
		}

		if (inProfile != null) {
			final String name = inProfile.name();
			Assert.hasText(name, "The name attribute supplied to @IfProfileValue must not be empty.");

			final String annotatedValue = inProfile.value();
			final String environmentValue = profileValueSource.get(name);
			final boolean bothValuesAreNull = (environmentValue == null) && (annotatedValue == null);

			enabled = bothValuesAreNull || ((environmentValue != null) && environmentValue.equals(annotatedValue));
		}

		// XXX Optional: add support for @IfNotProfileValue.

		return enabled;
	}

}
