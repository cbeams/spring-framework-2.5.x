/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.value.support;

import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.closure.Closure;

/**
 * @author Keith Donald
 */
public class RefreshableValueHolder extends ValueHolder {
	private Closure refreshFunction;

	private boolean alwaysRefresh;

	public RefreshableValueHolder(Closure refreshFunction) {
		this(refreshFunction, false);
	}

	public RefreshableValueHolder(Closure refreshFunction, boolean alwaysRefresh) {
		this(refreshFunction, alwaysRefresh, true);
	}

	public RefreshableValueHolder(Closure refreshFunction, boolean alwaysRefresh, boolean lazyInit) {
		super();
		Assert.notNull(refreshFunction, "The refresh callback cannot be null");
		this.refreshFunction = refreshFunction;
		this.alwaysRefresh = alwaysRefresh;
		if (!lazyInit) {
			refreshInternal();
		}
	}

	public Object getValue() {
		if (alwaysRefresh) {
			refreshInternal();
		}
		return super.getValue();
	}

	private void refreshInternal() {
		if (logger.isDebugEnabled()) {
			logger.debug("Autorefreshing held value '" + DefaultObjectStyler.call(super.getValue()) + "'");
		}
		setValueInternal(refreshFunction.call(null));
	}
	
	public void refresh() {
		if (logger.isDebugEnabled()) {
			logger.debug("Refreshing held value '" + DefaultObjectStyler.call(super.getValue()) + "'");
		}
		setValue(refreshFunction.call(null));
	}
}