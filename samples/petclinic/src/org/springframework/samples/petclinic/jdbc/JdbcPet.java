package org.springframework.samples.petclinic.jdbc;

import org.springframework.samples.petclinic.Pet;

/**
 * @author Juergen Hoeller
 */
public class JdbcPet extends Pet {

	private int typeId;

	private int ownerId;

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getTypeId() {
		return this.typeId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public int getOwnerId() {
		return ownerId;
	}

}
