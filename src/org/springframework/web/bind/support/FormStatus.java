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

package org.springframework.web.bind.support;

/**
 * Simple interface that can be injected into handler methods,
 * allowing them to signal that their form processing is complete.
 * The handler invoker may then follow up with appropriate cleanup,
 * e.g. of form session attributes.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public interface FormStatus {

	/**
	 * Mark the current form processing as complete, allowing for cleanup.
	 */
	void setComplete();

}
