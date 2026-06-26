# Proposal: fix-403-path-matching

## Intent

Add missing `PathPatternRequestMatcherBuilderFactoryBean` to bridge Spring Security 7's `PathPatternRequestMatcher` with Spring MVC's `PathPatternParser`. This fixes inconsistent 403 errors on valid JWT-authenticated requests (e.g. `GET /api/v1/orders`), caused by Spring Boot 4.0.6 / Security 7.0.5 removing `AntPathRequestMatcher` without aligning the pattern parser between the two frameworks.

## Scope

### In Scope
- Add `PathPatternRequestMatcherBuilderFactoryBean` bean to `SecurityConfig.java`
- Add required import from `org.springframework.security.web.util.matcher`
- Verify `./mvnw test` passes
- Manual verification: `GET /api/v1/orders` returns 200 with valid JWT

### Out of Scope
- Changes to authorization rules, roles, or permission model
- JWT token generation, validation, or filter logic
- Other path matchers or security configuration

## Capabilities

No spec-level behavior changes — this is an internal infrastructure alignment fix.

### New Capabilities
None

### Modified Capabilities
None

## Approach

1. Add one `@Bean` method to `SecurityConfig.java` that returns `new PathPatternRequestMatcherBuilderFactoryBean()`
2. The bean synchronises Spring Security's pattern parser with Spring MVC's, eliminating inconsistent 403 errors on certain HTTP methods
3. Run `./mvnw test` to confirm no regressions
4. Manual curl test: `GET /api/v1/orders` with valid JWT returns 200

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/deliveryhub/infrastructure/security/SecurityConfig.java` | Modified | Add one `@Bean` method + import |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Bean conflicts with custom `PathPatternParser` | Low | Default constructor only — no custom parser overrides |
| Spring Boot auto-config already provides this bean | Low | Test verifies 403 is gone; rollback is a one-line revert |

## Rollback Plan

Remove the `usePathPattern()` bean method and its import from `SecurityConfig.java`. Revert to the commit before this change.

## Dependencies

- Spring Security 7.0.5 (already on classpath)
- Spring Boot 4.0.6 (already on classpath)

## Success Criteria

- [ ] `./mvnw test` passes
- [ ] `curl GET /api/v1/orders` with valid JWT returns HTTP 200
- [ ] `curl GET /api/v1/orders` with invalid/missing JWT returns HTTP 401
