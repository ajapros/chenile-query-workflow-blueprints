# chenile-query-workflow-blueprints
This contains some blue prints for workflow and query. We can use these to quickly develop chenile applications
that conform to these blueprints. Also see chenile-gen for generators of code that comply to these blueprints

See [docs/REPO_OVERVIEW.md](docs/REPO_OVERVIEW.md) for a module map, dependency direction, request flow, and contributor reading order.
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

## Query Providers

The default query provider is `mybatis`. For JDBC databases whose SQL is compatible with the configured MyBatis
mappers, applications only need to change `query.datasources`, `query.mapperFiles`, and `query.definitionFiles`.

Applications can add support for another database by registering a Spring bean that implements
`QueryExecutionProvider` and setting `query.provider` to that provider name. See
[the query provider extension guide](https://chenile.org/query-provider-extension.html) for JDBC and non-SQL extension examples.

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
