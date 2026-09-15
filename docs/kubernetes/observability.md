# PayFlow — Kubernetes Observability

## Overview

Observability helps understand the health, performance, and behavior of PayFlow.

The project exposes Spring Boot Actuator endpoints for application health and metrics.

Kubernetes resource metrics are used for HPA.

These are related but distinct concerns.

---

## Application Health

PayFlow exposes:

```text
/actuator/health
/actuator/health/readiness
/actuator/health/liveness
```

The application uses health probes to control startup, readiness, and liveness behavior.

---

## Kubernetes Resource Metrics

Metrics Server provides resource usage information.

Example commands:

```powershell
kubectl top pods -n payflow
```

```powershell
kubectl top nodes
```

These metrics support resource monitoring and HPA decisions.

---

## Application Metrics

PayFlow exposes application metrics through Actuator.

The configured endpoint exposure includes:

```text
health
info
metrics
prometheus
startup
```

The exact configuration should be verified against the current application configuration.

---

## Logs

Application logs can be inspected through Kubernetes:

```powershell
kubectl -n payflow logs <pod-name>
```

Logs are useful for investigating:

* Application startup.
* Database connectivity.
* Redis connectivity.
* Authentication failures.
* Payment processing.
* Pod restarts.
* Deployment issues.

---

## Future Observability

A production deployment may add:

* Prometheus.
* Grafana.
* Centralized logging.
* Alerting.
* Distributed tracing.
* Long-term metrics retention.

These are not required to document the current local Kubernetes deployment unless they are implemented.

---

## Related Documentation

* [Scaling and HPA](./scaling-and-hpa.md)
* [Deployment and Access](./deployment-and-access.md)
* [Production Considerations](./production-considerations.md)
