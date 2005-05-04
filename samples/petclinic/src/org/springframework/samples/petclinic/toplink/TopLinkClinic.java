
package org.springframework.samples.petclinic.toplink;

import java.util.Collection;

import oracle.toplink.expressions.ExpressionBuilder;
import oracle.toplink.queryframework.ReadAllQuery;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.toplink.support.TopLinkDaoSupport;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;

/**
 * Toplink implementation of the Clinic interface.
 *
 * <p>The mappings are defined in "toplink-mappings.xml";
 * session data is specified in "toplink-sessions.xml"
 * (both are located in the root of the class path).
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.2
 */
public class TopLinkClinic extends TopLinkDaoSupport implements Clinic {

	/** Prepared TopLink query object for the getVets method */
	private final ReadAllQuery getVetsQuery;

	/** Prepared TopLink query object for the getPetTypes method */
	private final ReadAllQuery getPetTypesQuery;

	/** Prepared TopLink query object for the findOwners method */
	private final ReadAllQuery findOwnersQuery;

	public TopLinkClinic() {
		// Prepare TopLink query object for the getVets method.
		this.getVetsQuery = new ReadAllQuery(Vet.class);
		this.getVetsQuery.addAscendingOrdering("lastName");
		this.getVetsQuery.addAscendingOrdering("firstName");
		this.getVetsQuery.conformResultsInUnitOfWork();

		// Prepare TopLink query object for the getPetTypes method.
		this.getPetTypesQuery = new ReadAllQuery(PetType.class);
		this.getPetTypesQuery.addOrdering(
				this.getPetTypesQuery.getExpressionBuilder().get("name").ascending());
		this.getPetTypesQuery.conformResultsInUnitOfWork();

		// Prepare TopLink query object for the findOwners method.
		this.findOwnersQuery = new ReadAllQuery(Owner.class);
		this.findOwnersQuery.addArgument("LastName");
		ExpressionBuilder builder = this.findOwnersQuery.getExpressionBuilder();
		this.findOwnersQuery.setSelectionCriteria(
				builder.get("lastName").like(builder.getParameter("LastName")));
		this.findOwnersQuery.conformResultsInUnitOfWork();
	}

	/**
	 * Return all Vet objects from the shared cache.
	 */
	public Collection getVets() throws DataAccessException {
		return (Collection) getTopLinkTemplate().executeQuery(this.getVetsQuery);
	}

	/**
	 * Return all PetType objects from the shared cache.
	 */
	public Collection getPetTypes() throws DataAccessException {
		return (Collection) getTopLinkTemplate().executeQuery(this.getPetTypesQuery);
	}

	/**
	 * Return a set of Owner objects from the shared cache.
	 * Uses a "LASTNAME LIKE arg%" query.
	 */
	public Collection findOwners(final String lastName) throws DataAccessException {
		return (Collection) getTopLinkTemplate().executeQuery(
				this.findOwnersQuery, new Object[] {lastName + "%"});
	}

	/**
	 * Return a copy of the specified Owner object.
	 */
	public Owner loadOwner(int id) throws DataAccessException {
		return (Owner) getTopLinkTemplate().readAndCopy(Owner.class, new Integer(id));
	}

	/**
	 * Return a copy of the specified Pet object.
	 */
	public Pet loadPet(int id) throws DataAccessException {
		return (Pet) getTopLinkTemplate().readAndCopy(Pet.class, new Integer(id));
	}

	/**
	 * Merge the given Owner object into the current UnitOfWork.
	 */
	public void storeOwner(Owner owner) throws DataAccessException {
		// Note: TopLink's merge operation does not reassociate the object with the
		// current TopLink Session. Instead, it will always copy the state over to
		// a registered representation of the entity. In case of a new entity, it will
		// register a copy as well, but will also update the id of the passed-in object.
		getTopLinkTemplate().deepMerge(owner);
	}

	/**
	 * Merge the given Pet object into the current UnitOfWork.
	 */
	public void storePet(Pet pet) throws DataAccessException {
		getTopLinkTemplate().deepMerge(pet);
	}

	/**
	 * Merge the given Visit object into the current UnitOfWork.
	 */
	public void storeVisit(Visit visit) throws DataAccessException {
		getTopLinkTemplate().deepMerge(visit);
	}

}
