package org.springframework.samples.petclinic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.PropertyComparator;
import org.springframework.beans.MutableSortDefinition;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Owner extends Person {

	private Set pets;

	protected void setPetsInternal(Set pets) {
		this.pets = pets;
	}

	protected Set getPetsInternal() {
		if (this.pets == null) {
			this.pets = new HashSet();
		}
		return this.pets;
	}

	public List getPets() {
		List sortedPets = new ArrayList(getPetsInternal());
		PropertyComparator.sort(sortedPets, new MutableSortDefinition("name", true, true));
		return Collections.unmodifiableList(sortedPets);
	}

	/** Method to add a pet to the List of pets.
	 * @param pet New pet to be added to the List of pets
	 */
	public void addPet(Pet pet) {
		getPetsInternal().add(pet);
		pet.setOwner(this);
	}

	/** Method to test whether an <code>Owner</code> already has
	 * a <code>Pet</code> with a particular name (case-insensitive).
	 * @param name to test
	 * @return true if pet name is already in use
	 */
	public Pet getPet(String name) {
		return (Pet) NamedEntity.getByName(getPetsInternal(), Pet.class, name, true);
	}

}
