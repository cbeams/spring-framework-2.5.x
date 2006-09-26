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

package org.springframework.beans.factory.support;

import java.util.Properties;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.util.Assert;

/**
 * Represents a Spring-managed {@link Properties} instance that supports
 * merging of parent/child definitions.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class ManagedProperties extends Properties implements Mergeable, BeanMetadataElement {

	private boolean mergeEnabled;

	private Object source;


	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	public boolean isMergeEnabled() {
		return mergeEnabled;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}


	public synchronized Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Cannot merge when the mergeEnabled property is false");
		}
		Assert.notNull(parent);
		if (parent instanceof Properties) {
			Properties temp = new ManagedProperties();
			temp.putAll((Properties) parent);
			temp.putAll(this);
			return temp;
		}
		throw new IllegalArgumentException("Cannot merge object with object of type [" + parent.getClass() + "]");
	}

}
