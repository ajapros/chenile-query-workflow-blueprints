---
name: chenile-query-workflow-blueprints
description: Use when working in this repository or explaining how Chenile query, workflow, and generator blueprints fit together; covers module layout, `workflow-service` STM command conventions, query runtime wiring, and how neighboring `chenile-gen` blueprints generate code against these libraries.
---

# Chenile Query Workflow Blueprints

## Overview

Use this skill when the task is about this repository's architecture, query and workflow blueprint libraries, the `workflow-service` `stmcmds` package, or how code generators in the neighboring `chenile-gen` repository use these modules.

This skill is optimized for:

- explaining the repo overall
- tracing query request flow
- tracing workflow event flow
- modifying or reviewing `workflow-service` behavior
- understanding `STMTransitionActionResolver`, `StmBodyTypeSelector`, and related classes
- mapping `chenile-gen/jgen/bp-*` templates to this repo's libraries

## First Reads

Start with the smallest doc that answers the question:

- repo overview: [`../../docs/REPO_OVERVIEW.md`](../../docs/REPO_OVERVIEW.md)
- generator mapping: [`../../docs/GENERATOR_BLUEPRINT_MAPPING.md`](../../docs/GENERATOR_BLUEPRINT_MAPPING.md)
- workflow `stmcmds` package: [`../../docs/WORKFLOW_STMCMDS_GUIDE.md`](../../docs/WORKFLOW_STMCMDS_GUIDE.md)

Then read code only in the module involved.

## Module Map

- `query-api`
  Shared query contracts and common query-processing logic.
- `chenile-query-service`
  Spring and MyBatis query runtime.
- `chenile-query-controller`
  HTTP entry point for query execution.
- `workflow-api`
  Workflow service contracts and DTOs.
- `workflow-service`
  Generic workflow runtime built on Chenile STM.
- `workflow-utils`
  CLI and workflow visualization or test-case tooling.
- `cucumber-workflow-utils`
  Test helpers for workflow enablement and activity scenarios.
- `stm-generate-puml`
  Maven plugin that generates PlantUML from workflow XML.

## Task Routing

### Explain the repo overall

Read:

- [`../../docs/REPO_OVERVIEW.md`](../../docs/REPO_OVERVIEW.md)
- [`../../pom.xml`](../../pom.xml)
- [`../../README.md`](../../README.md)

Then open only the modules mentioned in the user's question.

### Query runtime questions

Read in this order:

- [`../../query-api/src/main/java/org/chenile/query/model/SearchRequest.java`](../../query-api/src/main/java/org/chenile/query/model/SearchRequest.java)
- [`../../query-api/src/main/java/org/chenile/query/model/QueryMetadata.java`](../../query-api/src/main/java/org/chenile/query/model/QueryMetadata.java)
- [`../../query-api/src/main/java/org/chenile/query/service/AbstractSearchServiceImpl.java`](../../query-api/src/main/java/org/chenile/query/service/AbstractSearchServiceImpl.java)
- [`../../chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryConfiguration.java`](../../chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryConfiguration.java)
- [`../../chenile-query-service/src/main/java/org/chenile/query/service/impl/NamedQueryServiceSpringMybatisImpl.java`](../../chenile-query-service/src/main/java/org/chenile/query/service/impl/NamedQueryServiceSpringMybatisImpl.java)
- [`../../chenile-query-controller/src/main/java/org/chenile/configuration/controller/QueryController.java`](../../chenile-query-controller/src/main/java/org/chenile/configuration/controller/QueryController.java)

Focus on:

- how `QueryMetadata` drives filtering and sorting
- how `query.mapperFiles` and `query.definitionFiles` are wired
- how workflow actions are attached to query rows

### Workflow runtime questions

Read in this order:

- [`../../workflow-api/src/main/java/org/chenile/workflow/api/StateEntityService.java`](../../workflow-api/src/main/java/org/chenile/workflow/api/StateEntityService.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/impl/StateEntityServiceImpl.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/impl/StateEntityServiceImpl.java)
- [`../../docs/WORKFLOW_STMCMDS_GUIDE.md`](../../docs/WORKFLOW_STMCMDS_GUIDE.md)
- then only the specific `stmcmds` classes relevant to the question

### `stmcmds` package questions

Read:

- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/STMTransitionActionResolver.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/STMTransitionActionResolver.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/BaseTransitionAction.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/BaseTransitionAction.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/AbstractSTMTransitionAction.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/AbstractSTMTransitionAction.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/StmBodyTypeSelector.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/StmBodyTypeSelector.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/GenericEntryAction.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/GenericEntryAction.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/DefaultPostSaveHook.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/DefaultPostSaveHook.java)
- [`../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/DefaultAutomaticStateComputation.java`](../../workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/DefaultAutomaticStateComputation.java)

Explain the package as a system:

- controller chooses payload type
- STM runs
- default transition action dispatches
- resolver finds event-specific bean by convention
- entry action persists
- post-save and auto-state hooks extend the same convention model

### Generator blueprint mapping questions

Read:

- [`../../docs/GENERATOR_BLUEPRINT_MAPPING.md`](../../docs/GENERATOR_BLUEPRINT_MAPPING.md)
- neighboring templates under `~/Documents/framework/chenile-gen/jgen/bp-mybatisQuery`
- neighboring templates under `~/Documents/framework/chenile-gen/jgen/bp-wfservice`
- neighboring templates under `~/Documents/framework/chenile-gen/jgen/bp-wfcustom`

Focus on:

- generated POM dependencies
- generated Spring configuration
- generated controllers
- generated workflow XML and PlantUML setup

## Conventions To Preserve

- Do not break the naming conventions around `STMTransitionActionResolver`.
- When changing `StmBodyTypeSelector`, check every `getConfigs()` consumer.
- When changing workflow wiring, check both runtime classes and generator templates in the neighboring `chenile-gen` repo.
- When changing query configuration keys, verify generated `mybatisQuery` templates still match.
- Prefer explaining request flow end-to-end instead of describing one class in isolation.

## Verification

For `workflow-service` changes, prefer:

```bash
mvn -pl workflow-service -am -DskipTests compile
```

For query-side changes, compile the affected module and any dependent controller module.

When changing shared conventions, also search:

```bash
rg -n "STMTransitionActionResolver|StmBodyTypeSelector|getConfigs\\(|query\\.mapperFiles|query\\.definitionFiles" .
```

## Expected Outputs

Good answers using this skill usually include:

- the small set of files that matter
- the runtime flow between those files
- the naming convention or configuration contract involved
- any generator-template impact when the change affects shared conventions
