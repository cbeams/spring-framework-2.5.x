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

package org.springframework.beans.support;

import java.io.Serializable;

/**
 * Mutable implementation of SortDefinition.
 * Supports toggling the ascending value on setting the same property again.
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 26.05.2003
 * @see #setToggleAscendingOnProperty
 */
public class MutableSortDefinition implements SortDefinition, Serializable {

	private String property = "";

	private boolean ignoreCase = true;

	private boolean ascending = true;

	private boolean toggleAscendingOnProperty = false;


	public MutableSortDefinition() {
	}

	public MutableSortDefinition(SortDefinition source) {
		this.property = source.getProperty();
		this.ignoreCase = source.isIgnoreCase();
		this.ascending = source.isAscending();
	}

	public MutableSortDefinition(String property, boolean ignoreCase, boolean ascending) {
		this.property = property;
		this.ignoreCase = ignoreCase;
		this.ascending = ascending;
	}

	public MutableSortDefinition(boolean toggleAscendingOnSameProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnSameProperty;
	}


	/**
	 * Set the sort property.
	 * If the property was the same as the current, the sort is reversed if
	 * "toggleAscendingOnProperty" is activated, else simply ignored.
	 */
	public void setProperty(String property) {
		if (property == null || "".equals(property)) {
			this.property = "";
		}
		else {
			// implicit toggling of ascending?
			if (this.toggleAscendingOnProperty) {
				if (property.equals(this.property)) {
					this.ascending = !this.ascending;
				}
				else {
					this.ascending = true;
				}
			}
			this.property = property;
		}
	}

	public String getProperty() {
		return property;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setToggleAscendingOnProperty(boolean toggleAscendingOnProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnProperty;
	}

	public boolean isToggleAscendingOnProperty() {
		return toggleAscendingOnProperty;
	}


	public boolean equals(Object obj) {
		if (!(obj instanceof SortDefinition)) {
			return false;
		}
		SortDefinition sd = (SortDefinition) obj;
		return (getProperty().equals(sd.getProperty()) &&
		    isAscending() == sd.isAscending() && isIgnoreCase() == sd.isIgnoreCase());
	}

	public int hashCode() {
		int result;
		result = this.property.hashCode();
		result = 29 * result + (this.ignoreCase ? 1 : 0);
		result = 29 * result + (this.ascending ? 1 : 0);
		return result;
	}

}
