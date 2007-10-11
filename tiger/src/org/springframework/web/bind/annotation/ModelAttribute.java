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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that binds a method parameter or method return value
 * to a named model attribute, exposed to a web view. Supported
 * for {@link RequestMapping} annotated handler classes.
 *
 * <p>Can be used to expose reference data to a web view
 * (through annotating no-arg accessor methods in a controller class),
 * or to expose command/form objects to a web view (through annotating
 * corresponding parameters of a {@link RequestMapping} annotated
 * handler method).
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModelAttribute {

	/**
	 * The name of the model attribute to bind to.
	 * <p>The default moddel attribute name is inferred from the
	 * attribute type, based on the non-qualified class name:
	 * e.g. "order" for class "mypackage.Order".
	 */
	String value();

}
