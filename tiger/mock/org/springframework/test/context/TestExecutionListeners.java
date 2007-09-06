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

package org.springframework.test.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * TestExecutionListeners defines class-level metadata for configuring which
 * {@link TestExecutionListener TestExecutionListeners} should be registered
 * with a {@link TestContextManager}. Typically, &#064;TestExecutionListeners
 * will be used in conjunction with &#064;ContextConfiguration.
 *
 * @author Sam Brannen
 * @see TestExecutionListener
 * @see TestContextManager
 * @see ContextConfiguration
 * @since 2.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface TestExecutionListeners {

	/**
	 * <p>
	 * The {@link TestExecutionListener TestExecutionListeners} to register with
	 * a {@link TestContextManager}.
	 * </p>
	 * <p>
	 * Defaults to {@link DependencyInjectionTestExecutionListener},
	 * {@link DirtiesContextTestExecutionListener}, and
	 * {@link TransactionalTestExecutionListener}.
	 * </p>
	 */
	Class<? extends TestExecutionListener>[] value() default {DependencyInjectionTestExecutionListener.class,
			DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class};

}
