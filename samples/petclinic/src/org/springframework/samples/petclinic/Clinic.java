package org.springframework.samples.petclinic;

import java.util.Collection;

import org.springframework.dao.DataAccessException;

/**
 * The high-level Petclinic business interface.
 * Basically a data access object, as Petclinic
 * doesn't have dedicated business logic.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public interface Clinic {

	/**
	 * Retrieve all <code>Vet</code>s from the datastore.
	 * @return a <code>List</code> of <code>Vet</code>s.
	 */
	public Collection getVets() throws DataAccessException;

	/**
	 * Retrieve all <code>PetType</code>s from the datastore.
	 * @return a <code>List</code> of <code>PetType</code>s.
	 */
	public Collection getPetTypes() throws DataAccessException;

	/**
	 * Retrieve <code>Owner</code>s from the datastore by last name,
	 * returning all owners whose last name <i>starts</i> with the given name.
	 * @param lastName Value to search for.
	 * @return a <code>List</code> of matching <code>Owner</code>s.
	 */
	public Collection findOwners(String lastName) throws DataAccessException;

	/**
	 * Retrieve an <code>Owner</code> from the datastore by id.
	 * @param id Value to search for.
	 * @return the <code>Owner</code> if found.
	 */
	public Owner loadOwner(int id) throws DataAccessException;

	/**
	 * Retrieve a <code>Pet</code> from the datastore by id.
	 * @param id Value to search for.
	 * @return the <code>Pet</code> if found.
	 */
	public Pet loadPet(int id) throws DataAccessException;

	/**
	 * Save an <code>Owner</code> to the datastore,
	 * either inserting or updating it.
	 * @param owner to add.
	 * @see Entity#isNew
	 */
	public void storeOwner(Owner owner) throws DataAccessException;

	/**
	 * Save a <code>Pet</code> to the datastore,
	 * either inserting or updating it.
	 * @param pet to add.
	 * @see Entity#isNew
	 */
	public void storePet(Pet pet) throws DataAccessException;

	/**
	 * Save a <code>Visit</code> to the datastore,
	 * either inserting or updating it.
	 * @param visit to add.
	 * @see Entity#isNew
	 */
	public void storeVisit(Visit visit);

}
