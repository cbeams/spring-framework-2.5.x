package org.springframework.orm.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="DRIVERS_LICENSE")
public class DriversLicense {
	
	@Id
	private int id;
	
	private String serial_number;
	
	protected DriversLicense() {
		
	}
	
	public DriversLicense(String serialNumber) {
		this.serial_number = serialNumber;
	}

	public String getSerialNumber() {
		return serial_number;
	}

}
