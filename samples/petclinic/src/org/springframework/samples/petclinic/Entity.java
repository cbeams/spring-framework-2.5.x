package org.springframework.samples.petclinic;

/**
 * Simple JavaBean domain object with an id property.
 * Used as a base class for objects needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class Entity {

	private Integer id;

	public void setId(int id) {
		this.id = new Integer(id);
	}

	public int getId() {
		return (this.id != null ? this.id.intValue() : -1);
	}

	public boolean isNew() {
		return (this.id == null);
	}

}
