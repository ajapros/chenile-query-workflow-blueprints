# Agent Traversal Guide

This guide is for coding agents working across the Chenile query/workflow framework and the Revyre client implementation layer.
Use it before broad repository searches.

## Roots

Framework root:

```text
/Users/gauravbhardwaj/work/ajapro/chenile-query-workflow-blueprints
```

Client implementation root:

```text
/Users/gauravbhardwaj/work/revyre-core/core
```

Client central-query module:

```text
/Users/gauravbhardwaj/work/revyre-core/core/central-query
```

## First Decision Table

| Problem area | Start here | Then check |
| --- | --- | --- |
| Query REST endpoint `/q/{queryName}` | `chenile-query-controller/src/main/java/org/chenile/configuration/controller/QueryController.java` | `chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryConfiguration.java` |
| Query execution, filters, count/no-count | `query-api/src/main/java/org/chenile/query/service/AbstractSearchServiceImpl.java` | `chenile-query-service/src/main/java/org/chenile/query/service/impl/NamedQueryServiceSpringMybatisImpl.java` |
| Query definitions JSON/XML loading | `chenile-query-service/src/main/java/org/chenile/query/service/impl/QueryDefinitions.java` | `chenile-query-service/src/test/java/org/chenile/query/service/impl/QueryDefinitionsTest.java` |
| Query MCP support | `query-mcp/src/main/java/org/chenile/configuration/query/mcp/QueryMCPController.java` | `query-mcp/src/main/java/org/chenile/query/service/impl/QueryPolymorph.java` |
| Workflow runtime behavior | `workflow-service/src/main/java/org/chenile/workflow/service/impl/StateEntityServiceImpl.java` | `workflow-service/src/main/java/org/chenile/workflow/service/stmcmds` |
| Workflow OpenAPI event endpoints | `workflow-service/src/main/java/org/chenile/workflow/service/config/ChenileOpenApiConfiguration.java` | `workflow-service/src/test/java/org/chenile/workflow/service/config/ChenileOpenApiConfigurationTest.java` |
| Workflow MCP support | `workflow-mcp/src/main/java/org/chenile/workflow/service/stmcmds/ProcessIdPolymorph.java` | `workflow-mcp/src/test/java/org/chenile/workflow/service/stmcmds/ProcessIdPolymorphTest.java` |
| Revyre central-query client API | `central-query/central-query-service/src/main/java/com/vymo/tech/query/service/query/CentralQueryServiceImpl.java` | `central-query/central-query-service/src/main/java/com/vymo/tech/query/configuration/controller/CQSController.java` |
| Revyre central-query request model | `central-query/central-query-api/src/main/java/com/vymo/tech/query/model/request/CentralQueryPayload.java` | `central-query/central-query-api/src/main/java/com/vymo/tech/query/model/request/builder` |
| Revyre query SQL mappers | `central-query/central-query-service/src/main/resources/com/vymo/tech/query/service/mapper` | `central-query/central-query-service/src/test/resources` |

## Framework Layers

### Query API

Path:

```text
query-api
```

Purpose:

- Query request/response model.
- Shared search-service contract.
- Common search flow in `AbstractSearchServiceImpl`.

Important files:

```text
query-api/src/main/java/org/chenile/query/model/SearchRequest.java
query-api/src/main/java/org/chenile/query/model/SearchResponse.java
query-api/src/main/java/org/chenile/query/model/QueryMetadata.java
query-api/src/main/java/org/chenile/query/service/SearchService.java
query-api/src/main/java/org/chenile/query/service/AbstractSearchServiceImpl.java
```

Use this layer when behavior is provider-neutral, such as pagination metadata, query metadata interpretation, filter normalization, or allowed-action handling.

### Query Service

Path:

```text
chenile-query-service
```

Purpose:

- Spring wiring for query runtime.
- MyBatis provider.
- Datasource routing.
- Query definition loading.

Important files:

```text
chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryConfiguration.java
chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryPaginationProperties.java
chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryDatasourcesProperties.java
chenile-query-service/src/main/java/org/chenile/query/service/impl/QueryDefinitions.java
chenile-query-service/src/main/java/org/chenile/query/service/impl/NamedQueryServiceSpringMybatisImpl.java
chenile-query-service/src/main/java/org/chenile/query/service/impl/MybatisQueryExecutionProvider.java
```

Important rule:

`chenile-query-service` must not depend on `chenile-mcp`.
MCP types in `QueryConfiguration` method signatures can break Spring introspection for apps that exclude MCP.

Regression test:

```text
chenile-query-service/src/test/java/org/chenile/configuration/query/service/QueryConfigurationIntrospectionTest.java
```

### Query Controller

Path:

```text
chenile-query-controller
```

Purpose:

- Base HTTP query endpoint.
- No MCP imports or annotations.

Important file:

```text
chenile-query-controller/src/main/java/org/chenile/configuration/controller/QueryController.java
```

Important rule:

The base controller must not import `org.chenile.mcp.*`.
MCP annotations belong in `query-mcp`.

Regression test:

```text
chenile-query-controller/src/test/java/org/chenile/configuration/controller/QueryControllerMcpAnnotationTest.java
```

### Query MCP

Path:

```text
query-mcp
```

Purpose:

- Optional MCP support for Chenile Query.
- Owns MCP controller, query polymorph provider, and `queryPolymorph` bean.

Important files:

```text
query-mcp/src/main/java/org/chenile/configuration/query/mcp/QueryMCPController.java
query-mcp/src/main/java/org/chenile/configuration/query/mcp/QueryMcpConfiguration.java
query-mcp/src/main/java/org/chenile/query/service/impl/QueryPolymorph.java
```

Behavior:

- `QueryMCPController` exposes the same `POST /q/{queryName}` endpoint as the base controller.
- Base `QueryController` is guarded with `@ConditionalOnMissingBean(name = "queryMCPController")` so adding `query-mcp` replaces the base controller instead of creating duplicate mappings.

Tests:

```text
query-mcp/src/test/java/org/chenile/configuration/query/mcp/QueryMCPControllerTest.java
query-mcp/src/test/java/org/chenile/configuration/query/mcp/QueryMcpConfigurationTest.java
query-mcp/src/test/java/org/chenile/query/service/impl/QueryPolymorphTest.java
```

### Workflow Service

Path:

```text
workflow-service
```

Purpose:

- Generic workflow implementation.
- STM command handling.
- Workflow OpenAPI event expansion.

Important files:

```text
workflow-service/src/main/java/org/chenile/workflow/service/impl/StateEntityServiceImpl.java
workflow-service/src/main/java/org/chenile/workflow/service/stmcmds/StmBodyTypeSelector.java
workflow-service/src/main/java/org/chenile/workflow/service/config/ChenileOpenApiConfiguration.java
```

OpenAPI rule:

`ChenileOpenApiConfiguration` resolves `ChenileConfiguration` lazily using `ObjectProvider`.
Do not snapshot the service registry during bean construction.

### Workflow MCP

Path:

```text
workflow-mcp
```

Purpose:

- Optional MCP support for workflow.
- Keeps `workflow-service` free from `chenile-mcp`.

Important files:

```text
workflow-mcp/src/main/java/org/chenile/workflow/service/stmcmds/ProcessIdPolymorph.java
workflow-mcp/src/test/java/org/chenile/workflow/service/stmcmds/ProcessIdPolymorphTest.java
```

## Revyre Client Layer

Client path:

```text
/Users/gauravbhardwaj/work/revyre-core/core
```

Central query path:

```text
central-query
```

Important files:

```text
central-query/central-query-service/src/main/java/com/vymo/tech/query/configuration/controller/CQSController.java
central-query/central-query-service/src/main/java/com/vymo/tech/query/service/query/CentralQueryServiceImpl.java
central-query/central-query-api/src/main/java/com/vymo/tech/query/model/request/CentralQueryPayload.java
central-query/central-query-api/src/main/java/com/vymo/tech/query/model/response/ListViewResponse.java
central-query/central-query-service/src/main/resources/com/vymo/tech/query/service/mapper
central-query/central-query-service/src/test/java/com/vymo/tech/query/service/query/CentralQueryServiceImplTest.java
central-query/central-query-service/src/test/resources/features/service.feature
```

Client flow:

1. `CQSController` receives the client request.
2. `CentralQueryServiceImpl` converts `CentralQueryPayload` into Chenile `SearchRequest`.
3. The service calls Chenile `SearchService<Map<String, Object>>`.
4. Framework query runtime resolves query definition and mapper SQL.
5. Revyre response is converted into `ListViewResponse`.

When a client test fails with a framework stack trace:

- First identify whether the failing class is from `org.chenile.*` or `com.vymo.tech.*`.
- If it is `org.chenile.configuration.query.service.QueryConfiguration`, start in `chenile-query-service`.
- If it is `org.chenile.configuration.controller.QueryController`, start in `chenile-query-controller`.
- If it is `com.vymo.tech.query.service.query.CentralQueryServiceImpl`, start in `central-query-service`.

## Common Failure Patterns

### Spring introspection fails with `NoClassDefFoundError`

Typical stack:

```text
Failed to introspect Class [org.chenile.configuration.query.service.QueryConfiguration]
Caused by: NoClassDefFoundError: org/chenile/mcp/model/...
```

Likely cause:

- A base framework configuration/controller class exposes optional MCP types in imports, annotations, method parameters, or return types.

Fix direction:

- Move MCP-only code into `query-mcp` or `workflow-mcp`.
- Keep base modules free from `org.chenile.mcp.*`.
- Add or update introspection tests.

### Duplicate Spring MVC mapping

Likely cause:

- Both base controller and MCP controller expose the same path.

Fix direction:

- Ensure base controller backs off with `@ConditionalOnMissingBean(name = "...")`.
- Ensure MCP controller bean name matches Spring's default lower-camel-case class name.

For `QueryMCPController`, the expected bean name is:

```text
queryMCPController
```

### Query count behavior is wrong

Start here:

```text
query-api/src/main/java/org/chenile/query/service/AbstractSearchServiceImpl.java
chenile-query-service/src/main/java/org/chenile/configuration/query/service/QueryPaginationProperties.java
chenile-query-service/src/test/java/org/chenile/query/service/impl/QueryDefinitionsTest.java
```

Truth table:

| Global `query.pagination.countQueryEnabled` | Query JSON `countQueryEnabled` | Effective behavior |
| --- | --- | --- |
| absent | absent | count enabled |
| true | absent | count enabled |
| false | absent | count disabled |
| true | true | count enabled |
| true | false | count disabled |
| false | true | count enabled |
| false | false | count disabled |

## Focused Commands

Framework query service:

```bash
mvn -pl chenile-query-service -am test
```

Framework query controller and MCP:

```bash
mvn -pl chenile-query-controller,query-mcp -am test
```

Optional query MCP only:

```bash
mvn -pl query-mcp -am test
```

Workflow service:

```bash
mvn -pl workflow-service -am test
```

Workflow MCP:

```bash
mvn -pl workflow-mcp -am test
```

Install changed framework artifacts for client tests:

```bash
mvn -pl chenile-query-service,chenile-query-controller,query-mcp -am install -DskipTests
```

Client central-query:

```bash
cd /Users/gauravbhardwaj/work/revyre-core/core
mvn -pl central-query/central-query-service -am test
```

Dependency check for accidental MCP leakage:

```bash
mvn -pl chenile-query-controller dependency:tree -Dincludes=org.chenile:chenile-mcp
mvn -pl chenile-query-service dependency:tree -Dincludes=org.chenile:chenile-mcp
```

Expected result for base modules:

```text
BUILD SUCCESS with no org.chenile:chenile-mcp dependency tree entries
```

## Search Shortcuts

Use these before broad searches:

```bash
rg -n "org\\.chenile\\.mcp|ChenileMCP|ChenilePolymorph|ChenilePolymorphProvider" \
  chenile-query-service chenile-query-controller query-mcp workflow-service workflow-mcp
```

```bash
rg -n "countQueryEnabled|count query|nextPageAvailable" \
  query-api chenile-query-service docs
```

```bash
rg -n "CentralQueryPayload|SearchRequest|queryForMap|CQSController" \
  /Users/gauravbhardwaj/work/revyre-core/core/central-query
```

```bash
rg -n "ChenileOpenApiConfiguration|StmBodyTypeSelector|bodyTypeSelector" \
  workflow-service workflow-mcp
```

## Editing Rules For Agents

- Do not move optional MCP code back into base modules.
- Do not add `chenile-mcp` dependency to `chenile-query-service`, `chenile-query-controller`, or `workflow-service`.
- If changing framework artifacts used by Revyre client tests, run `mvn install` for the changed framework modules before running client tests.
- If touching client code, prefer module-specific tests under `central-query/central-query-service` before the full Revyre build.
- Preserve unrelated dirty files. Check `git status --short --untracked-files=all` before and after edits.
