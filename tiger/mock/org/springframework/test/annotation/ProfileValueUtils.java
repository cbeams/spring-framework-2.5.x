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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * General utility methods for working with <em>profile values</em>.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see ProfileValueSource
 * @see ProfileValueSourceConfiguration
 * @see IfProfileValue
 */
public abstract class ProfileValueUtils {

	/** Class Logger. */
	private static final Log logger = LogFactory.getLog(ProfileValueUtils.class);


	/**
	 * <p>
	 * Retrieves the {@link ProfileValueSource} type for the specified
	 * {@link Class test class} as configured via the
	 * {@link ProfileValueSourceConfiguration @ProfileValueSourceConfiguration}
	 * annotation and instantiates a new instance of that type.
	 * </p>
	 * <p>
	 * If
	 * {@link ProfileValueSourceConfiguration @ProfileValueSourceConfiguration}
	 * is not present on the specified class or if a custom
	 * {@link ProfileValueSource} is not declared, the default
	 * {@link SystemProfileValueSource} will be returned instead.
	 * </p>
	 *
	 * @param testClass The test class for which the ProfileValueSource should
	 *        be retrieved.
	 * @return The configured (or default) ProfileValueSource for the specified
	 *         class.
	 * @see SystemProfileValueSource
	 */
	@SuppressWarnings("unchecked")
	public static final ProfileValueSource retrieveProfileValueSource(final Class<?> testClass) {

		Assert.notNull(testClass, "Can not retrieve a ProfileValueSource for a NULL class.");
		final Class<ProfileValueSourceConfiguration> annotationType = ProfileValueSourceConfiguration.class;
		final ProfileValueSourceConfiguration config = testClass.getAnnotation(annotationType);
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved @ProfileValueSourceConfiguration [" + config + "] for test class [" + testClass
					+ "].");
		}

		Class<? extends ProfileValueSource> profileValueSourceType;
		if (config != null) {
			profileValueSourceType = config.value();
		}
		else {
			profileValueSourceType = (Class<? extends ProfileValueSource>) AnnotationUtils.getDefaultValue(annotationType);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved ProfileValueSource type [" + profileValueSourceType + "] for class [" + testClass
					+ "].");
		}

		ProfileValueSource profileValueSource;
		if (SystemProfileValueSource.class.equals(profileValueSourceType)) {
			profileValueSource = SystemProfileValueSource.getInstance();
		}
		else {
			try {
				profileValueSource = profileValueSourceType.newInstance();
			}
			catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Could not instantiate a ProfileValueSource of type [" + profileValueSourceType
							+ "] for class [" + testClass + "]: using default.", e);
				}
				profileValueSource = SystemProfileValueSource.getInstance();
			}
		}

		return profileValueSource;
	}

	/**
	 * <p>
	 * Determines if the supplied <code>testMethod</code> is <em>enabled</em>
	 * in the current environment, as specified by the
	 * {@link IfProfileValue @IfProfileValue} annotation, which may be declared
	 * on the test method itself or at the class-level.
	 * </p>
	 * <p>
	 * Defaults to <code>true</code> if no
	 * {@link IfProfileValue @IfProfileValue} annotation is declared.
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

		IfProfileValue ifProfileValue = testMethod.getAnnotation(IfProfileValue.class);
		if (ifProfileValue == null) {
			ifProfileValue = testMethod.getDeclaringClass().getAnnotation(IfProfileValue.class);
		}

		if (ifProfileValue != null) {
			final String name = ifProfileValue.name();
			Assert.hasText(name, "The name attribute supplied to @IfProfileValue must not be empty.");

			final String environmentValue = profileValueSource.get(name);
			final String annotatedValue = ifProfileValue.value();
			final String[] annotatedValues = ifProfileValue.values();
			final boolean annotatedValueEmpty = !StringUtils.hasText(annotatedValue);
			final boolean annotatedValuesEmpty = ObjectUtils.isEmpty(annotatedValues);

			if (annotatedValueEmpty && annotatedValuesEmpty) {
				throw new IllegalArgumentException(
						"Either the 'value' or 'values' attribute of @IfProfileValue must be set.");
			}

			if (!annotatedValueEmpty && !annotatedValuesEmpty) {
				throw new IllegalArgumentException(
						"Setting both the 'value' and 'values' attributes of @IfProfileValue is not allowed: choose one or the other.");
			}

			final String[] values = (!annotatedValuesEmpty ? annotatedValues : new String[] { annotatedValue });
			for (String value : values) {
				boolean bothValuesAreNull = (environmentValue == null) && (value == null);
				enabled = bothValuesAreNull || ((environmentValue != null) && environmentValue.equals(value));
				if (enabled) {
					break;
				}
			}
		}

		// XXX Optional: add support for @IfNotProfileValue.

		return enabled;
	}

}
