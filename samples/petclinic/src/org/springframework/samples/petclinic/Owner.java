package org.springframework.samples.petclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

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

	/**
	 * Add a pet to the List of pets.
	 * @param pet New pet to be added to the List of pets
	 */
	public void addPet(Pet pet) {
		getPetsInternal().add(pet);
		pet.setOwner(this);
	}

	/**
	 * Return the Pet with the given name,
	 * or null if none found for this Owner.
	 * @param name to test
	 * @return true if pet name is already in use
	 */
	public Pet getPet(String name) {
		name = name.toLowerCase();
		for (Iterator it = getPetsInternal().iterator(); it.hasNext();) {
			Pet pet = (Pet) it.next();
			String compName = pet.getName();
			compName = compName.toLowerCase();
			if (compName.equals(name)) {
				return pet;
			}
		}
		return null;
	}

}
