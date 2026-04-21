package org.chenile.workflow.service.testmulti.store;

import org.chenile.utils.entity.service.EntityStore;
import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.chenile.workflow.service.testmulti.repo.VehicleRepository;

import java.util.Optional;
import java.util.UUID;

public class VehicleEntityStore implements EntityStore<Vehicle> {
	private final VehicleRepository vehicleRepository;

	public VehicleEntityStore(VehicleRepository vehicleRepository) {
		this.vehicleRepository = vehicleRepository;
	}

	@Override
	public void store(Vehicle entity) {
		if (entity.getId() == null) {
			entity.setId(UUID.randomUUID().toString());
		}
		vehicleRepository.save(entity);
	}

	@Override
	public Vehicle retrieve(String id) {
		Optional<Vehicle> vehicle = vehicleRepository.findById(id);
		return vehicle.orElse(null);
	}
}
