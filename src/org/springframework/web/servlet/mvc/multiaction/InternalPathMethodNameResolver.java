/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.servlet.mvc.multiaction;

/**
 * Simple implementation of MethodNameResolver that maps URL to method
 * name. Although this is the default implementation used by the
 * MultiActionController class (because it requires no configuration),
 * it's bit naive for most applications. In particular, we don't usually
 * want to tie URL to implementation methods.
 *
 * <p>Maps the resource name after the last slash, ignoring an extension.
 * E.g. "/foo/bar/baz.html" to "baz", assuming a "/foo/bar/baz.html"
 * controller mapping to the corresponding MultiActionController handler.
 * method. Doesn't support wildcards.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
*/
public class InternalPathMethodNameResolver extends AbstractUrlMethodNameResolver {

	private String prefix = "";

	private String suffix = "";

	/**
	 * Specify a common prefix for handler method names.
	 * Will be prepended to the internal path found in the URL:
	 * e.g. internal path "baz", prefix "my" -> method name "mybaz".
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Specify a common suffix for handler method names.
	 * Will be appended to the internal path found in the URL:
	 * e.g. internal path "baz", suffix "Handler" -> method name "bazHandler".
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	protected String getHandlerMethodNameForUrlPath(String urlPath) {
		String methodName = urlPath;

		// Look at resource name after last slash in the URL path.
		int slashIndex = methodName.lastIndexOf('/');
		if (slashIndex != -1) {
			methodName = methodName.substring(slashIndex+1);
		}

		// Ignore extension, if any.
		int dotIndex = methodName.lastIndexOf('.');
		if (dotIndex != -1) {
			methodName = methodName.substring(0, dotIndex);
		}

		// Prepend prefix and append suffix.
		return (this.prefix + methodName + this.suffix);
	}

}
