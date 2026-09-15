# PayFlow — Cleanup and Troubleshooting

## Overview

This document contains operational commands for inspecting, troubleshooting, and cleaning up the local Kubernetes deployment.

Use cleanup commands carefully because deleting Kubernetes resources can remove application access or persistent data.

---

## Inspect Resources

```powershell
kubectl -n payflow get all
```

```powershell
kubectl -n payflow get pods
```

```powershell
kubectl -n payflow get pvc
```

```powershell
kubectl -n payflow get hpa
```

```powershell
kubectl -n payflow get gateway
```

```powershell
kubectl -n payflow get httproute
```

---

## Inspect Pod Logs

```powershell
kubectl -n payflow logs <pod-name>
```

For a previous container instance:

```powershell
kubectl -n payflow logs <pod-name> --previous
```

---

## Describe Resources

```powershell
kubectl -n payflow describe pod <pod-name>
```

```powershell
kubectl -n payflow describe deployment <deployment-name>
```

---

## Common Problems

| Problem                     | First Check                           |
| --------------------------- | ------------------------------------- |
| Pod stuck Pending           | Resource availability and events      |
| Pod CrashLoopBackOff        | Logs and container configuration      |
| Readiness probe failure     | Application health endpoint           |
| Database connection failure | PostgreSQL Pod and Service            |
| Redis connection failure    | Redis Pod and Service                 |
| Gateway access failure      | Gateway and HTTPRoute                 |
| Image pull failure          | Image name and tag                    |
| Deployment failure          | Skaffold output and Kubernetes events |

---

## Cleanup

Before cleanup, identify the resources that will be deleted.

Example:

```powershell
kubectl -n payflow get all
```

To remove a deployment:

```powershell
kubectl -n payflow delete deployment <deployment-name>
```

To remove a Pod:

```powershell
kubectl -n payflow delete pod <pod-name>
```

To remove a PVC:

```powershell
kubectl -n payflow delete pvc <pvc-name>
```

**PVC deletion can result in data loss depending on the storage configuration.**

Do not use namespace deletion or PVC deletion as a routine cleanup step without confirming the intended effect.

---

## Related Documentation

* [Deployment and Access](./deployment-and-access.md)
* [Persistent Storage](./persistent-storage.md)
* [Backup and Recovery](./backup-and-recovery.md)
