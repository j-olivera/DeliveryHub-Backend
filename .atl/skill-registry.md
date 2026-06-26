# Skill Registry

This file indexes available skills for the `deliveryhub-backend` project.

| Skill | Description | Trigger/Context | Path |
|---|---|---|---|
| java-coding-standards | Java coding standards for Spring Boot services: naming, immutability, Optional usage, streams, exceptions, generics, project layout | Writing or reviewing Java code | `.agents/skills/java-coding-standards/SKILL.md` |
| java-docs | Ensure Java types documented with Javadoc following best practices | Documenting Java types | `.agents/skills/java-docs/SKILL.md` |
| java-springboot | Best practices for developing applications with Spring Boot | Developing Spring Boot applications | `.agents/skills/java-springboot/SKILL.md` |
| sdd-init | Initialize SDD context, testing capabilities, registry, and persistence | Project initialization, SDD bootstrap | system skill (opencode built-in) |
| sdd-propose | Create an SDD change proposal with intent, scope, and approach | Starting a new change workflow | system skill (opencode built-in) |
| sdd-spec | Write SDD delta specs with requirements and scenarios | After proposal is approved | system skill (opencode built-in) |
| sdd-design | Create SDD technical design and architecture approach | After spec is approved | system skill (opencode built-in) |
| sdd-tasks | Break an SDD change into implementation tasks | After design is approved | system skill (opencode built-in) |
| sdd-apply | Implement SDD tasks from specs and design | Executing implementation | system skill (opencode built-in) |
| sdd-verify | Execute tests and prove implementation matches specs, design, and tasks | After implementation | system skill (opencode built-in) |
| sdd-archive | Archive completed SDD change by syncing delta specs | After verification passes | system skill (opencode built-in) |
| sdd-explore | Explore SDD ideas before committing to a change | Early exploration / requirement clarification | system skill (opencode built-in) |
| sdd-onboard | Walk users through the SDD workflow on the real codebase | Onboarding, training | system skill (opencode built-in) |
