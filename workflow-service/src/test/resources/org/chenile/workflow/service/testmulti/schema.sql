drop table if exists vehicle_entity;

create table vehicle_entity (
    id varchar(64) primary key,
    vehicle_type varchar(16) not null,
    description varchar(255),
    current_state varchar(64),
    dispatch_comment varchar(255),
    completion_comment varchar(255),
    route_code varchar(64),
    seat_capacity integer,
    garage_code varchar(64),
    charging_slot varchar(64)
);
