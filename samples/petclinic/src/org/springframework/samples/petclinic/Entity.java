package org.springframework.samples.petclinic;

import java.util.Collection;
import java.util.Iterator;

/**
 * Simple JavaBean domain object with an id property.
 * Used as a base class for objects needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Entity {

	private long id = -1;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public boolean isNew() {
		return (this.id == -1);
	}

	/**
	 * Look up the entity of the given class with the given id
	 * in the given collection.
	 * @param entities the collection to search
	 * @param entityClass the entity class to look up
	 * @param entityId the entity id to look up
	 * @return the found entity
	 * @throws NoSuchEntityException if the specified entity was not found
	 */
	public static Entity getById(Collection entities, Class entityClass, long entityId) throws NoSuchEntityException {
		for (Iterator it = entities.iterator(); it.hasNext();) {
			Entity entity = (Entity) it.next();
			if (entity.getId() == entityId && (entityClass == null || entityClass.isInstance(entity))) {
				return entity;
			}
		}
		throw new NoSuchEntityException(entityClass, entityId);
	}

}
