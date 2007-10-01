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

package org.springframework.test.context.support;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Abstract application context loader, which provides a basis for all concrete
 * implementations of the {@link ContextLoader} strategy. Provides a
 * <em>Template Method</em> based approach for
 * {@link #processLocations(Class,String...) processing} locations.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see #generateDefaultLocations(Class)
 * @see #modifyLocations(Class,String...)
 */
public abstract class AbstractContextLoader implements ContextLoader {

	/**
	 * <p>
	 * Determines whether or not <em>default</em> resource locations should be
	 * generated if the <code>locations</code> provided to
	 * {@link #processLocations(Class,String...) processLocations()} are
	 * <code>null</code> or empty.
	 * </p>
	 * <p>
	 * Can be overridden by subclasses to change the default behavior.
	 * </p>
	 *
	 * @return <code>true</code>.
	 */
	protected boolean isGenerateDefaultLocations() {
		return true;
	}

	/**
	 * <p>
	 * Gets the suffix to append to {@link ApplicationContext} resource
	 * locations when generating default locations.
	 * </p>
	 * <p>
	 * Must be implemented by subclasses.
	 * </p>
	 *
	 * @return The resource suffix; should not be <code>null</code> or empty.
	 * @see #generateDefaultLocations(Class)
	 */
	protected abstract String getResourceSuffix();

	/**
	 * <p>
	 * If the supplied <code>locations</code> are <code>null</code> or
	 * <em>empty</em> and {@link #isGenerateDefaultLocations()} is
	 * <code>true</code>, default locations will be
	 * {@link #generateDefaultLocations(Class) generated} for the specified
	 * {@link Class class} and the configured
	 * {@link #getResourceSuffix() resource suffix}; otherwise, the supplied
	 * <code>locations</code> will be
	 * {@link #modifyLocations(Class,String...) modified} if necessary and
	 * returned.
	 * </p>
	 *
	 * @param clazz The class with which the locations are associated: to be
	 *        used when generating default locations.
	 * @param locations The unmodified locations to use for loading the
	 *        application context; can be <code>null</code> or empty.
	 * @return An array of application context resource locations.
	 * @see #generateDefaultLocations(Class)
	 * @see #modifyLocations(Class,String...)
	 * @see org.springframework.test.context.ContextLoader#processLocations(java.lang.Class,
	 *      java.lang.String[])
	 */
	public final String[] processLocations(final Class<?> clazz, final String... locations) {
		return (ObjectUtils.isEmpty(locations) && isGenerateDefaultLocations()) ? generateDefaultLocations(clazz)
				: modifyLocations(clazz, locations);
	}

	/**
	 * <p>
	 * Generates the default classpath resource locations array based on the
	 * supplied class.
	 * </p>
	 * <p>
	 * For example, if the supplied class is <code>com.example.MyTest</code>,
	 * the generated locations will contain a single string with a value of
	 * &quot;classpath:/com/example/MyTest<code>&lt;suffix&gt;</code>&quot;,
	 * where <code>&lt;suffix&gt;</code> is the value of the
	 * {@link #getResourceSuffix() resource suffix} string.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>default location generation</em> strategy.
	 * </p>
	 *
	 * @param clazz The class for which the default locations are to be
	 *        generated.
	 * @return An array of default application context resource locations.
	 * @see #getResourceSuffix()
	 */
	protected String[] generateDefaultLocations(final Class<?> clazz) {
		Assert.notNull(clazz, "clazz can not be null.");
		Assert.hasText(getResourceSuffix(), "resourceSuffix can not be empty.");
		return new String[] { ResourceUtils.CLASSPATH_URL_PREFIX + "/"
				+ ClassUtils.convertClassNameToResourcePath(clazz.getName()) + getResourceSuffix() };
	}

	/**
	 * <p>
	 * Generates a modified version of the supplied locations array and returns
	 * it.
	 * </p>
	 * <p>
	 * A plain path, e.g. &quot;context.xml&quot;, will be treated as a
	 * classpath resource from the same package in which the specified class is
	 * defined. A path starting with a slash is treated as a fully qualified
	 * class path location, e.g.:
	 * &quot;/org/springframework/whatever/foo.xml&quot;. A path which
	 * references a URL (e.g., a path prefixed with
	 * {@link ResourceUtils#CLASSPATH_URL_PREFIX classpath:},
	 * {@link ResourceUtils#FILE_URL_PREFIX file:}, <code>http:</code>,
	 * etc.) will be added to the results unchanged.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>location modification</em> strategy.
	 * </p>
	 *
	 * @param clazz The class with which the locations are associated.
	 * @param locations The resource locations to be modified.
	 * @return An array of modified application context resource locations.
	 */
	protected String[] modifyLocations(final Class<?> clazz, final String... locations) {

		final String[] modifiedLocations = new String[locations.length];

		for (int i = 0; i < locations.length; i++) {
			final String path = locations[i];
			if (path.startsWith("/")) {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX + path;
			}
			else if (!ResourceUtils.isUrl(path)) {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX + "/"
						+ StringUtils.cleanPath(ClassUtils.classPackageAsResourcePath(clazz) + "/" + path);
			}
			else {
				modifiedLocations[i] = StringUtils.cleanPath(path);
			}
		}
		return modifiedLocations;
	}

}
