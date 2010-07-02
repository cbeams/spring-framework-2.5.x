package org.springframework.samples.countries;

import java.util.List;
import java.util.Locale;

/**
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public interface CountryService {

	/**
	 * Provides the list of all ICountries in the given Locale.
	 * @param locale The Locale for which the records are intended
	 * @return The list of all countries in the given Locale
	 */
	List getAllCountries(Locale locale);

	/**
	 * Provides a list of ICountries in the given Locale with name and code
	 * starting with the corresponding parameters. A null or empty String 
	 * in the parameter is interpreted as no filtering on that parameter.
	 * @param name beginning of the name of the requested records
	 * @param code beginning of the code of the requested records
	 * @param locale the Locale for which the records are intended
	 * @return the list of Country objects matching with parameters in the given Locale
	 */
	List getFilteredCountries(String name, String code, Locale locale);

	/**
	 * Get a Country object following the parameters.
	 * @param code the code of the requested country
	 * @param locale the locale to use for searching the country
	 * @return the Country object corresponding on the parameters
	 */
	Country getCountry(String code, Locale locale);

}
