package org.springframework.samples.countries.web;

/**
 * @author Jean-Pierre Pawlak
 */
public class CountriesFilter {

	private String name;
	private String code;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean equals(Object obj) {
		return (obj instanceof CountriesFilter ? equals((CountriesFilter) obj) : false);
	}

	public boolean equals(CountriesFilter cf) {
		if (cf == this) return true;
		boolean result = (name == null ? cf.name == null : name.equals(cf.name));
		if (result) {
			result = (code == null ? cf.code == null : code.equals(cf.code));
		}
		return result;
	}

	public int hashCode() {
		int hash = 17;
		hash = 37 * hash + (name == null ? 0 : name.hashCode());
		hash = 37 * hash + (code == null ? 0 : code.hashCode());
		return hash;
	}

}
