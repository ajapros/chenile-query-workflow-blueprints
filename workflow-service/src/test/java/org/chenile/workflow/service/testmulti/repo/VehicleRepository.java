package org.chenile.workflow.service.testmulti.repo;

import org.chenile.workflow.service.testmulti.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {
}
