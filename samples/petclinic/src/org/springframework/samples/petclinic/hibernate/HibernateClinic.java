package org.springframework.samples.petclinic.hibernate;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.Visit;

/**
 * Hibernate implementation of the Clinic interface.
 * The mappings are defined in "petclinic.hbm.xml", located in this package.
 *
 * @author Juergen Hoeller
 * @since 19.10.2003
 */
public class HibernateClinic extends HibernateDaoSupport implements Clinic {

	public List getVets() throws DataAccessException {
		return getHibernateTemplate().find("from Vet vet order by vet.lastName, vet.firstName");
	}

	public List getPetTypes() throws DataAccessException {
		return getHibernateTemplate().find("from PetType type order by type.name");
	}

	public List findOwners(String lastName) throws DataAccessException {
		return getHibernateTemplate().find("from Owner owner where owner.lastName like ?", lastName + "%");
	}

	public Owner loadOwner(long id)  throws DataAccessException {
		return (Owner) getHibernateTemplate().load(Owner.class, new Long(id));
	}

	public Pet loadPet(long id) throws DataAccessException {
		return (Pet) getHibernateTemplate().load(Pet.class, new Long(id));
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		getHibernateTemplate().saveOrUpdate(owner);
	}

	public void storePet(Pet pet) throws DataAccessException {
		getHibernateTemplate().saveOrUpdate(pet);
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		getHibernateTemplate().saveOrUpdate(visit);
	}

}
