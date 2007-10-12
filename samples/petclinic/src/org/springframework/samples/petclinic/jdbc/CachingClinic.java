package org.springframework.samples.petclinic.jdbc;

/**
 * Interface that defines a cache refresh operation.
 * To be exposed for management via JMX.
 * 
 * @author Rob Harrop
 * @since 1.2
 * @see SimpleJdbcClinic
 */
public interface CachingClinic {

	/**
	 * Refresh the cache of Vets that the Clinic is holding.
	 * @see org.springframework.samples.petclinic.Clinic#getVets()
	 * @see SimpleJdbcClinic#refreshVetsCache()
	 */
	void refreshVetsCache();

}
