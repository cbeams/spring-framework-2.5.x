package org.springframework.samples.petclinic.ojb;

import java.util.Collection;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.ojb.support.PersistenceBrokerDaoSupport;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;

/**
 * OJB PersistenceBroker implementation of the Clinic interface.
 *
 * <p>The mappings are defined in "OJB-repository.xml",
 * located in the root of the classpath.
 *
 * @author Juergen Hoeller
 * @since 04.07.2004
 */
public class PersistenceBrokerClinic extends PersistenceBrokerDaoSupport implements Clinic {

	public Collection getVets() throws DataAccessException {
		return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(Vet.class));
	}

	public Collection getPetTypes() throws DataAccessException {
		return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(PetType.class));
	}

	public Collection findOwners(String lastName) throws DataAccessException {
		Criteria criteria = new Criteria();
		criteria.addLike("lastName", lastName + "%");
		return getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(Owner.class, criteria));
	}

	public Owner loadOwner(int id) throws DataAccessException {
		Criteria criteria = new Criteria();
		criteria.addLike("id", Integer.toString(id));
		Owner owner = (Owner) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(Owner.class, criteria));
		if (owner == null) {
			throw new ObjectRetrievalFailureException(Owner.class, new Integer(id));
		}
		return owner;
	}

	public Pet loadPet(int id) throws DataAccessException {
		Criteria criteria = new Criteria();
		criteria.addLike("id", Integer.toString(id));
		Pet pet = (Pet) getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(Pet.class, criteria));
		if (pet == null) {
			throw new ObjectRetrievalFailureException(Pet.class, new Integer(id));
		}
		return pet;
	}

	public void storeOwner(Owner owner) throws DataAccessException {
		getPersistenceBrokerTemplate().store(owner);
	}

	public void storePet(Pet pet) throws DataAccessException {
		getPersistenceBrokerTemplate().store(pet);
	}

	public void storeVisit(Visit visit) {
		getPersistenceBrokerTemplate().store(visit);
	}

}
