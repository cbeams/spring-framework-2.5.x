package org.springframework.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple nested test bean used for testing bean factories, AOP framework etc.
 * @author  Trevor D. Cook
 * @since 30-Sep-2003
 */
public class NestedTestBean implements INestedTestBean {

	String company = "";

	public NestedTestBean() {
	}

	public NestedTestBean(String company) {
		setCompany(company);
	}

	/**
	 * Get the company Setter for property age.
	 * 
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * Set the company
	 * 
	 * @param company the company
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof NestedTestBean)) {
			return false;
		}
		NestedTestBean ntb = (NestedTestBean) obj;
		return new EqualsBuilder().append(company, ntb.company).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(23, 91).append(company).toHashCode();
	}

}
