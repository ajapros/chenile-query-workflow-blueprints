# chenile-query-workflow-blueprints
This contains some blue prints for workflow and query. We can use these to quickly develop chenile applications
that conform to these blueprints. Also see chenile-gen for generators of code that comply to these blueprints

See [docs/REPO_OVERVIEW.md](docs/REPO_OVERVIEW.md) for a module map, dependency direction, request flow, and contributor reading order.
Agents should start with [docs/AGENT_TRAVERSAL_GUIDE.md](docs/AGENT_TRAVERSAL_GUIDE.md) to avoid broad searches across the framework and client layers.
See the Chenile docs site for [query migration](https://chenile.org/query-framework-migration-guide.html) and
[query provider extension](https://chenile.org/query-provider-extension.html) guidance.

## Query Pagination

Paginated MyBatis queries execute the `<queryId>-count` query by default so responses can include exact
`maxRows` and `maxPages`. Services can disable this count query globally:

```yaml
query:
  pagination:
    countQueryEnabled: false
```

When disabled, query-service fetches `pageSize + 1` rows, trims the extra row, and returns
`pagination.nextPageAvailable` instead of exact totals.

Individual query definitions can override the global setting:

```json
{
  "id": "Student.getAll",
  "name": "students",
  "paginated": true,
  "countQueryEnabled": false
}
```

When `countQueryEnabled` is absent, the query follows `query.pagination.countQueryEnabled`. Set it to
`true` to force count execution for a specific query, or `false` to force no-count pagination for that query.

## Query Providers

The default query provider is `mybatis`. For JDBC databases whose SQL is compatible with the configured MyBatis
mappers, applications only need to change `query.datasources`, `query.mapperFiles`, and `query.definitionFiles`.

Applications can add support for another database by registering a Spring bean that implements
`QueryExecutionProvider` and setting `query.provider` to that provider name. See
[the query provider extension guide](https://chenile.org/query-provider-extension.html) for JDBC and non-SQL extension examples.

## Database Query Catalog

MyBatis mapper XML and query-definition JSON can be loaded once at application startup from a
deployment-managed database catalog. It is disabled by default; existing `query.mapperFiles` and
`query.definitionFiles` continue to work. When enabled, either property may be omitted for a
catalog-only deployment. Enable the catalog only after the application exposes a dedicated read-only
`DataSource` bean named `queryCatalogDataSource` (or configure another bean name):

```yaml
query:
  catalog:
    jdbc:
      enabled: true
      dataSource: queryCatalogDataSource
      baseScope: __base__
```

Apply `chenile-query-catalog-schema.sql` through Liquibase or the application's migration tool.
Each mapper row stores one complete, global MyBatis XML document and its root namespace. Each definition
row stores one complete JSON query definition. Use `__base__` for a base definition or the tenant id
for an override. A tenant definition can select a tenant-qualified global statement such as
`tenant1.Student.getAll`. Enabled database rows override packaged rows with the same mapper namespace
or the same `(tenant scope, external query name)`; unrelated packaged rows remain available.

The catalog is read only at runtime: there is no write API, hot reload, scheduler, or polling loop.
Configuration updates are made through reviewed migration/admin SQL and take effect after restart.
Any invalid enabled XML, JSON, scope mismatch, duplicate database mapper namespace, or unresolved
database query statement prevents startup.

The repository now also includes `workflow-info`, a Chenile service layer for workflow introspection.
It supports:

- an XML-driven `workflowInfoService` built on `workflow-utils`
- a runtime-bound `StateEntityInfoServiceImpl` that is instantiated by generated workflow-service blueprints against a live `STMFlowStoreImpl`
- PNG-rendered workflow diagrams for admin and tooling use, instead of raw PlantUML text

# About chenile

Chenile is an open source framework for creating Micro services using Java and Spring Boot. 
Please check the details out at https://chenile.org

It provides an interception framework to decouple functional and non-functional requirements.
Chenile avoids the need to write repetitive code. It encourages modular coding best practices. 

In addition to creating REST services, Chenile services can also be used to create event processors, 
schedulers (with quartz), a file watcher etc. without the need for rewriting the code. 

Chenile has a state machine and an orchestration engine.  

The orchestration engine is internally used by Chenile to provide an interception framework that helps in 
disinter-mediating traffic irrespective of the incoming protocol (HTTP, message etc.)

Hence Chenile also serves like an IN-VM message bus. Chenile also facilitates easy swagger documentation 
(using Spring doc). 
Chenile allows the development of Cucumber based BDD tests with most of the plumbing already in place.
Chenile also is integrated with [keycloak](https://www.keycloak.org/) for security. 

Finally, Chenile ships with its own code generators to ease the development of micro services. 
Please see [Code Generation Repository](https://github.com/rajakolluru/chenile-gen) for more information 
about the code generator.
