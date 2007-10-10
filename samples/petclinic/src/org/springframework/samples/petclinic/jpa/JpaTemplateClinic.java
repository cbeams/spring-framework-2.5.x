
package org.springframework.samples.petclinic.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the Clinic interface.
 * <p>
 * The mappings are defined in "orm.xml" located in the META-INF dir.
 *
 * @author Mike Keith
 * @author Sam Brannen
 * @since 22.04.2006
 */
@Transactional
public class JpaTemplateClinic extends JpaDaoSupport implements Clinic {

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection getVets() throws DataAccessException {
		return getJpaTemplate().find("SELECT vet FROM Vet vet ORDER BY vet.lastName, vet.firstName");
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection getPetTypes() throws DataAccessException {
		return getJpaTemplate().find("SELECT ptype FROM PetType ptype ORDER BY ptype.name");
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection findOwners(String lastName) throws DataAccessException {
		Map<String, String> map = new HashMap<String, String>();
		map.put("lastName", lastName + "%");
		return getJpaTemplate().findByNamedParams("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName",
				map);
	}

	@Transactional(readOnly = true)
	public Owner loadOwner(int id) throws DataAccessException {
		return getJpaTemplate().find(Owner.class, id);
	}

	@Transactional(readOnly = true)
	public Pet loadPet(int id) throws DataAccessException {
		return getJpaTemplate().find(Pet.class, id);
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		// consider using merge with returning the persistent object here
		getJpaTemplate().persist(owner);
	}

	public void storePet(Pet pet) throws DataAccessException {
		// consider using merge with returning the persistent object here
		getJpaTemplate().persist(pet);
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		// consider using merge with returning the persistent object here
		getJpaTemplate().persist(visit);
	}
}
