-- Apply this through the application's schema migration tool. The runtime needs SELECT only.
-- Mapper XML is global. scope_key is '__base__' for shared definitions or the exact Chenile tenant id for an override.
create table if not exists chenile_query_mapper_source (
    namespace varchar(512) not null,
    mapper_xml text not null,
    enabled boolean not null default true,
    revision bigint not null default 1,
    checksum varchar(64) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    primary key (namespace)
);

create table if not exists chenile_query_definition_source (
    scope_key varchar(255) not null,
    query_name varchar(255) not null,
    definition_json text not null,
    enabled boolean not null default true,
    revision bigint not null default 1,
    checksum varchar(64) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    primary key (scope_key, query_name)
);
