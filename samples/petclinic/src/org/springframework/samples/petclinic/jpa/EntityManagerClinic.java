package org.springframework.samples.petclinic.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the Clinic interface using EntityManager.
 *
 * <p>The mappings are defined in "orm.xml"
 * located in the META-INF dir.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @since 22.4.2006
 */
@Repository
@Transactional
public class EntityManagerClinic implements Clinic {
	
	@PersistenceContext
	private EntityManager em;

	public Collection<Vet> getVets() throws DataAccessException {
		return em.createQuery("SELECT vet FROM Vet vet ORDER BY vet.lastName, vet.firstName").getResultList();
	}

	public Collection<PetType> getPetTypes() throws DataAccessException {
		return em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").getResultList();
	}

	public Collection<Owner> findOwners(String lastName) throws DataAccessException {
		Query query = em.createQuery("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName");
		query.setParameter("lastName", lastName + "%");
		return query.getResultList();
	}

	public Owner loadOwner(int id) throws DataAccessException {
		return em.find(Owner.class, id);	
	}

	public Pet loadPet(int id) throws DataAccessException {
		return em.find(Pet.class, id);
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		em.merge(owner);
	}

	public void storePet(Pet pet) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		em.merge(pet);
	}

	public void storeVisit(Visit visit) throws DataAccessException {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		em.merge(visit);
	}

}
