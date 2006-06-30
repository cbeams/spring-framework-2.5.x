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

package org.springframework.test.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
* Test annotation to indicate that a test
* method dirties the context for the current test.
* Using this annotation is less error-prone than
* calling setDirty() explicitly because the call
* to setDirty() is guaranteed to occur, even if the test
* failed. If only a particular code path in the test
* dirties the context, prefer calling setDirty()
* explicitly--and take care!
* 
* @author Rod Johnson
* @since 2.0
* @see AbstractDependencyInjectionSpringContextTests
*/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DirtiesContext {
	

}
