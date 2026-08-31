# PayFlow Kubernetes

Kubernetes manifests for the PayFlow application.

## Namespace

All PayFlow workloads run inside the `payflow` namespace.

## Components

- PayFlow Spring Boot application
- PostgreSQL 15
- Redis Stack
- Persistent storage for PostgreSQL
- Persistent storage for Redis
- Persistent storage for PayFlow application logs

## Configuration

Non-sensitive configuration is stored in `ConfigMap`.

Sensitive runtime configuration is stored in a Kubernetes `Secret`.

The repository should not contain real credential values.

## Current phase

This directory currently contains the base configuration and persistent storage resources.

The deployment sequence is:

1. Namespace
2. ConfigMap / Secret
3. PersistentVolumeClaims
4. PostgreSQL
5. Redis
6. PayFlow
7. Services
8. Health probes
9. Scaling / resilience