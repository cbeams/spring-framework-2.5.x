package org.springframework.samples.petclinic;

import java.util.Collection;
import java.util.Iterator;

/**
 * Simple JavaBean domain object adds a name property to <code>Entity</code>.
 * Used as a base class for objects needing these properties.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class NamedEntity extends Entity {

	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Look up the entity of the given class with the given name
	 * in the given collection.
	 * @param entities the collection to search
	 * @param entityClass the entity class to look up
	 * @param entityName the entity name to look up
	 * @return the entity, or null if not found
	 */
	public static NamedEntity getByName(Collection entities, Class entityClass, String entityName, boolean ignoreCase) {
		if (ignoreCase) {
			entityName = entityName.toLowerCase();
		}
		for (Iterator it = entities.iterator(); it.hasNext();) {
			NamedEntity entity = (NamedEntity) it.next();
			String compName = entity.getName();
			if (ignoreCase) {
				compName = compName.toLowerCase();
			}
			if (compName.equals(entityName) && (entityClass == null || entityClass.isInstance(entity))) {
				return entity;
			}
		}
		return null;
	}

}
