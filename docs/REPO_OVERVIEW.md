# Repository Overview

This repository contains reusable Chenile blueprints for two major capabilities:

- query services
- workflow and state-machine driven services

It is a multi-module Maven repository. The top-level parent POM aggregates reusable libraries and tools rather than implementing one deployable application by itself.

## Module Map

### Core modules

- `query-api`
  Defines the query contract and common query-processing logic.
  Key types include `SearchRequest`, `SearchResponse`, `QueryMetadata`, `SearchService`, and `AbstractSearchServiceImpl`.

- `chenile-query-service`
  Spring and MyBatis implementation of the query blueprint.
  It wires query definitions, mapper XML files, tenant-aware datasource routing, MyBatis sessions, and the `searchService` bean.

- `chenile-query-controller`
  HTTP controller layer for the query service.
  Exposes `POST /q/{queryName}` and delegates to the Chenile controller/service pipeline.

- `workflow-api`
  Defines the workflow-facing service contract for stateful entities.
  The central abstraction is `StateEntityService<T extends StateEntity>`.

- `workflow-service`
  Generic implementation for workflow-enabled entities using Chenile STM.
  It handles create, process, process-by-id, retrieve, allowed-action lookup, action resolution, activity helpers, and post-save hooks.

### Tooling and test-support modules

- `workflow-utils`
  CLI and helper utilities for working with workflow XML definitions.
  Supports rendering UML, listing allowed actions, converting XML to JSON, and generating or visualizing test cases.

- `cucumber-workflow-utils`
  Reusable Cucumber steps for workflow testing.
  These steps let tests dynamically enable or disable states and transitions, add metadata, and add activities.

- `stm-generate-puml`
  Maven plugin that scans workflow XML files and generates PlantUML output during the build.

### Auxiliary module

- `puml`
  Standalone Spring Boot application around PlantUML generation and visualization.
  This module is present in the repository but is not part of the top-level aggregated modules in the parent POM.

## Dependency Direction

The dependency flow is intentionally layered:

1. `workflow-api` is the low-level workflow contract.
2. `query-api` depends on `workflow-api` because query results can expose workflow actions.
3. `workflow-service` depends on `workflow-api`.
4. `chenile-query-service` depends on `query-api` and `workflow-api`.
5. `chenile-query-controller` depends on `chenile-query-service`.
6. `workflow-utils` depends on `workflow-service`.
7. `stm-generate-puml` depends on `workflow-utils`.

This gives the repository a clean split between:

- contracts and shared models
- runtime implementations
- developer tooling

## Query Request Flow

The query path is:

1. An HTTP client calls `POST /q/{queryName}` in `chenile-query-controller`.
2. `QueryController` delegates to the Chenile controller support layer.
3. The `searchService` bean from `chenile-query-service` receives the request.
4. `AbstractSearchServiceImpl` loads `QueryMetadata` for the external query name.
5. The request filters are normalized using column metadata.
   This includes support for:
   - like queries
   - contains queries
   - between queries
   - todo-list workflow filtering
   - custom variables
   - generated order-by clauses
6. `NamedQueryServiceSpringMybatisImpl` executes the MyBatis query.
7. If pagination is enabled, it first runs a count query.
8. Each result row is wrapped as a `ResponseRow`.
9. If the query is linked to a workflow, allowed actions are computed and attached to each row.
10. A `SearchResponse` is returned to the caller.

## Workflow Runtime Flow

The workflow path is:

1. An application creates a workflow-specific STM and an `EntityStore`.
2. `StateEntityServiceImpl` receives a create, process, or process-by-id call.
3. For create, it clears any incoming state so STM can assign the initial state.
4. For process-by-id, it loads the entity from the `EntityStore`.
5. It delegates the state transition to Chenile STM using `stm.proceed(...)`.
6. Workflow-specific transition handlers, post-save hooks, and auto-state computations are resolved using Spring naming conventions.
7. The service returns both:
   - the mutated entity
   - the allowed actions and metadata for the resulting state

This means the blueprint is designed so that application code mainly provides:

- the entity type
- the STM configuration
- persistence
- workflow-specific actions

The generic service handles the orchestration.

## Configuration Model

### Query blueprint configuration

The query blueprint expects:

- query definition files, typically JSON, that describe externally visible query names and metadata
- MyBatis mapper XML files
- one or more datasources under query configuration
- tenant context for datasource routing when multi-tenancy is used

### Workflow blueprint configuration

The workflow blueprint expects:

- workflow XML definitions for the STM
- Spring beans implementing transition actions or hooks where needed
- entity persistence via an `EntityStore`

## Conventions Used

- Query names are external, metadata-driven identifiers.
- Workflow actions are conventionally resolved from event names and workflow prefixes.
- The query layer can enrich results with workflow action metadata.
- Tooling works directly from workflow XML definitions, so documentation, tests, and diagrams can all be generated from the same source model.

## Recommended Reading Order

For a new contributor, this order gives the fastest understanding:

1. `README.md`
   High-level purpose of the repository.
2. Parent `pom.xml`
   See how the repository is divided into modules.
3. `workflow-api`
   Understand the service contract for workflow entities.
4. `query-api`
   Understand the query model and common query-processing logic.
5. `workflow-service`
   See how the workflow contract is implemented.
6. `chenile-query-service`
   See how query execution is wired into Spring and MyBatis.
7. `chenile-query-controller`
   See the web entry point.
8. `workflow-utils` and `stm-generate-puml`
   See the developer tooling around workflow definitions.

## Where To Start For Common Tasks

### Add or change a query

Start in:

- `chenile-query-service` for Spring wiring and metadata loading
- query definition JSON files used by `QueryDefinitions`
- MyBatis mapper XML files referenced by `query.mapperFiles`

Then confirm behavior in:

- `query-api` for filter handling
- `chenile-query-controller` if the HTTP surface matters

### Add or change a workflow

Start in:

- workflow XML definitions consumed by Chenile STM
- `workflow-service` for generic execution behavior
- application-specific transition action beans

Then check:

- `STMTransitionActionResolver` to understand bean naming conventions
- `workflow-utils` if you need diagrams or generated test cases

### Understand generated workflow diagrams and tests

Start in:

- `workflow-utils`
- `stm-generate-puml`
- `cucumber-workflow-utils`

## Practical Summary

This repository is best viewed as a blueprint library:

- `query-api` and `workflow-api` define reusable contracts
- `chenile-query-service` and `workflow-service` provide reusable runtime implementations
- `chenile-query-controller` exposes the query runtime over HTTP
- `workflow-utils`, `cucumber-workflow-utils`, and `stm-generate-puml` support visualization, testing, and developer workflow

Applications built on Chenile are expected to import these modules and supply domain-specific configuration, persistence, and action implementations on top.

## Related Generator Blueprints

See [GENERATOR_BLUEPRINT_MAPPING.md](/Users/rajashankarkolluru/Documents/framework/chenile-query-workflow-blueprints/docs/GENERATOR_BLUEPRINT_MAPPING.md) for how neighboring `chenile-gen` blueprints generate code that uses the libraries in this repository.

## Workflow Package Guide

See [WORKFLOW_STMCMDS_GUIDE.md](/Users/rajashankarkolluru/Documents/framework/chenile-query-workflow-blueprints/docs/WORKFLOW_STMCMDS_GUIDE.md) for a detailed explanation of the `workflow-service` `stmcmds` package and its naming conventions.
