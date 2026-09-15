# PayFlow — Kubernetes Deployment and Access

## Overview

This document explains how to access the local Kubernetes cluster, deploy PayFlow, and verify the state of the application.

The commands are intended for the Docker Desktop Kubernetes environment.

---

## Prerequisites

* Docker Desktop installed.
* Kubernetes enabled in Docker Desktop.
* kubectl installed.
* Skaffold installed.
* Java and Maven wrapper available.
* PayFlow repository cloned locally.

---

## Verify Docker

```powershell
docker version
```

Verify that Docker is running and the client can communicate with the Docker Engine.

---

## Verify Kubernetes

```powershell
kubectl cluster-info
```

```powershell
kubectl get nodes
```

The node should report `Ready`.

---

## Deploy PayFlow

From the project root:

```powershell
skaffold run
```

This runs the configured Skaffold deployment workflow.

---

## Check Namespace

```powershell
kubectl get namespaces
```

```powershell
kubectl -n payflow get all
```

The namespace must match the configured namespace in the Kubernetes manifests.

---

## Check Pods

```powershell
kubectl -n payflow get pods
```

For more details:

```powershell
kubectl -n payflow describe pod <pod-name>
```

To inspect logs:

```powershell
kubectl -n payflow logs <pod-name>
```

---

## Check Deployments

```powershell
kubectl -n payflow get deployments
```

```powershell
kubectl -n payflow rollout status deployment/payflow
```

---

## Check Services

```powershell
kubectl -n payflow get services
```

---

## Check Gateway API

```powershell
kubectl -n payflow get gateway
```

```powershell
kubectl -n payflow get httproute
```

The exact namespace and resource names should be confirmed against the manifests.

---

## Check Application Health

PayFlow exposes Spring Boot Actuator endpoints.

The configured health endpoints include:

```text
/actuator/health
/actuator/health/readiness
/actuator/health/liveness
```

The application should be considered ready only after its readiness probe succeeds.

---

## Troubleshooting

| Problem                       | First check                          |
| ----------------------------- | ------------------------------------ |
| Application doesn't start     | Pod status and logs                  |
| PostgreSQL connection failure | PostgreSQL Pod and configuration     |
| Redis connection failure      | Redis Pod and configuration          |
| Login failure                 | Credentials, JWT, and session state  |
| Payment doesn't open          | Razorpay configuration               |
| Payment verification fails    | Test credentials and payment details |
| Port already in use           | Docker and host ports                |

---

## Related Documentation

* [Kubernetes Architecture](./architecture.md)
* [Skaffold Workflow](./skaffold.md)
* [Cleanup and Troubleshooting](./cleanup-and-troubleshooting.md)
