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
 * Class that encapsulates an object error, i.e. a global reason for
 * rejecting an object.
 *
 * <p>See DefaultMessageCodesResolver javadoc for details on how a message
 * code list is built for an ObjectError.
 *
 * @author Juergen Hoeller
 * @since 10.03.2003
 * @see FieldError
 * @see DefaultMessageCodesResolver
 */
public class ObjectError extends DefaultMessageSourceResolvable {

  private final String objectName;

  /**
   * Create a new ObjectError instance, using multiple codes.
   * @see org.springframework.context.MessageSourceResolvable#getCodes
   */
	public ObjectError(String objectName, String[] codes, Object[] args, String defaultMessage) {
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
