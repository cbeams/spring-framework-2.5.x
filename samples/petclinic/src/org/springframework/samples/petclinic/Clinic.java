package org.springframework.samples.petclinic;

import java.util.List;

/**
 *  The high-level Petclinic business interface.
 *
 *  @author  Ken Krebs
 */
public interface Clinic {

	/**
	 * Method to retrieve all <code>Vet</code>s from the datastore.
	 * @return a <code>List</code> of <code>Vet</code>s.
	 */
	public List getVets();

	/**
	 * Method to retrieve all <code>PetType</code>s from the datastore.
	 * @return a <code>List</code> of <code>PetType</code>s.
	 */
	public List getPetTypes();

	/**
	 * Method to retrieve <code>Owner</code>s from the datastore by last name.
	 * @param lastName Value to search for.
	 * @return a <code>List</code> of matching <code>Owner</code>s.
	 */
	public List findOwners(String lastName);

	/**
	 * Method to retrieve an <code>Owner</code> from the datastore by id.
	 * @param id Value to search for.
	 * @return the <code>Owner</code> if found.
	 */
	public Owner loadOwner(long id) throws NoSuchEntityException;

	/**
	 * Method to retrieve a <code>Pet</code> from the datastore by id.
	 * @param id Value to search for.
	 * @return the <code>Pet</code> if found.
	 */
	public Pet loadPet(long id) throws NoSuchEntityException;

	/**
	 * Method to add a new <code>Owner</code> to the datastore.
	 * @param owner to add.
	 */
	public void storeOwner(Owner owner);

	/**
	 * Method to add a new <code>Pet</code> to the datastore.
	 * @param pet to add.
	 */
	public void storePet(Pet pet);

	/**
	 * Method to add a new <code>Visit</code> to the datastore.
	 * @param visit to add.
	 */
	public void storeVisit(Visit visit);

}
