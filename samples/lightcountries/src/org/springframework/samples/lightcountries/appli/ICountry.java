package org.springframework.samples.lightcountries.appli;

/**
 * @author Jean-Pierre PAWLAK
 */
public interface ICountry {

	//~ Methods ----------------------------------------------------------------

	public String getName();

	public String getCode();

	public void setName(String nom);

	public void setCode(String code);
}
