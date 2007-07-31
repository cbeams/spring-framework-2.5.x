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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to indicate that a test is enabled for a specific testing
 * environment. If the configured {@link ProfileValueSource} returns a matching
 * {@link #value() value} for the provided {@link #name() name}, the test will
 * be enabled. This annotation can be applied at the class-level or
 * method-level.
 *
 * <p>Example: when using {@link SystemProfileValueSource} as the
 * {@link ProfileValueSource} implementation, you could configure a JUnit 4 test
 * method to run only on Java VMs from Sun Microsystems as follows:
 *
 * <pre class="code">
 * &#064;IfProfileValue(name=&quot;java.vendor&quot;, value=&quot;Sun Microsystems Inc.&quot;)
 * &#064;Test
 * testSomething() {
 *     // ...
 * }</pre>
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 2.0
 * @see ProfileValueSource
 * @see AbstractAnnotationAwareTransactionalTests
 */
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IfProfileValue {

	/**
	 * The <code>name</code> of the <em>profile value</em> against which to test.
	 */
	String name();

	/**
	 * The required <code>value</code> of the <em>profile value</em> for the
	 * given {@link #name() name}.
	 */
	String value();

}
