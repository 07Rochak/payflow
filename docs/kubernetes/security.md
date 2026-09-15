# PayFlow — Kubernetes Security

## Overview

PayFlow uses application-level security and Kubernetes deployment-level security.

The two layers serve different purposes.

Application security protects users, sessions, and API access.

Kubernetes security controls how containers and workloads run inside the cluster.

---

## Application Security

PayFlow includes application-level security features such as:

* Password hashing.
* JWT authentication.
* Refresh-token/session state.
* Redis-backed session management.
* Role-based access control.
* Protected endpoints.
* Admin restrictions.
* Session invalidation behavior.

See the main project README for application security details.

---

## Kubernetes Security

The Kubernetes deployment includes security-related configuration such as:

* Non-root container execution.
* ServiceAccount configuration.
* Secret resources.
* Resource requests and limits.

The exact security context should be verified against the final manifests.

---

## Secrets

Kubernetes Secrets are used for sensitive configuration.

Examples include:

* Database credentials.
* JWT secret.
* Razorpay credentials.

Secrets should not be committed to source control.

Kubernetes Secrets should not be treated as a complete production secrets-management solution.

---

## Hardening Considerations

Potential improvements include:

* `runAsNonRoot`.
* `allowPrivilegeEscalation: false`.
* Dropping unnecessary Linux capabilities.
* Read-only root filesystem where supported.
* Restricting network access with NetworkPolicy.
* External secrets management.
* HTTPS/TLS.
* Container image scanning.

These should be added only after verifying compatibility with PayFlow.

---

## Current Scope

This project demonstrates local Kubernetes security configuration.

It does not claim to provide complete production-grade security hardening.

---

## Related Documentation

* [Kubernetes Architecture](./architecture.md)
* [Production Considerations](./production-considerations.md)
