package org.chenile.workflow.service.testmulti.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.chenile.stm.State;
import org.chenile.utils.entity.model.AbstractExtendedStateEntity;

@Entity
@Table(name = "vehicle_entity")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "vehicleType", visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = Bus.class, name = "BUS"),
		@JsonSubTypes.Type(value = Car.class, name = "CAR")
})
public abstract class Vehicle extends AbstractExtendedStateEntity {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "vehicle_type", insertable = false, updatable = false)
	public String vehicleType;

	@Column(name = "description")
	public String description;

	@Column(name = "dispatch_comment")
	public String dispatchComment;

	@Column(name = "completion_comment")
	public String completionComment;

	@Column(name = "current_state")
	private String currentStateId;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setCurrentState(State currentState) {
		this.currentStateId = currentState == null ? null : currentState.getStateId();
	}

	@Override
	public State getCurrentState() {
		return currentStateId == null ? null : new State(currentStateId, "VEHICLE_FLOW");
	}
}
