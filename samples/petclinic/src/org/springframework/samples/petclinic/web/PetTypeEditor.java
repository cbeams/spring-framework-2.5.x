
package org.springframework.samples.petclinic.web;

import java.beans.PropertyEditorSupport;
import java.util.Collection;

import org.springframework.samples.petclinic.PetType;

/**
 * @author Mark Fisher
 */
public class PetTypeEditor extends PropertyEditorSupport {

	private final Collection<PetType> petTypes;


	public PetTypeEditor(Collection<PetType> petTypes) {
		this.petTypes = petTypes;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		for (PetType type : petTypes) {
			if (type.getName().equals(text)) {
				setValue(type);
			}
		}
	}

}
