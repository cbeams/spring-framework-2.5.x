/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.validation;

import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Encapsulates an object error, i.e. a global reason for rejection.
 *
 * <p>Normally, an ObjectError has a single code for message resolution.
 *
 * @author Juergen Hoeller
 * @since 10.03.2003
 * @see FieldError
 */
public class ObjectError extends DefaultMessageSourceResolvable {

  private final String objectName;

	/**
	 * Create a new ObjectError instance, using a default code.
	 */
	public ObjectError(String objectName, String code, Object[] args, String defaultMessage) {
	  this(objectName, new String[] {code}, args, defaultMessage);
	}

  /**
   * Create a new ObjectError instance, using multiple codes.
   * <p>This is only meant to be used by subclasses like FieldError.
   * @see org.springframework.context.MessageSourceResolvable#getCodes
   */
	protected ObjectError(String objectName, String[] codes, Object[] args, String defaultMessage) {
    super(codes, args, defaultMessage);
    this.objectName = objectName;
  }

  public String getObjectName() {
    return objectName;
  }

  public String toString() {
    return "Error occurred in object [" + this.objectName + "]: " + resolvableToString();
  }

}
