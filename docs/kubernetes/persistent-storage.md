# PayFlow — Persistent Storage

## Overview

PayFlow uses PostgreSQL and Redis as supporting services.

Kubernetes PersistentVolumeClaims provide persistent storage for these services.

Persistent storage is intended to preserve data beyond the lifetime of an individual Pod.

---

## Storage Architecture

```text
PostgreSQL Deployment
        |
        v
PostgreSQL PVC
        |
        v
Persistent Volume
```

```text
Redis Deployment
        |
        v
Redis PVC
        |
        v
Persistent Volume
```

---

## PostgreSQL

PostgreSQL stores persistent application data such as:

* Users.
* Wallets.
* Transactions.
* Payment records.
* Other database entities defined by the application.

The PostgreSQL Deployment mounts its configured PVC.

---

## Redis

Redis is used by PayFlow for session-related state and other cache functionality defined by the application.

The Redis Deployment mounts its configured PVC.

Redis persistence behavior depends on the Redis configuration and storage setup.

A PVC alone does not guarantee that every Redis key survives every failure scenario.

---

## Verify PVCs

```powershell
kubectl -n payflow get pvc
```

```powershell
kubectl -n payflow get pv
```

---

## Persistence Verification

A persistence test should:

1. Create or identify known application data.
2. Verify the data exists.
3. Restart or recreate the relevant Pod.
4. Confirm that the same PVC is mounted.
5. Verify that the data remains available.

---

## Important Distinction

A PVC provides persistent storage.

It is not a complete backup solution.

Backups and restoration must be documented separately.

---

## Related Documentation

* [Backup and Recovery](./backup-and-recovery.md)
* [Kubernetes Architecture](./architecture.md)
* [Resilience Testing](./resilience-testing.md)
