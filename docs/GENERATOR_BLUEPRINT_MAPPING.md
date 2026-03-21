# Generator Blueprint Mapping

This document explains how the code-generator blueprints in the generator repository
`chenile-gen/jgen/bp-*` generate code that depends on the libraries in this repository.

## Why this matters

This repository provides reusable runtime libraries and tools.

The `chenile-gen` blueprints do not reimplement query or workflow infrastructure from scratch.
Instead, they generate application code that imports and wires the libraries from this repository.

## Direct consumers of these libraries

The main generator blueprints that directly consume modules from this repository are:

- `bp-mybatisQuery`
- `bp-wfservice`
- `bp-wfcustom`

Other blueprints in `chenile-gen` are more general-purpose and do not directly reference these query and workflow blueprint libraries.

## Query blueprint mapping

### Source generator

- `chenile-gen/jgen/bp-mybatisQuery`

### What it generates

This blueprint generates a query test/service project around MyBatis query definitions.

The generated project depends on `chenile-query-controller`, which pulls in the query blueprint runtime.

### Dependency usage

The generated POM includes:

- `org.chenile:chenile-query-controller`

That dependency gives the generated project:

- the HTTP query controller
- the Spring query configuration
- the MyBatis-backed query service
- the shared query API types

### Generated inputs for the query runtime

The blueprint emits:

- MyBatis mapper XML
- query metadata JSON
- test Spring Boot configuration
- test `application.yml`

These are exactly the inputs expected by the query blueprint libraries in this repository.

### How the pieces line up

The generated `application.yml` populates:

- `query.mapperFiles`
- `query.definitionFiles`

Those are the same properties consumed by `QueryConfiguration` in this repository.

The generated JSON file defines `QueryMetadata`.
The generated XML file defines MyBatis queries and count queries.
At runtime, `QueryDefinitions` reads the JSON and `NamedQueryServiceSpringMybatisImpl` executes the MyBatis mapper.

### Practical flow

`bp-mybatisQuery` generates metadata and mapper files.
`chenile-query-controller` and `chenile-query-service` provide the reusable runtime that loads and executes them.

## Workflow blueprint mapping

### Source generators

- `chenile-gen/jgen/bp-wfservice`
- `chenile-gen/jgen/bp-wfcustom`

### What they generate

These blueprints generate workflow-enabled services backed by Chenile STM.

They generate:

- an API module
- a service module
- workflow XML definitions
- Spring configuration for STM wiring
- REST controllers
- sample transition actions
- optional enablement, activity, auto-state, and post-save-hook support

### API dependency usage

The generated API POM depends on:

- `workflow-api`

That gives the generated model module the shared workflow types such as:

- `StateEntity`
- `StateEntityService`
- `StateEntityServiceResponse`

### Service dependency usage

The generated service POM depends on:

- `workflow-service`
- optionally `cucumber-workflow-utils`
- the Maven plugin `stm-generate-puml`

This is the key handoff from generated code to reusable blueprint runtime.

### How generated configuration uses `workflow-service`

The generated Spring configuration creates:

- `STMFlowStoreImpl`
- `STM`
- `STMActionsInfoProvider`
- `EntityStore`
- `StateEntityServiceImpl`
- `STMTransitionActionResolver`
- `StmBodyTypeSelector`
- `BaseTransitionAction`
- optionally `ActivityChecker`
- optionally `AreActivitiesComplete`
- optionally `DefaultPostSaveHook`
- optionally `DefaultAutomaticStateComputation`

These are all classes provided by the workflow libraries in this repository.

The generator does not replace the workflow engine.
It generates the glue code that instantiates and configures the workflow engine for a specific domain service.

### How generated controllers use `workflow-api` and `workflow-service`

The generated controllers expose REST endpoints like:

- `GET /{service}/{id}`
- `POST /{service}`
- `PATCH /{service}/{id}/{eventID}`

They return `StateEntityServiceResponse<DomainEntity>` and target a generated bean named like `_serviceStateEntityService_`.

That bean is created using `StateEntityServiceImpl` from this repository.

### Convention-based action binding

The generated workflow configurations rely on `STMTransitionActionResolver`.

This means the generator emits Spring bean methods whose names follow the conventions expected by the workflow runtime.
That is how event-specific transition actions become discoverable without extra manual wiring.

`bp-wfcustom` leans on this even more heavily by generating actions, post-save hooks, and auto-state beans from the supplied workflow XML.

## Tooling mapping

### `cucumber-workflow-utils`

When enablement is turned on in the workflow blueprints, the generated service adds:

- `org.chenile:cucumber-workflow-utils`

That supports BDD tests that dynamically enable or disable states, transitions, and activities.

### `stm-generate-puml`

The generated workflow service also configures:

- `org.chenile:stm-generate-puml`

This plugin scans generated workflow XML resources and produces PlantUML files during the Maven build.

The generated build often follows this with `plantuml-maven-plugin` to render PNG and SVG diagrams from the generated `.puml` files.

### `workflow-utils`

The workflow generator does not normally depend on `workflow-utils` directly in the generated service POM.
Instead, it consumes `workflow-utils` indirectly through the `stm-generate-puml` plugin, because that plugin uses the workflow CLI/helper code from this repository.

## End-to-end examples

### Example 1: Generated query service

1. Run the `mybatisQuery` generator.
2. It creates query metadata JSON and MyBatis XML.
3. The generated test app pulls in `chenile-query-controller`.
4. `QueryConfiguration` loads the generated JSON and XML.
5. `AbstractSearchServiceImpl` interprets the metadata.
6. `NamedQueryServiceSpringMybatisImpl` executes the generated MyBatis query.

### Example 2: Generated workflow service

1. Run the `wfservice` generator.
2. It creates a domain model, STM XML, controllers, and configuration classes.
3. The API module depends on `workflow-api`.
4. The service module depends on `workflow-service`.
5. The generated configuration instantiates `StateEntityServiceImpl` and related STM components.
6. Generated transition action beans are resolved by `STMTransitionActionResolver`.
7. The generated controller exposes create, retrieve, and event-processing endpoints.

### Example 3: Generated custom workflow from XML

1. Run the `wfcustom` generator with an input workflow XML file.
2. It generates service wiring plus action classes derived from workflow structure.
3. The service module depends on `workflow-service`.
4. Optional post-save hooks and auto-state beans are generated to match workflow states.
5. The generated build uses `stm-generate-puml` to produce diagrams from the generated workflow resources.

## Summary

The clean separation is:

- this repository provides reusable runtime libraries and tooling
- `chenile-gen` provides project templates that instantiate those libraries for a specific domain

In other words, the blueprints in `chenile-gen` generate application code that is intentionally thin on infrastructure because this repository already supplies the infrastructure.
