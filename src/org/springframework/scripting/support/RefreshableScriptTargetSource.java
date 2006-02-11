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

package org.springframework.scripting.support;

import org.springframework.aop.target.dynamic.BeanFactoryRefreshableTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.util.Assert;

/**
 * Subclass of BeanFactoryRefreshableTargetSource that determines whether a
 * refresh is required through the given ScriptSource.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0
 */
public class RefreshableScriptTargetSource extends BeanFactoryRefreshableTargetSource {

	private final ScriptSource scriptSource;


	/**
	 * Create a new RefreshableScriptTargetSource.
	 * @param beanFactory the BeanFactory to fetch beans from
	 * @param beanName the name of the target bean
	 * @param scriptSource the ScriptSource to delegate to
	 * for determining whether a refresh is required
	 */
	public RefreshableScriptTargetSource(BeanFactory beanFactory, String beanName, ScriptSource scriptSource) {
		super(beanFactory, beanName);
		Assert.notNull(scriptSource, "ScriptSource must not be null");
		this.scriptSource = scriptSource;
	}

	/**
	 * Determine whether a refresh is required through calling
	 * ScriptSource's <code>isModified()</code> method.
	 * @see org.springframework.scripting.ScriptSource#isModified()
	 */
	protected boolean requiresRefresh() {
		return this.scriptSource.isModified();
	}

}
