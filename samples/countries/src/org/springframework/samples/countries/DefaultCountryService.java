package org.springframework.samples.countries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public class DefaultCountryService implements CountryService, InitializingBean {

	private final Map countriesLists = new HashMap();

	private final Map countriesMaps = new HashMap();


	public void afterPropertiesSet() {
		String countries[] = Locale.getISOCountries();
		String langs[] = {"fr", "en", "de"};
		for (int langId = 0; langId < langs.length; langId++) {
			String lang = langs[langId];
			Locale crtLoc = new Locale(lang, "");
			List list = new ArrayList();
			Map map = new HashMap();
			Country country = null;
			for (int j = 0; j < countries.length; j++) {
				String countryCode = countries[j];
				Locale countryLoc = new Locale("en", countryCode);
				String name = countryLoc.getDisplayCountry(crtLoc);
				if (!name.equals(countryCode)) {
					country = new Country(countryCode, name);
					list.add(country);
					map.put(countryCode, country);
				}
			}
			this.countriesLists.put(lang, list);
			this.countriesMaps.put(lang, map);
		}
	}


	public List getAllCountries(Locale locale) {
		List countries = (List) this.countriesLists.get(getLanguage(locale));
		if (countries == null) {
			countries = (List) this.countriesLists.get(Locale.getDefault().getLanguage());
		}
		return countries;
	}

	public List getFilteredCountries(String name, String code, Locale locale) {
		List allCountries = getAllCountries(locale);
		List countries = new ArrayList();
		Iterator it = allCountries.iterator();
		while (it.hasNext()) {
			Country country = (Country) it.next();
			if ((name == null || country.getName().startsWith(name)) &&
					(code == null || country.getCode().startsWith(code))) {
				countries.add(country);
			}
		}
		return countries;
	}

	public Country getCountry(String code, Locale locale) {
		return (Country) ((Map) this.countriesMaps.get(getLanguage(locale))).get(code);
	}

	private String getLanguage(Locale locale) {
		if (locale != null) {
			return locale.getLanguage();
		}
		return Locale.getDefault().getLanguage();
	}

}

