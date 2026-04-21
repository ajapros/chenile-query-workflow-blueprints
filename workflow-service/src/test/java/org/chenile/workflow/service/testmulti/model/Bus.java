package org.chenile.workflow.service.testmulti.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BUS")
public class Bus extends Vehicle {
	@Column(name = "route_code")
	public String routeCode;

	@Column(name = "seat_capacity")
	public Integer seatCapacity;

	public Bus() {
		this.vehicleType = "BUS";
	}
}
