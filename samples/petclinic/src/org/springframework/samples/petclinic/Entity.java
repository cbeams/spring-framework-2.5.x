package org.springframework.samples.petclinic;

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

}
