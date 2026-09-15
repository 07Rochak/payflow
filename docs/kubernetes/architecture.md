# PayFlow — Kubernetes Architecture

## Overview

PayFlow runs inside a Kubernetes namespace with separate resources for the application, database, cache, networking, configuration, and scaling.

The Kubernetes architecture is separate from the application architecture described in the main README.

The application architecture explains how business requests flow through PayFlow.

This document explains how Kubernetes deploys, exposes, scales, and manages those application components.

---

## Architecture

```text
Docker Image
     |
     v
  Skaffold
     |
     v
Kubernetes Namespace
     |
     +------------------------+
     |                        |
     v                        v
PayFlow Deployment       Supporting Services
     |                        |
     |                        +--------------------+
     |                        |                    |
     v                        v                    v
PayFlow Service          PostgreSQL             Redis
     |                        |                    |
     |                        v                    v
     |                       PVC                  PVC
     |
     v
Gateway API
     |
     +----------------+
     |                |
     v                v
  Gateway         HTTPRoute
     |
     v
Envoy Gateway
     |
     v
PayFlow Service
     |
     v
PayFlow Pods
```

---

## Kubernetes Resources

| Component           | Kubernetes Resource               | Responsibility                                            |
| ------------------- | --------------------------------- | --------------------------------------------------------- |
| PayFlow             | Deployment                        | Maintains application Pods                                |
| PayFlow networking  | Service                           | Routes traffic to PayFlow Pods                            |
| PostgreSQL          | Deployment                        | Runs the database                                         |
| PostgreSQL storage  | PVC                               | Provides persistent storage                               |
| Redis               | Deployment                        | Runs the cache/session store                              |
| Redis storage       | PVC                               | Provides persistent storage                               |
| Configuration       | ConfigMap                         | Provides non-sensitive configuration                      |
| Credentials         | Secret                            | Provides sensitive configuration                          |
| Scaling             | HPA                               | Adjusts PayFlow replica count                             |
| Availability        | PDB                               | Protects against excessive voluntary disruption           |
| External access     | Gateway                           | Provides Gateway API entry point                          |
| Routing             | HTTPRoute                         | Routes requests to the PayFlow Service                    |
| Health              | Startup/Readiness/Liveness probes | Controls startup, traffic readiness, and health detection |
| Deployment workflow | Skaffold                          | Builds and deploys the application                        |

---

## Namespace

PayFlow resources are deployed into the PayFlow Kubernetes namespace.

The namespace provides a logical boundary for the application's Kubernetes resources.

All commands in the operational documentation should use the configured namespace rather than assuming that resources exist in the default namespace.

---

## PayFlow Deployment

The PayFlow Deployment manages the application Pods.

Its responsibilities include:

* Maintaining the desired number of replicas.
* Replacing terminated Pods.
* Managing rolling updates.
* Applying resource requests and limits.
* Configuring health probes.
* Providing the application container configuration.

The exact Deployment configuration is defined in the Kubernetes manifests.

---

## Networking

Traffic is routed through the Gateway API resources.

```text
Client
  |
  v
Gateway
  |
  v
HTTPRoute
  |
  v
PayFlow Service
  |
  v
PayFlow Pod
```

The Service provides stable networking for the application Pods.

The Gateway and HTTPRoute provide the configured external access path.

---

## Configuration and Secrets

PayFlow uses Kubernetes configuration resources to separate configuration from application image contents.

ConfigMap:

* Non-sensitive application configuration.

Secret:

* Database credentials.
* JWT secret.
* Razorpay credentials.
* Other sensitive configuration defined by the deployment.

Sensitive values should not be committed to source control.

---

## Scaling and Availability

The application supports multiple replicas.

Kubernetes manages application availability through:

* Deployment replica management.
* Readiness probes.
* RollingUpdate strategy.
* PodDisruptionBudget.
* HorizontalPodAutoscaler.

See [Scaling and HPA](./scaling-and-hpa.md) and [Self-Healing and Rolling Updates](./self-healing-and-rolling-updates.md).

---

## Related Documentation

* [Skaffold Workflow](./skaffold.md)
* [Deployment and Access](./deployment-and-access.md)
* [Scaling and HPA](./scaling-and-hpa.md)
* [Persistent Storage](./persistent-storage.md)
