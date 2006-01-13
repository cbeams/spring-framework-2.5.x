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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract implementation of the {@link Script} interface.
 * Provides base configuration of the underlying
 * {@link ScriptSource} and handles the opening/closing of
 * the {@link InputStream} for the script.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0M2
 */
public abstract class AbstractScript implements Script {

	/**
	 * <code>Log</code> instance for this <code>Script</code>.
	 */
	protected Log logger = LogFactory.getLog(getClass());

	/**
	 * {@link ScriptSource} for this <code>Script</code> instance.
	 */
	private ScriptSource scriptSource;

	/**
	 * Create a new instance using the given <code>ScriptSource</code> to
	 * locate the script code.
	 */
	protected AbstractScript(ScriptSource scriptSource) {
		this.scriptSource = scriptSource;
	}

	/**
	 * Handles opening/closing of the script {@link InputStream} and
	 * delegates to the {@link #doCreateObject} template method.
	 */
	public final Object createObject() throws Exception {
		InputStream is = null;
        try {
            is = getScriptSource().getScript();
            return doCreateObject(is);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ex) {
                    logger.debug("Unable to close InputStream.", ex);
                }
            }
        }
	}

	/**
	 * Allows sub-classes to access the {@link ScriptSource} passed in during
	 * construction.
	 */
	protected ScriptSource getScriptSource() {
		return this.scriptSource;
	}

	/**
	 * Sub-classes should implement this method and return an instance of
	 * scripted object represented by the supplied {@link InputStream}.
	 */
	protected abstract Object doCreateObject(InputStream inputStream) throws Exception;
}
