package org.springframework.beans;

/**
 * Mutable implementation of SortDefinition.
 * Supports toggling the ascending value on setting the same property again.
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 26.05.2003
 * @see #setToggleAscendingOnProperty
 */
public class MutableSortDefinition implements SortDefinition {

	public static final String NO_CHANGE = "nochange";

	private String property = "";

	private boolean ignoreCase = true;

	private boolean ascending = true;

	private boolean toggleAscendingOnProperty = false;

	public MutableSortDefinition() {
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
	 * Sets the sort property.
	 * If the property was the same as the current, the sort is reversed if
	 * toggleAscendingOnProperty is activated, else simply ignored.
	 * <br>If the property equals <code>NO_CHANGE</code>, nothing is done.
	 * In web applications, this allows having a form element managing the property. 
	 */
	public void setProperty(String property) {
		// If NO_CHANGE does nothing
		if (NO_CHANGE.equals(property)) {
			return;
		}
		if (property == null || "".equals(property)) {
			this.property = "";
		}
		else {
			// implicit toggling of ascending?
			if (this.toggleAscendingOnProperty) {
				if (property.equals(this.property)) {
					ascending = !ascending;
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
