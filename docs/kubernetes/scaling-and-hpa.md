# PayFlow — Horizontal Pod Autoscaling

## Overview

PayFlow uses Kubernetes Horizontal Pod Autoscaling to adjust the number of application Pods based on resource utilization.

HPA allows Kubernetes to increase or decrease the number of PayFlow replicas according to the configured scaling targets.

---

## HPA Architecture

```text
Application Pods
      |
      v
Resource Usage
      |
      v
Metrics Server
      |
      v
HorizontalPodAutoscaler
      |
      v
Desired Replica Count
      |
      v
PayFlow Deployment
```

---

## Configuration

The expected HPA configuration from the project roadmap is:

| Setting          | Value |
| ---------------- | ----- |
| Minimum replicas | 2     |
| Maximum replicas | 5     |
| CPU target       | 70%   |
| Memory target    | 80%   |

These values must be confirmed against the final HPA manifest before publishing them as current configuration.

---

## Verify HPA

```powershell
kubectl -n payflow get hpa
```

```powershell
kubectl -n payflow describe hpa payflow-hpa
```

---

## Verify Metrics

```powershell
kubectl top pods -n payflow
```

```powershell
kubectl top nodes
```

Metrics Server provides resource usage information used by HPA.

---

## Scaling Behavior

The expected scale-up flow is:

```text
Increased Application Load
          |
          v
Higher CPU / Memory Usage
          |
          v
Metrics Server
          |
          v
HPA Calculates Desired Replicas
          |
          v
PayFlow Deployment Scales
          |
          v
New Pod Becomes Ready
```

---

## Verification Status

### Configuration

The HPA configuration is part of the Kubernetes implementation.

### Scale-Up

The roadmap records a previously observed HPA state of:

```text
CPU:     3% / 70%
Memory: 73% / 80%
Min:     2
Max:     5
Current: 3
```

This demonstrates an observed HPA state, but it does not by itself prove a complete scale-up test from 2 to 5 replicas.

### Scale-Down

Scale-down should be documented only after a controlled test has confirmed the behavior.

The configured stabilization window, if present, should be verified from the HPA manifest.

---

## Documentation Rules

Do not claim:

* HPA scaled from 2 to 5 replicas unless that test was recorded.
* Scale-down completed unless the test was performed.
* Memory scaling was verified unless the relevant test was performed.
* HPA guarantees application performance under arbitrary load.

HPA changes replica count. It does not automatically guarantee that the application, database, cache, or external payment provider can handle every level of traffic.

---

## Related Documentation

* [Kubernetes Architecture](./architecture.md)
* [Resilience Testing](./resilience-testing.md)
* [Observability](./observability.md)
