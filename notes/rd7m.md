---
title: DI with Koin Compiler
date: 07/09/2026
tags: []
---

Dependency Injection is managed with Koin 4 and its compiler plugin.
- @Factory: used for Presenters that should be re-created as needed.
- @Singleton / @Single: used for repositories and singletons.
- @ComponentScan: used to automatically find and register annotated classes within a module.
In MainActivity and Views, 'koinInject()' is used to retrieve instances. This setup provides compile-time safety and reduces boilerplate for manual module definition.


## Links
