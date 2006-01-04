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

package org.springframework.scripting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Rob Harrop
 * @since 2.0M2
 */
public abstract class AbstractStringBasedScript extends AbstractScript {

	protected AbstractStringBasedScript(ScriptSource scriptSource) {
		super(scriptSource);
	}

	protected final Object doCreateObject(InputStream inputStream) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer code = new StringBuffer();
		String line;

		while ((line = reader.readLine()) != null) {
			code.append(line).append("\n");
		}
		return doCreateObjectFromScript(code.toString());
	}

	protected abstract Object doCreateObjectFromScript(String script) throws Exception;
}
