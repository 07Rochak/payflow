# PayFlow — Kubernetes Resilience Testing

## Overview

This document records resilience tests performed against the PayFlow Kubernetes deployment.

The purpose is to verify how the application responds to Pod failures, scaling events, rolling updates, and supporting service disruptions.

---

## Test Categories

| Test                 | Expected Behavior                                      |
| -------------------- | ------------------------------------------------------ |
| PayFlow Pod deletion | Deployment creates replacement                         |
| PayFlow Pod failure  | Replacement Pod becomes Ready                          |
| HPA scale-up         | Replica count increases under load                     |
| HPA scale-down       | Replica count decreases after load reduces             |
| Rolling update       | New Pods become Ready before old Pods terminate        |
| PostgreSQL restart   | Database recovers using configured storage             |
| Redis restart        | Application behavior matches configured Redis recovery |
| Probe failure        | Kubernetes responds according to probe configuration   |

---

## Test Record

Each test should record:

* Date.
* Test name.
* Initial state.
* Command executed.
* Observed behavior.
* Final state.
* Evidence.
* Result.

---

## Example: Pod Replacement

```powershell
kubectl -n payflow get pods
```

```powershell
kubectl -n payflow delete pod <pod-name>
```

```powershell
kubectl -n payflow get pods -w
```

Verify that a replacement Pod is created and becomes Ready.

---

## Example: Rolling Update

```powershell
kubectl -n payflow rollout status deployment/payflow
```

```powershell
kubectl -n payflow rollout history deployment/payflow
```

---

## Verification Rules

Do not mark a test as passed based only on the presence of a Kubernetes resource.

A passing test requires observable evidence of the expected behavior.

---

## Related Documentation

* [Self-Healing and Rolling Updates](./self-healing-and-rolling-updates.md)
* [Scaling and HPA](./scaling-and-hpa.md)
* [Persistent Storage](./persistent-storage.md)
