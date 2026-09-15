# PayFlow — Self-Healing and Rolling Updates

## Overview

Kubernetes manages PayFlow through a Deployment.

The Deployment maintains the desired number of replicas and replaces Pods that terminate or become unavailable.

PayFlow also uses a RollingUpdate strategy to allow application updates without terminating all replicas simultaneously.

---

## Pod Self-Healing

```text
PayFlow Pod Terminates
        |
        v
Deployment Controller Detects Replica Deficit
        |
        v
Replacement Pod Created
        |
        v
Startup Probe
        |
        v
Readiness Probe
        |
        v
Pod Becomes Ready
```

---

## Rolling Updates

```text
Existing Application Version
        |
        v
Deployment Updated
        |
        v
New Pods Created
        |
        v
New Pods Become Ready
        |
        v
Old Pods Terminated Gradually
        |
        v
Updated Deployment
```

---

## Verification

```powershell
kubectl -n payflow rollout status deployment/payflow
```

```powershell
kubectl -n payflow rollout history deployment/payflow
```

To test Pod replacement:

```powershell
kubectl -n payflow delete pod <pod-name>
```

Observe:

* The original Pod terminates.
* A replacement Pod is created.
* Startup and readiness checks run.
* The replacement Pod becomes Ready.

---

## Verification Status

The Deployment configuration and rolling update behavior must be confirmed against the current manifests and recorded test output.

A configured Deployment does not, by itself, prove that self-healing and rolling updates were tested.

---

## Related Documentation

* [Kubernetes Architecture](./architecture.md)
* [Scaling and HPA](./scaling-and-hpa.md)
* [Resilience Testing](./resilience-testing.md)
