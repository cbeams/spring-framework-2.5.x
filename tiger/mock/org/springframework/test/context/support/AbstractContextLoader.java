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
package org.springframework.test.context.support;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * TODO Revise JavaDoc!
 * <p>
 * Abstract application context loader, which provides a basis for all concrete
 * implementations of the {@link ContextLoader} strategy.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.3 $
 * @since 2.1
 */
public abstract class AbstractContextLoader implements ContextLoader {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * TODO Revise JavaDoc!
	 * <p>
	 * Whether or not <em>default</em> resource locations should be generated
	 * if no {@link #locations() locations} are explicitly defined.
	 * </p>
	 * <p>
	 * Defaults to <code>true</code>.
	 * </p>
	 *
	 * @return Returns the generateDefaultLocations.
	 */
	protected final boolean isGenerateDefaultLocations() {

		return true;
	}

	// ------------------------------------------------------------------------|

	/**
	 * TODO Revise JavaDoc!
	 * <p>
	 * The suffix to append to {@link ApplicationContext} resource locations
	 * when generating default locations.
	 * </p>
	 * <p>
	 * Defaults to &quot;<code>-context.xml</code>&quot;.
	 * </p>
	 *
	 * @see #generateDefaultLocations()
	 * @return Returns the resourceSuffix.
	 */
	protected abstract String getResourceSuffix();

	// ------------------------------------------------------------------------|

	/**
	 * TODO Revise JavaDoc!
	 * <p>
	 * Processes application context configuration locations.
	 * </p>
	 * <p>
	 * If the supplied <code>locations</code> are <code>null</code> or
	 * <em>empty</em> and <code>generateDefaultLocations</code> is
	 * <code>true</code>, default locations will be
	 * {@link #generateDefaultLocations(Class, String) generated} for the
	 * specified {@link Class} and <code>resourcesSuffix</code>; otherwise,
	 * the supplied <code>locations</code> will be
	 * {@link #modifyLocations(String[], Class) modified} if necessary and
	 * returned.
	 * </p>
	 *
	 * @see #generateDefaultLocations(Class, String)
	 * @see #modifyLocations(String[], Class)
	 * @param locations The unmodified locations to use for loading the
	 *        application context; can be <code>null</code> or empty.
	 * @param clazz The class with which the locations are associated: to be
	 *        used when generating default locations.
	 * @param generateDefaultLocations Whether or not default locations should
	 *        be generated if no locations are explicitly defined.
	 * @param resourceSuffix The suffix to append to application context
	 *        resource paths when generating default locations.
	 * @return An array of config locations
	 */
	public final String[] processLocations(final String[] locations, final Class<?> clazz) {

		return (ArrayUtils.isEmpty(locations) && isGenerateDefaultLocations()) ? generateDefaultLocations(clazz)
				: modifyLocations(locations, clazz);
	}

	// ------------------------------------------------------------------------|

	/**
	 * TODO Revise JavaDoc!
	 * <p>
	 * Generates the default classpath resource locations array based on the
	 * supplied class.
	 * </p>
	 * <p>
	 * For example, if the supplied class is <code>com.example.MyTest</code>,
	 * the generated locations will contain a single string with a value of
	 * &quot;classpath:/com/example/MyTest<code>&lt;suffix&gt;</code>&quot;,
	 * where <code>&lt;suffix&gt;</code> is the value of the supplied
	 * <code>resourceSuffix</code> string.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>default location generation</em> strategy.
	 * </p>
	 *
	 * @param clazz The class for which the default locations are to be
	 *        generated.
	 * @return An array of default config locations.
	 */
	protected String[] generateDefaultLocations(final Class<?> clazz) {

		Assert.notNull(clazz, "clazz can not be null.");
		Assert.hasText(getResourceSuffix(), "resourceSuffix can not be empty.");

		return new String[] { ResourceUtils.CLASSPATH_URL_PREFIX + "/"
				+ ClassUtils.convertClassNameToResourcePath(clazz.getName()) + getResourceSuffix() };
	}

	// ------------------------------------------------------------------------|

	/**
	 * TODO Revise JavaDoc!
	 * <p>
	 * Generates a modified version of the supplied locations array and returns
	 * it.
	 * </p>
	 * <p>
	 * A plain path, e.g. &quot;context.xml&quot;, will be treated as a
	 * classpath resource from the same package in which the specified class is
	 * defined. A path starting with a slash is treated as a fully qualified
	 * class path location, e.g.:
	 * &quot;/org/springframework/whatever/foo.xml&quot;.
	 * </p>
	 * <p>
	 * Subclasses can override this method to implement a different
	 * <em>location modification</em> strategy.
	 * </p>
	 *
	 * @param locations The resource locations to be modified.
	 * @param clazz The class with which the locations are associated.
	 * @return An array of modified config locations.
	 */
	protected String[] modifyLocations(final String[] locations, final Class<?> clazz) {

		final String[] modifiedLocations = new String[locations.length];

		for (int i = 0; i < locations.length; i++) {
			final String path = locations[i];
			if (path.startsWith("/")) {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX + path;
			}
			else {
				modifiedLocations[i] = ResourceUtils.CLASSPATH_URL_PREFIX
						+ StringUtils.cleanPath(ClassUtils.classPackageAsResourcePath(clazz) + "/" + path);
			}
		}
		return modifiedLocations;
	}

	// ------------------------------------------------------------------------|

}
