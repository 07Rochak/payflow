$ErrorActionPreference = "Stop"

$Namespace = "payflow"

Write-Host "==> Creating/verifying namespace: $Namespace"

kubectl apply -f "$PSScriptRoot/namespace.yaml"

kubectl wait `
    --for=jsonpath="{.status.phase}"=Active `
    "namespace/$Namespace" `
    --timeout=30s

Write-Host "==> Creating/verifying persistent storage"

kubectl apply -f "$PSScriptRoot/payflow/pvc-logs.yaml"
kubectl apply -f "$PSScriptRoot/postgres/pvc.yaml"
kubectl apply -f "$PSScriptRoot/redis/pvc.yaml"

Write-Host ""
Write-Host "==> Persistent infrastructure is ready"
Write-Host ""

kubectl -n $Namespace get pvc