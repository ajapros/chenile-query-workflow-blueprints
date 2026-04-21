package org.chenile.workflow.service.testmulti.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
	@Column(name = "garage_code")
	public String garageCode;

	@Column(name = "charging_slot")
	public String chargingSlot;

	public Car() {
		this.vehicleType = "CAR";
	}
}
