package org.springframework.samples.petclinic.jdbc;

import org.springframework.samples.petclinic.Pet;

/**
 * @author Juergen Hoeller
 */
public class JdbcPet extends Pet {

	private long typeId;

	private long ownerId;

	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	public long getTypeId() {
		return this.typeId;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public long getOwnerId() {
		return ownerId;
	}



}
