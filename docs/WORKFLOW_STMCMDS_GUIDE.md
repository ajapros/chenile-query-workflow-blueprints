# Workflow `stmcmds` Package Guide

This document explains the purpose of the classes under:

- `workflow-service/src/main/java/org/chenile/workflow/service/stmcmds`

These classes are the glue between:

- Chenile STM
- Spring bean lookup
- workflow XML metadata
- generated or handwritten transition actions

## The main idea

The workflow engine is generic.
Application-specific behavior is discovered by convention instead of being hardwired into the engine.

The `stmcmds` package provides:

- default transition execution
- convention-based transition action lookup
- payload-type inference for event bodies
- entry and exit actions
- post-save hooks
- automatic state computation
- activity support
- optional chaining of multiple transition actions
- security authority lookup for workflow events

## How the package works together

The normal runtime path is:

1. A workflow controller receives `processById(id, eventID, payload)`.
2. `StmBodyTypeSelector` decides how to deserialize the event payload.
3. STM executes the transition.
4. `BaseTransitionAction` acts as the default dispatcher for the event.
5. `STMTransitionActionResolver` looks up the event-specific Spring bean by naming convention.
6. The typed transition action runs.
7. `GenericEntryAction` persists the entity and optionally invokes post-save hooks.
8. If configured, automatic state computation or additional chained actions also run.

## Key classes

### `STMTransitionActionResolver`

This is the central naming-convention resolver.

It resolves three kinds of components:

- transition actions
- post-save hooks
- automatic state computations

It builds Spring bean names from:

- a workflow prefix
- an event ID or state ID
- an optional suffix

Typical naming patterns are:

- transition action: `prefix + Capitalize(eventId)`
- transition action with suffix enabled: `prefix + Capitalize(eventId) + "Action"`
- post-save hook with suffix enabled: `prefix + Capitalize(stateId) + "PostSaveHook"`
- auto-state with suffix enabled: `prefix + Capitalize(stateId) + "AutoState"`

The resolver also supports context-sensitive prefixes using headers stored in `ContextContainer`.
That allows client-specific or tenant-specific overrides.

### `BaseTransitionAction`

This is the usual default STM transition action.

It dispatches the transition in this order:

1. execute a direct OWIZ `command` bean if named in transition metadata
2. execute an OWIZ `orchExecutor` bean if named in metadata
3. execute an orchestration XML if `orchestratedCommandsConfiguration` is present
4. otherwise resolve the event-specific transition action using `STMTransitionActionResolver`

It also supports activity management when an `ActivityChecker` is injected.
That allows the workflow to:

- log activity transitions
- enforce completion of mandatory activities before completion-checker transitions proceed

### `AbstractSTMTransitionAction`

Chenile STM transition actions receive payload as `Object`.

This class exists so subclasses can declare a strongly typed payload in `transitionTo(...)`.
That gives two benefits:

- transition code can work with a specific payload type
- `StmBodyTypeSelector` can infer the expected payload type using reflection

This is the base class expected by the generator blueprints for event-specific actions.

### `StmBodyTypeSelector`

This class selects the request body type for workflow event processing.

It works in two steps:

1. if event metadata explicitly defines `bodyType`, use that
2. otherwise inspect the resolved transition action and infer the payload type from the second parameter of `transitionTo(...)`

This is what allows generated workflow controllers to handle different payload types for different events without hardcoding every event in the controller.

### `GenericEntryAction`

This is the standard STM entry action for persisted workflow entities.

It:

- updates state-entry time
- computes SLA timing fields
- stores the entity using `EntityStore`
- invokes a `PostSaveHook` if one is configured

This is the persistence boundary after a transition.

### `GenericExitAction`

This is a minimal default STM exit action.

Right now it is mostly a placeholder extension point.
It exists so applications can replace or extend exit behavior without changing the workflow engine.

### `PostSaveHook`

This is a small interface for behavior that should run after the entity has been stored during entry action processing.

Typical uses would be:

- side effects
- integration callbacks
- audit or notification logic

### `DefaultPostSaveHook`

This class applies the same naming-convention idea to post-save behavior.

It resolves a state-specific `PostSaveHook` bean using the current state ID and delegates to it if found.

Example with suffix enabled:

- current state `resolved`
- prefix `issue`
- resolved hook bean name `issueResolvedPostSaveHook`

### `DefaultAutomaticStateComputation`

This class applies naming-convention lookup to automatic state computation.

It resolves a state-specific automatic-state bean using the current state ID and delegates to it.

Example with suffix enabled:

- current state `readyForClosure`
- prefix `issue`
- resolved bean name `issueReadyForClosureAutoState`

### `MultipleCommandsRegistry`

This is a registry for follow-up transition actions attached to the same event.

The registry stores secondary actions per event with an integer order.
After the primary typed action runs, registered secondary actions are executed in order.

### `SecondSTMTransitionAction`

This is the base class for those follow-up transition actions.

It:

- extends `AbstractSTMTransitionAction`
- registers itself against one or more event IDs on application startup
- executes after the main transition action

The index passed during registration controls execution order.

### `GenericRetrievalStrategy`

This class helps STM retrieve a persisted state entity from `EntityStore` when the incoming object only carries an ID.

It is a small persistence helper used before transition processing.

### `StmAuthoritiesBuilder`

This builds a function that maps the incoming workflow event to required authorities.

It reads ACL data from STM event metadata.
If explicit ACL metadata is absent and default ACLs are enabled, it derives a fallback authority name from service reference and event ID.

This is how workflow event authorization can be driven directly from workflow definitions.

### `ProcessIdPolymorph`

This class exposes the polymorphic nature of `processById`.

Different events can require different payload types.
`ProcessIdPolymorph` uses `StmBodyTypeSelector` to enumerate those variants and publish them as polymorphic operation variants for Chenile MCP metadata.

That is mainly useful for tools, introspection, and generated contracts.

## Concrete naming examples

Assume:

- workflow prefix: `issue`
- `useSuffix = true`

Then these names are expected:

- event `assign` -> `issueAssignAction`
- event `close` -> `issueCloseAction`
- state `resolved` post-save hook -> `issueResolvedPostSaveHook`
- state `readyForClosure` auto-state -> `issueReadyForClosureAutoState`

If `useSuffix = false`, the names become:

- `issueAssign`
- `issueClose`
- `issueResolved`
- `issueReadyForClosure`

## End-to-end example for `PATCH /issue/{id}/assign`

1. The HTTP controller receives event ID `assign`.
2. `StmBodyTypeSelector` determines the payload type for `assign`.
3. STM starts the transition.
4. `BaseTransitionAction` runs as the default transition action.
5. If there is no metadata-driven OWIZ command, it asks `STMTransitionActionResolver` for the bean for `assign`.
6. The resolver returns `issueAssignAction` when suffix mode is enabled.
7. The resolved action, usually a subclass of `AbstractSTMTransitionAction`, executes typed business logic.
8. If secondary actions were registered through `SecondSTMTransitionAction`, they run afterward in configured order.
9. `GenericEntryAction` stores the entity and invokes a state-specific post-save hook if present.
10. The workflow service returns the mutated entity and allowed actions.

## Design tradeoff

This package favors convention over explicit per-transition wiring.

That has two consequences:

- application code is smaller and generator-friendly
- naming conventions become part of the contract and must be followed carefully

That tradeoff is intentional and is the reason the code generators in `chenile-gen` can emit small, mostly declarative workflow services.
